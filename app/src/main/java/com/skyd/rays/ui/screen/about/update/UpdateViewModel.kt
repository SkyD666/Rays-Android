package com.skyd.rays.ui.screen.about.update

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.base.mvi.MviSingleEvent
import com.skyd.rays.config.GITHUB_REPO
import com.skyd.rays.ext.startWith
import com.skyd.rays.model.respository.UpdateRepository
import com.skyd.rays.util.CommonUtil.getAppVersionCode
import com.skyd.rays.util.CommonUtil.openBrowser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import okhttp3.internal.toLongOrDefault
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(private var updateRepo: UpdateRepository) :
    AbstractMviViewModel<UpdateIntent, UpdateState, MviSingleEvent>() {

    override val viewState: StateFlow<UpdateState>

    init {
        val initialVS = UpdateState.initial()

        viewState = merge(
            intentSharedFlow.filterIsInstance<UpdateIntent.CheckUpdate>().take(1),
            intentSharedFlow.filterNot { it is UpdateIntent.CheckUpdate }
        )
            .shareWhileSubscribed()
            .toUpdatePartialStateChangeFlow()
            .debugLog("UpdatePartialStateChange")
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun SharedFlow<UpdateIntent>.toUpdatePartialStateChangeFlow(): Flow<UpdatePartialStateChange> {
        return merge(
            filterIsInstance<UpdateIntent.CheckUpdate>().flatMapConcat {
                updateRepo.checkUpdate().map { data ->
                    if (getAppVersionCode() < data.tagName.toLongOrDefault(0L)) {
                        val date = SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss'Z'",
                            Locale.getDefault()
                        ).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }.parse(data.publishedAt)
                        val publishedAt: String = if (date != null) {
                            SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
                        } else {
                            data.publishedAt
                        }

                        UpdatePartialStateChange.CheckUpdate.HasUpdate(
                            data.copy(publishedAt = publishedAt)
                        )
                    } else {
                        UpdatePartialStateChange.CheckUpdate.NoUpdate
                    }
                }.startWith(UpdatePartialStateChange.LoadingDialog)
            },

            filterIsInstance<UpdateIntent.Update>().map { intent ->
                openBrowser(intent.url ?: GITHUB_REPO)
                UpdatePartialStateChange.RequestUpdate
            },
        )
    }
}