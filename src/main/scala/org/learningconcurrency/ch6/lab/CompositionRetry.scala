package org.learningconcurrency.ch6.lab

import rx.lang.scala._
import scala.concurrent.duration._
import scala.io.Source
import Observable._
    
// There is always one more bug.
object CompositionRetry extends App {
    println("CompositionRetry App TOP")

    def randomQuote = Observable.apply[String] { obs => 
        {
            println("In def randomQuote")
            val url = "http://www.iheartquotes.com/api/v1/random?" +
                "show_permalink=false&show_source=false"
            obs.onNext(Source.fromURL(url).getLines.mkString)
            obs.onCompleted()
            Subscription()
        }
    }

    def errorMessage = {
        println("In def errorMessage, about to call items Retrying+Exception")
        /*
         * def error[T](exception: Throwable): Observable[T]
         * Returns an Observable that invokes an rx.lang.scala.Observer's 
         * onError method when the Observer subscribes to it.
         * rw: Only I don't see onError getting triggered in this scenario.
         * instead, I believe retry intercepts the error and only passes
         * it on to onError if none of the retries are successfully complete.
         */
        val res = items("quote too long, retrying...") ++ error(new Exception)
        res
    }
    
    def shortQuote = {
        println("In shortQuote TOP")
        for {
            txt     <- randomQuote
            message <- if (txt.length < 100) items(txt) else errorMessage
        } yield {
            println("~~In yield, message: " + message)
            message
        }
    }

    // This attempts to get just one quote, with up to 5 retries.
    shortQuote.retry(5).subscribe(
        /*
         * This first param to subscribe, coming after retry, somehow
         * knows when there's an error and to attempt a retry. It's
         * not the middle param that catches each Exception unless all
         * retries are exhausted.
         * TODO: I can't figure out how it knows to retry. There isn't
         * any explicit appearance of the Exception that I can detect.
         */
        q => println("In subscribe, quote: " + q),
        e => println(s"In subscribe, all quotes too long - $e"), // This only manifests if all retries fail
        () => println("In subscribe, done!")
    )
    
    println("CompositionRetry App BOTTOM, before sleep")
    Thread.sleep(3500)
    println("END")
}


