package org.learningconcurrency.ch4.lab

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.io.Source
  
object FuturesDataType extends App {
    
  // Trivial, little value here

  val buildFile: Future[String] = Future {
    val f = Source.fromFile("build.sbt")
    try f.getLines.mkString("\n") finally f.close()
  }

  log(s"started reading build file asynchronously")
  log(s"status: ${buildFile.isCompleted}")
  
  Thread.sleep(250)
  
  log(s"status: ${buildFile.isCompleted}")
  log(s"buildFile.value:\n${buildFile.value}")
  
  def log = (s: String) => println(s)

}