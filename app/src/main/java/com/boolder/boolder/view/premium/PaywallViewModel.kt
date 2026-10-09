package com.boolder.boolder.view.premium

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.boolder.boolder.data.premium.repository.PremiumRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PaywallViewModel(
    premiumRepository: PremiumRepository
) : ViewModel() {

    val premiumPrice = premiumRepository.product
        .map { it?.formattedPrice }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = premiumRepository.product.value?.formattedPrice
        )
}
