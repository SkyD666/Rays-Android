package com.skyd.rays.ui.activity

import android.graphics.BitmapFactory
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.viewModelScope
import androidx.palette.graphics.Palette
import com.skyd.rays.appContext
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.base.mvi.MviViewState
import com.skyd.rays.ext.dataStore
import com.skyd.rays.ext.getOrDefault
import com.skyd.rays.model.preference.theme.CustomPrimaryColorPreference
import com.skyd.rays.model.preference.theme.StickerColorThemePreference
import com.skyd.rays.model.preference.theme.ThemeNamePreference
import com.skyd.rays.util.stickerUuidToFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.lang.Integer.max
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor() :
    AbstractMviViewModel<MainIntent, MviViewState, MviSingleEvent>() {


    override val viewState: StateFlow<MviViewState>

    init {
        viewState = merge(
            intentSharedFlow.filterIsInstance<MainIntent.Init>().take(1),
            intentSharedFlow.filterNot { it is MainIntent.Init }
        )
            .shareWhileSubscribed()
            .process()
            .map { object : MviViewState {} }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                object : MviViewState {},
            )

        viewModelScope.launch {
            viewState.collect()
        }
    }

    private fun SharedFlow<MainIntent>.process(): Flow<Any> {
        return merge(
            filterIsInstance<MainIntent.Init>(),

            filterIsInstance<MainIntent.UpdateThemeColor>().map { intent ->
                if (intent.stickerUuid.isNotBlank() &&
                    appContext.dataStore.getOrDefault(StickerColorThemePreference)
                ) {
                    setPrimaryColor(uuid = intent.stickerUuid)
                }
            },
        )
    }

    private suspend fun setPrimaryColor(uuid: String) = runCatching {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        val stickerFilePath = stickerUuidToFile(uuid).path
        val l = max(options.outHeight, options.outWidth)
        options.apply {
            inSampleSize = l / 20
            inJustDecodeBounds = false
        }
        val bitmap = BitmapFactory.decodeFile(stickerFilePath, options)
        val swatch = suspendCancellableCoroutine { cont ->
            Palette.from(bitmap).generate {
                cont.resume(it?.dominantSwatch, onCancellation = null)
            }
        }
        bitmap.recycle()
        if (swatch == null) {
            return@runCatching
        }
        appContext.dataStore.edit { pref ->
            pref[ThemeNamePreference.key] = ThemeNamePreference.CUSTOM_THEME_NAME
            pref[CustomPrimaryColorPreference.key] = Integer.toHexString(swatch.rgb)
        }
    }
}