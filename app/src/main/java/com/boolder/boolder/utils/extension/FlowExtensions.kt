package com.boolder.boolder.utils.extension

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

inline fun <T> Flow<T>.launchAndCollectIn(
    owner: LifecycleOwner,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline collector: (T) -> Unit
) =
    owner.lifecycleScope.launch {
        owner.repeatOnLifecycle(minActiveState) {
            collect { collector(it) }
        }
    }
