package com.example.finapay.ui.animation

import android.animation.ObjectAnimator
import android.view.View

class Animation {

    fun animationslideright(view: View, duration: Long = 500) {
        val slideInFromLeft = ObjectAnimator.ofFloat(view, "translationX", -1000f, 0f)
        slideInFromLeft.duration = duration
        slideInFromLeft.start()
    }

    fun animationslideleft(view: View, duration: Long = 500) {
        val slideInFromRight = ObjectAnimator.ofFloat(view, "translationX", 1000f, 0f)
        slideInFromRight.duration = duration
        slideInFromRight.start()
    }

    fun animationslidebottom(view: View, duration: Long = 500) {
        val slideInFromBottom = ObjectAnimator.ofFloat(view, "translationY", 1000f, 0f)
        slideInFromBottom.duration = duration
        slideInFromBottom.start()
    }

    fun animationPopup(view: View, duration: Long = 500) {
        view.alpha = 0f
        view.scaleX = 0f
        view.scaleY = 0f

        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .start()
    }

    fun animationFadeIn(view: View, duration: Long = 500) {
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
            .alpha(1f)
            .setDuration(duration)
            .start()
    }
}
