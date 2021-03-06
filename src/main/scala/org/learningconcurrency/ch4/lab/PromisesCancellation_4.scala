package org.learningconcurrency.ch4.lab

import scala.concurrent._
import ExecutionContext.Implicits.global

object PromisesCancellation_4 extends App {

   /*
     * We are passing an anonymous function to runContext. This anonymous 
     * function contains in its body the anynchronous computation that will 
     * return a 'value'. Note the familiar pattern in which a val is used to 
     * hold a reference to a working Future returned from a factory (context) 
     * that also provides reference to a paired Promise (i.e. tuple) that 
     * provides the two way communication between the asynchronous computation 
     * and the client for cancellation. This means that the client will receive 
     * notice of the completed async computation, and the working Future (i.e. 
     * anynchronous computation) will recieve notice if client wants it to 
     * cancel the async computation.
     * 
     * This anonymous function passed to runContext will be referred to as
     * 'body' there (because the body is in fact concrete executable code
     * whereas the input is a placeholder (type) of an arg that 
     * will be passed to 'body' inside runContext. When invoked, this input 
     * to 'body', cancel_F, will be substituted with cancel_P.future and 
     * referred to as cancel_F. cancel_P is a Promise[Unit]. cancel_P.future 
     * is the context for cancellation. The async computation periodically 
     * checks cancel_F to see if it has been completed by the client calling 
     * tryComplete on cancel_P.
     * 
     * cancel_P and cancel_F both refer to the same Promise/Future duality.
     * The client uses the cancel_P Promise to cancel the async computation, 
     * and the async computation (working Future) checks cancel_F to see
     * if it should throw a CancellationException.
     */
    val (cancel_P, value) = runContext { cancel_F => 
        { ///// body that will be ansychronous computation in runContext
            val result = for {
                 i <- 1 to 5
            } yield {
                 Thread.sleep(500)
                 println(s"$i: working")
                 // Checking to see if its been cancelled. Using exception as off switch, i.e. trigger failure, since there are only two ways to complete a Future (Success or Failure).
                 if (cancel_F.isCompleted) throw new CancellationException
                 i
            }
            result.toList // List[Int] returned wrapped in Future by runContext to appear (above) as 'value'.
        } ///// end body
    }
    
    /* runContext is renamed from cancellable in the book (p. 126/149).
     * The body of the 'body' param is the asynchronous computation. It will
     * commence when 'body' function is called on cancel_P.future.
     */
    def runContext[T](body: Future[Unit] => T): (Promise[Unit], Future[T]) = {
        val cancel_P = Promise[Unit]
        val asyncCompFut = Future {
            val returnValue = body(cancel_P.future) // body's async computation is triggered
            // See the book's explanation for why this check is need to avoid race condition.
            if (!cancel_P.tryFailure(new Exception)) throw new CancellationException
            returnValue // completed type T
        }
        (cancel_P, asyncCompFut)
    }

    value foreach { case v => println("value: " + v) }
    value.failed foreach { case e => println("error: " + e) }
    
    // Simulated cancellation period threshhold
    Thread.sleep(scala.util.Random.nextInt(7000))
    cancel_P trySuccess () // 'cancel' Promise becomes completed

    Thread.sleep(500)
    println("JVM leavin da haus")
}

