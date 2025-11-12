package com.spread.footspa

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class MainApplication : Application() {

    val applicationCoroutineContext = SupervisorJob() + Dispatchers.Default

    val applicationScope = CoroutineScope(applicationCoroutineContext)

    companion object {
        lateinit var instance: MainApplication
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
    }

}

fun <T> Flow<T>.asApplicationStateFlow(initialValue: T): StateFlow<T> = stateIn(
    scope = MainApplication.instance.applicationScope,
    started = SharingStarted.Eagerly,
    initialValue = initialValue
)

fun runOnApplicationScope(
    context: CoroutineContext = MainApplication.instance.applicationCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) = MainApplication.instance.applicationScope.launch(
    context = context,
    block = block
)