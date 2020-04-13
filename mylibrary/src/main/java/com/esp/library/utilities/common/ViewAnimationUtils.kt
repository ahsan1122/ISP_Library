package com.esp.library.utilities.common

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView

class ViewAnimationUtils {


    companion object {

        fun expand(view: View) {
            view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val initialHeight = 0
            val targetHeight = view.measuredHeight
            view.layoutParams.height = 0
            animateView(view, initialHeight, targetHeight)
        }

        fun collapse(view: View, viewSectionLabelsName: View) {
            view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            val initialHeight = view.measuredHeight
            val targetHeight = viewSectionLabelsName.measuredHeight
            view.layoutParams.height = initialHeight

            animateView(view, initialHeight, targetHeight)
        }

        private fun animateView(v: View, initialHeight: Int, targetHeight: Int) {
            val valueAnimator = ValueAnimator.ofInt(initialHeight, targetHeight)
            valueAnimator.addUpdateListener { animation ->
                v.layoutParams.height = animation.animatedValue as Int
                v.requestLayout()
            }
            valueAnimator.addListener(object : Animator.AnimatorListener {
                override fun onAnimationEnd(animation: Animator) {
                    v.layoutParams.height = targetHeight
                }

                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
            valueAnimator.duration = 300
            valueAnimator.interpolator = DecelerateInterpolator()
            valueAnimator.start()
        }

    }

}