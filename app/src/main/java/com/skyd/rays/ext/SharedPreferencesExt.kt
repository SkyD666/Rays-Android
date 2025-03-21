package com.skyd.rays.ext

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.skyd.rays.appContext
import com.skyd.rays.model.RaysEncryptedSharedPreferences

fun secretSharedPreferences(name: String = "Secret"): RaysEncryptedSharedPreferences {
    val masterKey = MasterKey.Builder(appContext)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return RaysEncryptedSharedPreferences.create(
        appContext,
        name,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}
