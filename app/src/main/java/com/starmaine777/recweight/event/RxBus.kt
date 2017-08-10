package com.starmaine777.recweight.event

import io.reactivex.subjects.PublishSubject
import io.reactivex.Observable

/**
 * Event用Rxクラス
 * Created by 0025331458 on 2017/07/21.
 */

class RxBus {

    companion object {
        private val subject = PublishSubject.create<Any>()

        fun <T> subscribe(eventType: Class<T>) :Observable<T> = subject.ofType(eventType)

        fun publish(message:Any) {
            subject.onNext(message)
        }

    }
}
