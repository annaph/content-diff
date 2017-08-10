/*                     __                                               *\
**     ________ ___   / /  ___     content-diff                         **
**    / __/ __// _ | / /  / _ |    (c) 2017                             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.content.diff

import java.util

import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.content.diff.ContentDiffMessages.{DiffResponse, LeftRightResponse, Response}
import org.springframework.http.{HttpStatus, ResponseEntity}

import scala.collection.JavaConverters._
import scala.language.implicitConversions

/**
  * Service that provides mechanism to publish results of the requests for content
  * comparison to clients who initiated the requests.
  *
  * @constructor Creates new Reply service.
  */
class ReplyService {

  import ReplyService._

  private val publishSubject: PublishSubject[Response] = PublishSubject.create()

  publishSubject.flatMap(item => Observable.just(item).subscribeOn(Schedulers.newThread())).subscribe { response =>
    response match {
      case LeftRightResponse(result, Right(())) =>
        result setResult new ResponseEntity(HttpStatus.CREATED)
      case LeftRightResponse(result, Left(_)) =>
        result setResult new ResponseEntity(HttpStatus.BAD_REQUEST)
      case DiffResponse(result, Right(contentDiff)) =>
        result setResult new ResponseEntity(contentDiff.toDiffsJson, HttpStatus.OK)
      case DiffResponse(result, Left(_)) =>
        result setResult new ResponseEntity(HttpStatus.NOT_FOUND)
    }
  }

  /**
    * Publish results of the requests for content comparison.
    *
    * @param response Object containing results of the comparison.
    */
  def publish(response: Response): Unit =
    publishSubject onNext response

  /**
    * Publish results of the requests for content comparison.
    *
    * @param response Object containing results of the comparison.
    */
  def !(response: Response): Unit =
    this publish response
}

/** Factory for [[ReplyService]] instances. */
object ReplyService {

  /**
    * Creates new [[ReplyService]] object.
    *
    * @return Reply service.
    */
  def apply(): ReplyService =
    new ReplyService

  /**
    * Converts [[ContentDiff]] object to [[DiffsJson]] object.
    *
    * @param contentDiff Object to convert.
    * @return Converted object.
    */
  def toDiffsJson(contentDiff: ContentDiff): DiffsJson = contentDiff match {
    case SizeDoNotMatch =>
      new DiffsJson("SizeDoNotMatch", new util.ArrayList[DiffJson]())
    case ContentDoNotMatch(diffs) =>
      val xs: List[DiffJson] = diffs.map {
        case (offset, length) =>
          new DiffJson(offset.toString, length.toString)
      }
      new DiffsJson("ContentDoNotMatch", xs.toBuffer.asJava)
    case _ =>
      new DiffsJson("Equals", new util.ArrayList[DiffJson]())
  }

  /** Used to implicitly convert [[ContentDiff]] object to [[ReplyServiceOps]] object. */
  implicit def toReplyServiceOps(contentDiff: ContentDiff): ReplyServiceOps =
    new ReplyServiceOps(contentDiff)

  /**
    * Adds additional operations to [[ContentDiff]] instances.
    *
    * @param contentDiff [[ContentDiff]] instance.
    */
  class ReplyServiceOps(contentDiff: ContentDiff) {

    /**
      * Converts this object to [[DiffsJson]] object.
      *
      * @return Converted object.
      */
    def toDiffsJson: DiffsJson =
      ReplyService.toDiffsJson(contentDiff)
  }

}
