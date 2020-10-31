package com.khoben.autotitle.huawei.common.eventbus

import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.kotlin.ofType


/**
 * Events are posted to common bus and all active subscribers are notified of changes. Threading and
 * backpresssure can be handled by subscribers.
 */
interface EventBus {
    /**
     * Post an event to bus.
     */
    fun postEvent(event: Any)

    /**
     * @return A [Flowable] that will emit event for every [postEvent] call until subscriber is
     * unsubscribed.
     *
     * Note: This does not emit event on any particular [Scheduler]
     */
    fun observeEvents(): Flowable<Any>

    /**
     * Same as [observeEvent] but emits event on the UI thread.
     */
    fun observeEventsOnUi(): Flowable<Any>
}

/**
 * Observer extension that will only emit event of type [T], filtering all other events.
 */
inline fun <reified T : Any> EventBus.observeEvent(): Flowable<T> {
    return observeEvents().ofType()
}

/**
 * Same as [observeEvent] but emits on UI thread.
 */
inline fun <reified T : Any> EventBus.observeEventOnUi(): Flowable<T> {
    return observeEventsOnUi().ofType()
}