/*                     __                                               *\
**     ________ ___   / /  ___     content-diff                         **
**    / __/ __// _ | / /  / _ |    (c) 2017                             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.content.diff

import java.io.File
import java.util.Base64

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.config.ConfigFactory
import org.content.diff.ContentDiffMessages._
import org.content.diff.ContentProcessor._
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult

import scala.collection.mutable.ListBuffer
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

/**
  * Represent difference between left and right content with same ID.
  *
  * Following objects/classes use this trait:
  * [[Equal]]
  * [[SizeDoNotMatch]]
  * [[ContentDoNotMatch]]
  */
sealed trait ContentDiff

/** Content difference object used when left and right content with same ID are equal. */
case object Equal extends ContentDiff

/** Content difference object used when left and right content with same ID have different size. */
case object SizeDoNotMatch extends ContentDiff

/**
  * Content difference class used when left and right content with same ID and size are not equal.
  *
  * @constructor Create a new content difference object.
  * @param diffs List of all differences.
  */
case class ContentDoNotMatch(diffs: List[(Offset, Length)]) extends ContentDiff

/**
  * ContentDiff service responsible to handle requests for comparing left and right content.
  *
  * Creates local Actor system for processing incoming requests.
  * Sends results of comparison to [[ReplyService]].
  *
  * @constructor Create new ContentDiff service.
  * @param replyService Reply service.
  */
class ContentDiffService(val replyService: ReplyService) {
  private var _contentProcessorRef: ActorRef = _

  Try {
    getClass.getClassLoader.getResource("content-processor.conf").getFile
  } map { f =>
    ConfigFactory.parseFile(new File(f))
  } match {
    case Success(config) =>
      val system = ActorSystem("ContentProcessorSystem", config)
      _contentProcessorRef = system.actorOf(Props(classOf[ContentProcessor], replyService), "content-processor")
    case Failure(e) =>
      e.printStackTrace()
  }

  def contentProcessor: ActorRef =
    _contentProcessorRef
}

/** Factory for [[ContentDiffService]] instances. */
object ContentDiffService {

  /**
    * Creates new [[ContentDiffService]] object.
    *
    * @param replyService Reply service.
    * @return ContentDiff service.
    */
  def apply(replyService: ReplyService): ContentDiffService =
    new ContentDiffService(replyService)
}

/**
  * Actor responsible to process requests for comparing left and right content with same ID.
  *
  * Internally uses cache to store left and right contents, together with the results of comparison.
  * Sends results of comparison to [[ReplyService]] for further asynchronous processing.
  *
  * @constructor Creates a new ContentProcessor actor.
  * @param replyService Reply service
  */
class ContentProcessor(replyService: ReplyService) extends Actor {

  import scala.collection.mutable

  private[diff] val left = mutable.HashMap.empty[String, String]
  private[diff] val right = mutable.HashMap.empty[String, String]
  private[diff] val diffs = mutable.HashMap.empty[String, ContentDiff]

  override def receive: Receive = {
    case rq: LeftRequest =>
      rq.content match {
        case Some(content) =>
          handleLeftOrRightRequest(rq.result, rq.id, content, left, right)
        case _ =>
          replyService ! LeftRightResponse(rq.result, Left("null content"))
      }

    case rq: RightRequest =>
      rq.content match {
        case Some(content) =>
          handleLeftOrRightRequest(rq.result, rq.id, content, right, left)
        case _ =>
          replyService ! LeftRightResponse(rq.result, Left("null content"))
      }

    case rq: DiffRequest =>
      diffs.get(rq.id) match {
        case Some(d) =>
          replyService ! DiffResponse(rq.result, Right(d))
        case _ =>
          replyService ! DiffResponse(rq.result, Left("no content to compare"))
      }
  }

  private def handleLeftOrRightRequest(result: DeferredResult[ResponseEntity[Unit]], id: String, content: String,
                                       firstMap: mutable.HashMap[String, String],
                                       secondMap: mutable.HashMap[String, String]): Unit = {
    def updateFirstAndPublish(): Unit = {
      firstMap += (id -> content)
      replyService ! LeftRightResponse(result, Right(()))
    }

    secondMap.get(id) match {
      case Some(secondContent) =>
        Try {
          content diff secondContent
        } match {
          case Success(contentDiff) =>
            diffs put(id, contentDiff)
            updateFirstAndPublish()
          case _ =>
            replyService ! LeftRightResponse(result, Left("invalid content"))
        }
      case _ =>
        if (content.isValid) {
          updateFirstAndPublish()
        }
        else {
          replyService ! LeftRightResponse(result, Left("invalid content"))
        }
    }
  }
}

/** Factory for [[ContentProcessor]] instances. */
object ContentProcessor {
  type Offset = Int
  type Length = Int

  /**
    * Returns difference between left and right content.
    *
    * @param leftContent  Left content.
    * @param rightContent Right content.
    * @return Content difference.
    */
  def diff(leftContent: String, rightContent: String): ContentDiff = {
    val leftDecoded: Array[Byte] = Base64.getDecoder.decode(leftContent)
    val rightDecoded: Array[Byte] = Base64.getDecoder.decode(rightContent)

    (leftDecoded.length, rightDecoded.length) match {
      case (size1, size2) if size1 != size2 =>
        SizeDoNotMatch
      case _ =>
        diff(leftDecoded, rightDecoded) match {
          case xs if xs.isEmpty =>
            Equal
          case xs =>
            ContentDoNotMatch(xs.toList)
        }
    }
  }

  /**
    * Checks is content valid.
    *
    * @param content Content to check.
    * @return True if content is valid, otherwise false.
    */
  def isValid(content: String): Boolean =
    Try {
      Base64.getDecoder.decode(content)
    } match {
      case Success(_) =>
        true
      case _ =>
        false
    }

  /** Used to implicitly convert [[String]] content object to [[ContentProcessorOps]] object. */
  implicit def toContentProcessorOps(content: String): ContentProcessorOps =
    new ContentProcessorOps(content)

  /**
    * Adds additional operations to [[String]] content instances.
    *
    * @param content Content instance.
    */
  class ContentProcessorOps(content: String) {

    /**
      * Returns difference between this and that content.
      *
      * @param thatContent That content.
      * @return Content difference.
      */
    def diff(thatContent: String): ContentDiff =
      ContentProcessor.diff(this.content, thatContent)

    /**
      * Checks is content valid.
      *
      * @return True if content is valid, otherwise false.
      */
    def isValid: Boolean =
      ContentProcessor.isValid(content)
  }

  private def diff(leftDecoded: Array[Byte], rightDecoded: Array[Byte]): ListBuffer[(Offset, Length)] = {
    type Acc = ListBuffer[(Offset, Length)]

    def newAcc(p: Int, k: Int, acc: Acc): Acc = {
      val length = k - p
      if (length != 0) acc += (p -> length) else acc
    }

    val (p, k, acc) = (leftDecoded zip rightDecoded).foldLeft[(Int, Int, Acc)](0, 0, ListBuffer()) {
      case ((p, k, acc), (left, right)) =>
        if (left == right) {
          (k + 1, k + 1, newAcc(p, k, acc))
        }
        else {
          (p, k + 1, acc)
        }
    }

    newAcc(p, k, acc)
  }
}
