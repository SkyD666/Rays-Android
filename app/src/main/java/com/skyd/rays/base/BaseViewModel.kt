package com.skyd.rays.base

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
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
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch


abstract class BaseViewModel<UiState : IUiState, UiEvent : IUiEvent, UiIntent : IUiIntent> :
    ViewModel() {

    private val _uiIntentFlow: MutableSharedFlow<UiIntent> = MutableSharedFlow()

    protected abstract fun initUiState(): UiState

    /**
     * 若 IUIChange 是 Event，则发送出去，不纳入 UiState
     */
    private fun Flow<IUIChange>.sendEvent(): Flow<UiState> = transform { value ->
        Log.e("TAG", "sendEvent: $value")
        val (state, event) = value.checkStateOrEvent()
        if (event != null) {
            uiEventChannel.send(event)      // 此时 state 为 null
        }
        state ?: return@transform
        return@transform emit(state)
    }/*mapNotNull {
        val (state, event) = it.checkStateOrEvent()
        if (event != null) {
            uiEventChannel.send(event)      // 此时 state 为 null
        }
        state
    }*/

    protected abstract fun IUIChange.checkStateOrEvent(): Pair<UiState?, UiEvent?>

    private val uiEventChannel: Channel<UiEvent> = Channel()
    val uiEventFlow = uiEventChannel.receiveAsFlow()

    private val _loadUiIntentFlow: MutableSharedFlow<LoadUiIntent> = MutableSharedFlow()
    val loadUiIntentFlow: SharedFlow<LoadUiIntent> = _loadUiIntentFlow

    fun sendUiIntent(uiIntent: UiIntent) {
        viewModelScope.launch(Dispatchers.IO) {
            if (uiIntent.showLoading) {
                sendLoadUiIntent(LoadUiIntent.Loading(true))
            }
            _uiIntentFlow.emit(uiIntent)
        }
    }

    protected abstract fun Flow<UiIntent>.handleIntent(): Flow<IUIChange>

    protected open fun Flow<UiIntent>.handleIntent2(): Flow<PartialChange<UiState>> = map {
        object : PartialChange<UiState> {
            override fun reduce(oldState: UiState): UiState {
                return oldState
            }
        }
    }

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
        crossinline transform: suspend CoroutineScope.(value: T) -> Flow<IUIChange>
    ): Flow<IUIChange> = filterIsInstance<T>().flatMapConcat { viewModelScope.transform(it) }

    /**
     * 若 T 是给定的类型则执行...
     */
    protected inline fun <reified T> Flow<*>.doIsInstance2(
        crossinline transform: suspend CoroutineScope.(value: T) -> Flow<PartialChange<UiState>>
    ): Flow<PartialChange<UiState>> =
        filterIsInstance<T>().flatMapConcat { viewModelScope.transform(it) }

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
     * Flow<BaseData<T>> 转为 Flow<PartialChange>
     */
    protected fun <T> Flow<BaseData<T>>.mapToPartialChange(
        onError: (value: BaseData<T>) -> PartialChange<UiState> = { error(it.msg.toString()) },
        transform: (value: T) -> PartialChange<UiState>,
    ): Flow<PartialChange<UiState>> = map {
        when (it.state) {
            ReqState.Success -> {
                val data = it.data
                if (data != null) {
                    transform(data)
                } else error(it.msg.toString())
            }

            else -> onError(it)
        }
    }

    // 从 Flow<T> 变换为 Flow<R>
    protected fun <T, R> Flow<T>.scan(
        initial: R, // 初始值
        operation: suspend (accumulator: R, value: T) -> R // 累加算法
    ): Flow<R> = runningFold(initial, operation)

    private fun <T, R> Flow<T>.runningFold(
        initial: R,
        operation: suspend (accumulator: R, value: T) -> R
    ): Flow<R> = flow {
        // 累加器
        var accumulator: R = initial
        emit(accumulator)
        collect { value ->
            // 进行累加
            accumulator = operation(accumulator, value)
            // 向下游发射累加值
            emit(accumulator)
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

    val uiStateFlow2: StateFlow<UiState> = _uiIntentFlow
        .handleIntent2()
        .scan(initUiState()) { oldState, partialChange -> partialChange.reduce(oldState) }
        .sendEvent()
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, initUiState())
}