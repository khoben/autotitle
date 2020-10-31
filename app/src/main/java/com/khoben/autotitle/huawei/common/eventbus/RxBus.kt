package com.khoben.autotitle.huawei.common.eventbus

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.processors.PublishProcessor

/**
 * [EventBus] implementation backed by a [PublishProcessor]
 */
class RxBus : EventBus {
    private val publishProcessor = PublishProcessor.create<Any>()

    override fun postEvent(event: Any) {
        publishProcessor.onNext(event)
    }

    override fun observeEvents(): Flowable<Any> {
        return publishProcessor.serialize()
    }

    override fun observeEventsOnUi(): Flowable<Any> {
        return observeEvents()
            .observeOn(AndroidSchedulers.mainThread())
    }
}