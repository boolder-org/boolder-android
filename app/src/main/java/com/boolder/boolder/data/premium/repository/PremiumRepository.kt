package com.boolder.boolder.data.premium.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.boolder.boolder.data.datastore.PREF_PREMIUM_SUBSCRIBED
import com.boolder.boolder.data.premium.model.PremiumStatus
import com.boolder.boolder.data.premium.model.toPremiumStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class PremiumRepository(
    context: Context,
    private val dataStore: DataStore<Preferences>
) {

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _status = MutableStateFlow(readCachedStatus())
    val status: StateFlow<PremiumStatus> = _status.asStateFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, _ -> if (billingResult.isOk()) queryPurchases() }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    fun start() {
        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.isOk()) queryPurchases()
                }

                override fun onBillingServiceDisconnected() = Unit
            }
        )
    }

    private fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (!billingResult.isOk()) return@queryPurchasesAsync

            purchases
                .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                .forEach(::acknowledgePurchase)

            updateStatus(purchases.toPremiumStatus())
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) {}
    }

    private fun updateStatus(status: PremiumStatus) {
        _status.value = status

        coroutineScope.launch {
            dataStore.edit { it[PREF_PREMIUM_SUBSCRIBED] = status == PremiumStatus.SUBSCRIBED }
        }
    }

    private fun readCachedStatus(): PremiumStatus {
        val isSubscribed = runBlocking { dataStore.data.first()[PREF_PREMIUM_SUBSCRIBED] } ?: false

        return if (isSubscribed) PremiumStatus.SUBSCRIBED else PremiumStatus.NOT_SUBSCRIBED
    }

    private fun BillingResult.isOk(): Boolean =
        responseCode == BillingResponseCode.OK
}
