package com.boolder.boolder.view.premium

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.premium.model.PurchaseResult
import com.boolder.boolder.data.premium.model.RestoreResult
import com.boolder.boolder.data.premium.repository.PremiumRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PaywallViewModel(
    private val premiumRepository: PremiumRepository
) : ViewModel() {

    val premiumPrice = premiumRepository.product
        .map { it?.formattedPrice }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = premiumRepository.product.value?.formattedPrice
        )

    val premiumStatus = premiumRepository.status

    private val _isPurchaseInProgress = MutableStateFlow(false)
    val isPurchaseInProgress = _isPurchaseInProgress.asStateFlow()

    private val _isRestoreInProgress = MutableStateFlow(false)

    private val _isPurchaseSucceeded = MutableStateFlow(false)
    val isPurchaseSucceeded = _isPurchaseSucceeded.asStateFlow()

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            premiumRepository.purchaseResults.collect(::onPurchaseResult)
        }
    }

    fun onSubscribeClicked(activity: Activity) {
        if (_isPurchaseInProgress.value || _isRestoreInProgress.value) return

        _isPurchaseInProgress.value = true

        premiumRepository.launchPurchase(activity)
    }

    fun onRestoreClicked() {
        if (_isPurchaseInProgress.value || _isRestoreInProgress.value) return

        _isRestoreInProgress.value = true

        premiumRepository.restorePurchases { restoreResult ->
            _isRestoreInProgress.value = false

            viewModelScope.launch { _events.emit(restoreResult.toEvent()) }
        }
    }

    fun onIntroShown() {
        premiumRepository.markPremiumIntroAsSeen()
    }

    fun onSuccessDismissed() {
        _isPurchaseSucceeded.value = false
    }

    private suspend fun onPurchaseResult(purchaseResult: PurchaseResult) {
        _isPurchaseInProgress.value = false

        when (purchaseResult) {
            PurchaseResult.SUCCESS -> _isPurchaseSucceeded.value = true
            PurchaseResult.ALREADY_OWNED -> _events.emit(Event.RestoreSucceeded)
            PurchaseResult.PENDING -> _events.emit(Event.PurchasePending)
            PurchaseResult.ERROR -> _events.emit(Event.PurchaseFailed)
            PurchaseResult.CANCELED -> Unit
        }
    }

    private fun RestoreResult.toEvent(): Event =
        when (this) {
            RestoreResult.RESTORED -> Event.RestoreSucceeded
            RestoreResult.NOT_FOUND -> Event.RestoreNotFound
            RestoreResult.ERROR -> Event.RestoreFailed
        }

    sealed interface Event {
        data object PurchasePending : Event
        data object PurchaseFailed : Event
        data object RestoreSucceeded : Event
        data object RestoreNotFound : Event
        data object RestoreFailed : Event
    }
}
