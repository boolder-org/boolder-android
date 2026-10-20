package com.boolder.boolder.data.premium.repository

import android.app.Activity
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ConnectionState
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.InAppMessageParams
import com.android.billingclient.api.InAppMessageParams.InAppMessageCategoryId
import com.android.billingclient.api.InAppMessageResult.InAppMessageResponseCode
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.boolder.boolder.data.database.repository.AreaRepository
import com.boolder.boolder.data.datastore.PREF_PREMIUM_INTRO_SEEN
import com.boolder.boolder.data.datastore.PREF_PREMIUM_SUBSCRIBED
import com.boolder.boolder.data.premium.model.PREMIUM_PRODUCT_ID
import com.boolder.boolder.data.premium.model.PremiumIntroDecision
import com.boolder.boolder.data.premium.model.PremiumProduct
import com.boolder.boolder.data.premium.model.PremiumStatus
import com.boolder.boolder.data.premium.model.PurchaseResult
import com.boolder.boolder.data.premium.model.RestoreResult
import com.boolder.boolder.data.premium.model.decidePremiumIntro
import com.boolder.boolder.data.premium.model.toPremiumProduct
import com.boolder.boolder.data.premium.model.toPremiumStatus
import com.boolder.boolder.data.premium.model.toPurchaseResult
import com.boolder.boolder.data.premium.model.toRestoreResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

class PremiumRepository(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val areaRepository: AreaRepository
) {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _status = MutableStateFlow(readCachedStatus())
    val status: StateFlow<PremiumStatus> = _status.asStateFlow()

    private val isStatusResolved = MutableStateFlow(false)

    private val _product = MutableStateFlow<PremiumProduct?>(null)
    val product: StateFlow<PremiumProduct?> = _product.asStateFlow()

    private val _freeAreaIds = MutableStateFlow(emptySet<Int>())
    val freeAreaIds: StateFlow<Set<Int>> = _freeAreaIds.asStateFlow()

    private val _purchaseResults = MutableSharedFlow<PurchaseResult>()
    val purchaseResults: SharedFlow<PurchaseResult> = _purchaseResults.asSharedFlow()

    private val billingClient = BillingClient.newBuilder(context)
        .setListener { billingResult, purchases -> onPurchasesUpdated(billingResult, purchases.orEmpty()) }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .enableAutoServiceReconnection()
        .build()

    fun start() {
        coroutineScope.launch {
            _freeAreaIds.value = areaRepository.getTaggedAreas(FREE_AREAS_TAG).map { it.id }.toSet()
        }

        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (!billingResult.isOk()) return

                    refreshPurchases()
                    loadProduct()
                }

                override fun onBillingServiceDisconnected() = Unit
            }
        )
    }

    fun refreshPurchases() {
        if (billingClient.connectionState == ConnectionState.CONNECTING) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (!billingResult.isOk()) return@queryPurchasesAsync

            onPurchasesFetched(purchases)
        }
    }

    fun restorePurchases(onResult: (RestoreResult) -> Unit) {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            val restoreResult = toRestoreResult(billingResult.responseCode, purchases)

            if (restoreResult != RestoreResult.ERROR) onPurchasesFetched(purchases)

            onResult(restoreResult)
        }
    }

    fun launchPurchase(activity: Activity) {
        val product = _product.value ?: return

        val productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(product.productDetails)
            .setOfferToken(product.offerToken)
            .build()

        val params = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParams))
            .build()

        val billingResult = billingClient.launchBillingFlow(activity, params)

        if (!billingResult.isOk()) onPurchasesUpdated(billingResult, emptyList())
    }

    fun showInAppMessages(activity: Activity) {
        val params = InAppMessageParams.newBuilder()
            .addInAppMessageCategoryToShow(InAppMessageCategoryId.TRANSACTIONAL)
            .build()

        billingClient.showInAppMessages(activity, params) { result ->
            if (result.responseCode == InAppMessageResponseCode.SUBSCRIPTION_STATUS_UPDATED) refreshPurchases()
        }
    }

    private fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>) {
        val purchaseResult = toPurchaseResult(billingResult.responseCode, purchases)

        when (purchaseResult) {
            PurchaseResult.SUCCESS -> onPurchasesFetched(purchases)
            PurchaseResult.ALREADY_OWNED -> refreshPurchases()
            PurchaseResult.CANCELED,
            PurchaseResult.PENDING,
            PurchaseResult.ERROR -> Unit
        }

        coroutineScope.launch { _purchaseResults.emit(purchaseResult) }
    }

    private fun onPurchasesFetched(purchases: List<Purchase>) {
        purchases
            .filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
            .forEach(::acknowledgePurchase)

        updateStatus(purchases.toPremiumStatus())
        isStatusResolved.value = true
    }

    fun loadProduct() {
        if (_product.value != null) return
        if (billingClient.connectionState == ConnectionState.CONNECTING) return

        val product = QueryProductDetailsParams.Product.newBuilder()
            .setProductId(PREMIUM_PRODUCT_ID)
            .setProductType(ProductType.SUBS)
            .build()

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(product))
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, result ->
            if (!billingResult.isOk()) return@queryProductDetailsAsync

            _product.value = result.productDetailsList.firstOrNull()?.toPremiumProduct()
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) {}
    }

    suspend fun getPremiumIntroDecision(): PremiumIntroDecision {
        val isIntroSeen = dataStore.data.first()[PREF_PREMIUM_INTRO_SEEN] ?: false

        val status = withTimeoutOrNull(STATUS_RESOLUTION_TIMEOUT) {
            isStatusResolved.first { it }
            _status.value
        }

        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

        return decidePremiumIntro(
            isIntroSeen = isIntroSeen,
            status = status,
            firstInstallTime = packageInfo.firstInstallTime,
            lastUpdateTime = packageInfo.lastUpdateTime
        )
    }

    fun markPremiumIntroAsSeen() {
        coroutineScope.launch {
            dataStore.edit { it[PREF_PREMIUM_INTRO_SEEN] = true }
        }
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

    companion object {
        private const val FREE_AREAS_TAG = "popular"
        private const val STATUS_RESOLUTION_TIMEOUT = 3_000L
    }
}
