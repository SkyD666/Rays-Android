package com.skyd.rays.ui.component

import android.widget.Toast
import com.skyd.rays.appContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private var scope = CoroutineScope(Dispatchers.Main.immediate)

fun CharSequence.showToast(duration: Int = Toast.LENGTH_SHORT) {
    scope.launch {
        val toast = Toast.makeText(appContext, this@showToast, duration)
        toast.duration = duration
        toast.show()
    }
}