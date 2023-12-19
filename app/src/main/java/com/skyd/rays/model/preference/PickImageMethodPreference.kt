package com.skyd.rays.model.preference

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.put
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object PickImageMethodPreference : BasePreference<String> {
    val methodList = arrayOf(
        "PickVisualMedia",
        "OpenDocument",
        "GetContent",
    )

    private const val PICK_IMAGE_METHOD = "pickImageMethod"
    override val default = methodList[0]

    val key = stringPreferencesKey(PICK_IMAGE_METHOD)

    fun put(context: Context, scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            context.dataStore.put(key, value)
        }
    }

    override fun fromPreferences(preferences: Preferences): String = preferences[key] ?: default
}