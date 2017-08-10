package org.content.diff

import java.util.Base64

import akka.Done
import akka.actor.Actor
import org.content.diff.ContentDiffMessages.{DiffRequest, LeftRequest, RightRequest}
import org.content.diff.ContentProcessor.{Length, Offset}

import scala.concurrent.Promise
import scala.util.{Failure, Try}

/**
  * Contains utility methods and classes used only in unit tests.
  */
object TestHelper {

  /**
    * Test actor that completes Promise with success only if it receives [[LeftRequest]] with ID '1' and content '"AAAAAA=='.
    * If it receives any other message completes Promise with failure.
    *
    * @param p Promise to complete.
    */
  class LeftActor(p: Promise[Done]) extends Actor {
    override def receive: Receive = {
      case LeftRequest(_, "1", Some("AAAAAA==")) =>
        p complete Try(Done)
      case _ =>
        p complete Failure(new Exception("left failure"))
    }
  }

  /**
    * Test actor that completes Promise with success only if it receives [[RightRequest]] with ID '2' and content 'AQABAQ=='.
    * If it receives any other message completes Promise with failure.
    *
    * @param p Promise to complete.
    */
  class RightActor(p: Promise[Done]) extends Actor {
    override def receive: Receive = {
      case RightRequest(_, "2", Some("AQABAQ==")) =>
        p complete Try(Done)
      case _ =>
        p complete Failure(new Exception("right failure"))
    }
  }

  /**
    * Test actor that completes Promise with success only if it receives [[DiffRequest]] with ID '3'.
    * If it receives any other message completes Promise with failure.
    *
    * @param p Promise to complete.
    */
  class DiffActor(p: Promise[Done]) extends Actor {
    override def receive: Receive = {
      case DiffRequest(_, "3") =>
        p complete Try(Done)
      case _ =>
        p complete Failure(new Exception("diff failure"))
    }
  }

  /**
    * Transforms right content into left content using given list of differences.
    *
    * If list of differences is correct then transformed right content should be equal to the left content.
    *
    * @param leftContent  Left content.
    * @param rightContent Right content to transform.
    * @param diffs        List of differences.
    * @return Transformed right content.
    */
  def transform(leftContent: String, rightContent: String, diffs: List[(Offset, Length)]): String = {
    val decoded: Array[Byte] = Base64.getDecoder.decode(leftContent)
    val transformed: Array[Byte] = Base64.getDecoder.decode(rightContent)

    for {
      (offset, length) <- diffs
      i <- 0 until length
    } transformed(offset + i) = decoded(offset + i)

    Base64.getEncoder.encodeToString(transformed)
  }
}
