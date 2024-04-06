package com.skyd.rays.ui.screen.about.update

import androidx.lifecycle.viewModelScope
import com.skyd.rays.base.mvi.AbstractMviViewModel
import com.skyd.rays.config.GITHUB_REPO
import com.skyd.rays.ext.catchMap
import com.skyd.rays.ext.startWith
import com.skyd.rays.ext.toDateTimeString
import com.skyd.rays.model.respository.UpdateRepository
import com.skyd.rays.util.CommonUtil.getAppVersionCode
import com.skyd.rays.util.CommonUtil.openBrowser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
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
    AbstractMviViewModel<UpdateIntent, UpdateState, UpdateEvent>() {

    override val viewState: StateFlow<UpdateState>

    init {
        val initialVS = UpdateState.initial()

        viewState = merge(
            intentSharedFlow.filter { it is UpdateIntent.CheckUpdate && !it.isRetry }.take(1),
            intentSharedFlow.filter { it is UpdateIntent.CheckUpdate && it.isRetry },
            intentSharedFlow.filterNot { it is UpdateIntent.CheckUpdate }
        )
            .shareWhileSubscribed()
            .toUpdatePartialStateChangeFlow()
            .debugLog("UpdatePartialStateChange")
            .sendSingleEvent()
            .scan(initialVS) { vs, change -> change.reduce(vs) }
            .debugLog("ViewState")
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                initialVS
            )
    }

    private fun Flow<UpdatePartialStateChange>.sendSingleEvent(): Flow<UpdatePartialStateChange> {
        return onEach { change ->
            val event = when (change) {
                is UpdatePartialStateChange.Error -> UpdateEvent.CheckError(change.msg)
                is UpdatePartialStateChange.CheckUpdate.NoUpdate,
                is UpdatePartialStateChange.CheckUpdate.HasUpdate -> UpdateEvent.CheckSuccess()

                else -> return@onEach
            }
            sendEvent(event)
        }
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
                        val publishedAt: String = date?.toDateTimeString() ?: data.publishedAt

                        UpdatePartialStateChange.CheckUpdate.HasUpdate(
                            data.copy(publishedAt = publishedAt)
                        )
                    } else {
                        UpdatePartialStateChange.CheckUpdate.NoUpdate
                    }
                }.startWith(UpdatePartialStateChange.LoadingDialog)
                    .catchMap { UpdatePartialStateChange.Error(it.message.orEmpty()) }
            },

            filterIsInstance<UpdateIntent.Update>().map { intent ->
                openBrowser(intent.url ?: GITHUB_REPO)
                UpdatePartialStateChange.RequestUpdate
            },
        )
    }
}