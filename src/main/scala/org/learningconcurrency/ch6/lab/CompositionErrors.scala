package org.learningconcurrency.ch6.lab

import rx.lang.scala._

object CompositionErrors extends App {
    /* TODO: I DO NOT HAVE A CLEAR UNDERSTANDING OF WHAT'S GOING ON HERE.
     * Evidently, an Obervable is just generating an empty Subscription upon
     * which any number of onNext calls can be made, each of which is supplying
     * their own String content.
     */

    val status = Observable[String] { sub =>
        sub.onNext("ok")
        sub.onNext("still ok")
        //sub.onNext(2) // error, String type has been established
        sub.onError(new Exception("very bad")) // generating an error
        sub.onNext("here again") // Doesn't fire, onError has terminated Observable
    }

    val fixedStatus = status.onErrorReturn(e => e.getMessage)
    fixedStatus.subscribe(println(_)) // Maybe onErrorResume automatically gets the next call? Looks like that might be what's going on...

    val continuedStatus = status.onErrorResumeNext(e => 
        Observable.items("better", "much better"))
        continuedStatus.subscribe(println(_))
}