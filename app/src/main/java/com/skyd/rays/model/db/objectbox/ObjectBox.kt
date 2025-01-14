package com.skyd.rays.model.db.objectbox

import android.content.Context
import com.skyd.rays.model.db.objectbox.entity.MyObjectBox
import io.objectbox.BoxStore

object ObjectBox {
    @Volatile
    private var instance: BoxStore? = null

    fun getInstance(context: Context): BoxStore {
        return instance ?: synchronized(this) {
            instance ?: MyObjectBox.builder()
                .androidContext(context)
                .build()
                .apply { instance = this }
        }
    }
}