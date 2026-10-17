package com.boolder.boolder.data.premium.model

enum class PremiumIntroDecision {
    SHOW,
    MARK_AS_SEEN,
    NOTHING
}

fun decidePremiumIntro(
    isIntroSeen: Boolean,
    status: PremiumStatus?,
    firstInstallTime: Long,
    lastUpdateTime: Long
): PremiumIntroDecision =
    when {
        isIntroSeen -> PremiumIntroDecision.NOTHING
        status == null -> PremiumIntroDecision.NOTHING
        status == PremiumStatus.SUBSCRIBED -> PremiumIntroDecision.MARK_AS_SEEN
        lastUpdateTime <= firstInstallTime -> PremiumIntroDecision.MARK_AS_SEEN
        else -> PremiumIntroDecision.SHOW
    }
