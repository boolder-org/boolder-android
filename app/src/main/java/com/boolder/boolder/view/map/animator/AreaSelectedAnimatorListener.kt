package com.boolder.boolder.view.map.animator

import android.animation.Animator

fun animationEndListener(
    onAnimationEnd: () -> Unit
) = object : Animator.AnimatorListener {

    override fun onAnimationStart(animator: Animator) {}
    override fun onAnimationCancel(animator: Animator) {}
    override fun onAnimationRepeat(animator: Animator) {}

    override fun onAnimationEnd(animator: Animator) {
        onAnimationEnd()
    }
}
