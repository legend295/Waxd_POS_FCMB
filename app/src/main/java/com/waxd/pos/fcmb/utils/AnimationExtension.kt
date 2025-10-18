package com.waxd.pos.fcmb.utils

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import kotlin.apply

fun View.animateVisibility(
    isVisible: Boolean,
    duration: Long = 300L,
    onEnd: (() -> Unit)? = null
) {
    animate().cancel()
    if (isVisible) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction { onEnd?.invoke() }
            .start()
    } else {
        this.animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                this.visibility = View.GONE
                onEnd?.invoke()
            }
            .start()
    }
}

fun TextView.animateTextChange(
    newText: CharSequence,
    duration: Long = 300L,
    onEnd: (() -> Unit)? = null
) {
    if (text == newText) {
        // Skip if the new text is the same
        onEnd?.invoke()
        return
    }

    animate().cancel()

    animate()
        .alpha(0f)
        .setDuration(duration / 2)
        .withEndAction {
            text = newText
            animate()
                .alpha(1f)
                .setDuration(duration / 2)
                .withEndAction { onEnd?.invoke() }
                .start()
        }
        .start()
}


fun View.decelerateAnimation(duration: Long = 300) {
    DecelerateInterpolator().apply {
        this@decelerateAnimation.scaleX = 0f
        this@decelerateAnimation.scaleY = 0f
        this@decelerateAnimation.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setInterpolator(this)
            .setDuration(duration)
            .start()
    }
}

fun View.bounceAnimation(duration: Long = 300) {
    BounceInterpolator().apply {
        this@bounceAnimation.translationY = 100f
        this@bounceAnimation.animate()
            .translationY(0f)
            .setInterpolator(this)
            .setDuration(300)
            .start()
    }

}

fun View.accelerateAnimation(duration: Long = 1000) {
    AccelerateInterpolator().apply {
        this@accelerateAnimation.animate().translationX(100f)
            .setInterpolator(this)
            .setDuration(duration)
            .start()
    }

}


