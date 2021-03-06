package org.learningconcurrency.ch4.lab

import scala.concurrent._
import ExecutionContext.Implicits.global

object Futures_lab_2 extends App {

    def sleep(duration: Long) { blocking{ Thread.sleep(duration) } }

    val s = "Hello"
    
    def computation = {
        println("computation (i.e. Future apply() body) gets invoked immediately upon parsing definition.")
        sleep(4000)
        println("computation is done!")
        s + " future!"
    }
    
    // f is really a DefaultPromise
    val f: Future[String] = Future {
        /* The body gets invoked immediately upon parsing definition */
        computation
    }
    
    // onSuccess is evidently heading for deprecation
    f onSuccess { // internal Success extends Try[T]
        case msg => println("onSuccess: " + msg)
    }
    
    /* foreach is supposedly the norm way for completion
     * (when not otherwise composed).
     */
    f foreach { // internal Success extends Try[T]
        case msg => println("foreach: " + msg)
    }
    
    println("TCB")
    
    sleep(10000)

}