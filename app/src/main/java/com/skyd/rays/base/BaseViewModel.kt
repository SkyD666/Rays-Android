package com.skyd.rays.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


abstract class BaseViewModel<UiState : IUiState, UiEvent : IUiEvent, UiIntent : IUiIntent> :
    ViewModel() {

    private val _uiIntentFlow: MutableSharedFlow<UiIntent> = MutableSharedFlow()

    protected abstract fun initUiState(): UiState

    /**
     * 若 IUIChange 是 Event，则发送出去，不纳入 UiState
     */
    private fun Flow<IUIChange>.sendEvent(): Flow<UiState> = mapNotNull {
        val (state, event) = it.checkStateOrEvent()
        if (event != null) {
            uiEventChannel.send(event)      // 此时 state 为 null
        }
        state
    }

    protected abstract fun IUIChange.checkStateOrEvent(): Pair<UiState?, UiEvent?>

    private val uiEventChannel: Channel<UiEvent> = Channel()
    val uiEventFlow = uiEventChannel.receiveAsFlow()

    private val _loadUiIntentFlow: MutableSharedFlow<LoadUiIntent> = MutableSharedFlow()
    val loadUiIntentFlow: SharedFlow<LoadUiIntent> = _loadUiIntentFlow

    fun sendUiIntent(uiIntent: UiIntent) {
        viewModelScope.launch {
            if (uiIntent.showLoading) {
                sendLoadUiIntent(LoadUiIntent.Loading(true))
            }
            _uiIntentFlow.emit(uiIntent)
        }
    }

    protected abstract fun Flow<UiIntent>.handleIntent(): Flow<IUIChange>

    /**
     * 发送当前加载状态：Loading、Error、Normal
     */
    protected fun sendLoadUiIntent(loadUiIntent: LoadUiIntent) {
        viewModelScope.launch {
            _loadUiIntentFlow.emit(loadUiIntent)
        }
    }

    /**
     * 不需要访问 repository 的空 Flow
     */
    protected fun emptyFlow(): Flow<BaseData<Unit>> = flowOf(BaseData<Unit>().apply {
        state = ReqState.Success
        data = Unit
    })

    /**
     * 不需要访问 repository 的简单的 Flow
     */
    protected fun <T> simpleFlow(action: suspend FlowCollector<BaseData<T>>.() -> T): Flow<BaseData<T>> =
        flow {
            emit(BaseData<T>().apply {
                state = ReqState.Success
                data = action.invoke(this@flow)
            })
        }

    /**
     * 若 T 是给定的类型则执行...
     */
    protected inline fun <reified T> Flow<*>.doIsInstance(
        crossinline transform: suspend (value: T) -> Flow<IUIChange>
    ): Flow<IUIChange> = filterIsInstance<T>().flatMapConcat { transform(it) }

    /**
     * Flow<BaseData<T>> 转为 Flow<IUIChange>
     */
    protected fun <T> Flow<BaseData<T>>.mapToUIChange(
        onError: (UiState).(value: BaseData<T>) -> IUIChange = { error(it.msg.toString()) },
        transform: (UiState).(value: T) -> IUIChange,
    ): Flow<IUIChange> = map {
        when (it.state) {
            ReqState.Success -> {
                val data = it.data
                if (data != null) {
                    uiStateFlow.value.transform(data)
                } else error(it.msg.toString())
            }

            else -> uiStateFlow.value.onError(it)
        }
    }

    /**
     * 在每个 UiIntent 结束调用
     */
    protected fun <T> Flow<T>.defaultFinally(): Flow<T> = onCompletion {
        sendLoadUiIntent(LoadUiIntent.Loading(false))
    }.catch {
        it.printStackTrace()
        sendLoadUiIntent(LoadUiIntent.Loading(false))
        sendLoadUiIntent(LoadUiIntent.Error(it.message.toString()))
    }

    val uiStateFlow: StateFlow<UiState> = _uiIntentFlow
        .handleIntent()
        .sendEvent()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initUiState())
}