package com.skyd.rays.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
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
            sendLoadUiIntent(LoadUiIntent.Loading(true))
            _uiIntentFlow.emit(uiIntent)
        }
    }

    protected abstract fun Flow<UiIntent>.handleIntent(): Flow<IUIChange>

    /**
     * 发送当前加载状态：Loading、Error、Normal
     */
    private fun sendLoadUiIntent(loadUiIntent: LoadUiIntent) {
        viewModelScope.launch {
            _loadUiIntentFlow.emit(loadUiIntent)
        }
    }

    /**
     * 若 T 是给定的类型则执行...
     */
    inline fun <reified T> Flow<*>.doIsInstance(
        crossinline transform: suspend (value: T) -> Flow<IUIChange>
    ): Flow<IUIChange> = filterIsInstance<T>().flatMapConcat { transform(it) }

    /**
     * Flow<BaseData<T>> 转为 Flow<IUIChange>
     */
    protected fun <T> Flow<BaseData<T>>.mapToUIChange(
        transform: (UiState).(value: T) -> IUIChange
    ): Flow<IUIChange> = map {
        when (it.state) {
            ReqState.Success -> {
                val data = it.data
                if (data != null) {
                    sendLoadUiIntent(LoadUiIntent.ShowMainView)
                    uiStateFlow.value.transform(data)
                } else error(it.msg.toString())
            }
            else -> error(it.msg.toString())
        }
    }

    /**
     * 在每个 UiIntent 结束调用
     */
    protected fun <T> Flow<T>.defaultFinally(): Flow<T> = onCompletion {
        sendLoadUiIntent(LoadUiIntent.Loading(false))
    }.catch {
        it.printStackTrace()
        sendLoadUiIntent(LoadUiIntent.Error(it.message.toString()))
    }

    val uiStateFlow: StateFlow<UiState> = _uiIntentFlow
        .handleIntent()
        .sendEvent()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initUiState())
}