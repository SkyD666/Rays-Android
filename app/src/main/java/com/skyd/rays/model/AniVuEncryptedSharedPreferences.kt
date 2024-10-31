package com.skyd.rays.model

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences.PrefKeyEncryptionScheme
import androidx.security.crypto.EncryptedSharedPreferences.PrefValueEncryptionScheme
import androidx.security.crypto.MasterKey
import javax.crypto.AEADBadTagException

class AniVuEncryptedSharedPreferences(
    private val encryptedSharedPreferences: EncryptedSharedPreferences,
) : SharedPreferences {

    companion object {
        fun create(
            context: Context,
            fileName: String,
            masterKey: MasterKey,
            prefKeyEncryptionScheme: PrefKeyEncryptionScheme,
            prefValueEncryptionScheme: PrefValueEncryptionScheme,
        ): AniVuEncryptedSharedPreferences {
            val creator: () -> EncryptedSharedPreferences = {
                EncryptedSharedPreferences.create(
                    context,
                    fileName,
                    masterKey,
                    prefKeyEncryptionScheme,
                    prefValueEncryptionScheme
                ) as EncryptedSharedPreferences
            }
            return AniVuEncryptedSharedPreferences(
                safeBlock(creator)
                    ?: context.deleteSharedPreferences(fileName).run { creator() }
            )
        }

        private fun <T> safeBlock(block: () -> T): T? {
            return try {
                block()
            } catch (e: AEADBadTagException) {
                e.printStackTrace()
                null
            }
        }
    }

    override fun getAll() = safeBlock { encryptedSharedPreferences.all }.orEmpty().toMutableMap()

    override fun getString(key: String?, defValue: String?): String? = safeBlock {
        encryptedSharedPreferences.getString(key, defValue)
    } ?: defValue

    override fun getStringSet(
        key: String?,
        defValues: MutableSet<String>?,
    ): MutableSet<String>? = safeBlock {
        encryptedSharedPreferences.getStringSet(key, defValues)
    } ?: defValues

    override fun getInt(key: String?, defValue: Int): Int = safeBlock {
        encryptedSharedPreferences.getInt(key, defValue)
    } ?: defValue

    override fun getLong(key: String?, defValue: Long): Long = safeBlock {
        encryptedSharedPreferences.getLong(key, defValue)
    } ?: defValue

    override fun getFloat(key: String?, defValue: Float): Float = safeBlock {
        encryptedSharedPreferences.getFloat(key, defValue)
    } ?: defValue

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = safeBlock {
        encryptedSharedPreferences.getBoolean(key, defValue)
    } ?: defValue

    override fun contains(key: String?): Boolean = safeBlock {
        encryptedSharedPreferences.contains(key)
    } ?: false

    override fun edit(): SharedPreferences.Editor = encryptedSharedPreferences.edit()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) = encryptedSharedPreferences.registerOnSharedPreferenceChangeListener(listener)

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener,
    ) = encryptedSharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
}