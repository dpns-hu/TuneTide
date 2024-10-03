package com.tunetide.music.util

import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner

fun backButtonActsLikeHomeButton(
    fragment: Fragment,
    lifecycleOwner: LifecycleOwner
) {
    fragment.requireActivity().onBackPressedDispatcher.addCallback(
        lifecycleOwner,
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                fragment.requireActivity().moveTaskToBack(true)
            }
        }
    )
}