/*                     __                                               *\
**     ________ ___   / /  ___     content-diff                         **
**    / __/ __// _ | / /  / _ |    (c) 2017                             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.content.diff

import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult

/**
  * Contains definition of the messages used in communication between components of the system.
  */
object ContentDiffMessages {

  /**
    * Represents messages used to create requests for content processing.
    *
    * Following objects use this trait:
    * [[LeftRequest]]
    * [[RightRequest]]
    * [[DiffRequest]]
    *
    * @tparam R type of the object that will be returned upon completion of this request.
    */
  sealed trait Request[R] {
    /** Placeholder for the response value upon request completion. */
    val result: DeferredResult[ResponseEntity[R]]

    /** ID of the content to process. */
    val id: String
  }

  /**
    * Request used to update/put left content.
    *
    * @constructor Creates new Left request.
    * @param result  Placeholder for the response.
    * @param id      Content id.
    * @param content Content to process.
    */
  case class LeftRequest(
                          result: DeferredResult[ResponseEntity[Unit]],
                          id: String,
                          content: Option[String])
    extends Request[Unit]

  /**
    * Request used to update/put right content.
    *
    * @constructor Creates new Right request.
    * @param result  Placeholder for the response.
    * @param id      Content id.
    * @param content Content to process.
    */
  case class RightRequest(
                           result: DeferredResult[ResponseEntity[Unit]],
                           id: String,
                           content: Option[String])
    extends Request[Unit]

  /**
    * Request used to get difference between left and right content with same ID.
    *
    * @constructor Creates new Difference request.
    * @param result Placeholder for the response.
    * @param id     Left/right content id.
    */
  case class DiffRequest(
                          result: DeferredResult[ResponseEntity[DiffsJson]],
                          id: String)
    extends Request[DiffsJson]

  /**
    * Represents messages that contain result of content processing requests.
    *
    * Following objects use this trait:
    * [[LeftRightResponse]]
    * [[DiffResponse]]
    */
  sealed trait Response {
    /** Type of the result object. */
    type R

    /** Placeholder for the result object. */
    val result: DeferredResult[ResponseEntity[R]]
  }

  /**
    * Response that contains result of processing update/put left and right requests.
    *
    * @constructor Creates new Left/Right response.
    * @param result        Placeholder for the result object.
    * @param contentUpdate Left if request is failure, otherwise Right.
    */
  case class LeftRightResponse(
                                result: DeferredResult[ResponseEntity[Unit]],
                                contentUpdate: Either[String, Unit])
    extends Response {
    type R = Unit
  }

  /**
    * Response that contains result of processing request to get difference between left and right content with same ID.
    *
    * @constructor Creates new Difference response.
    * @param result      Placeholder for the result object.
    * @param contentDiff Left if request is failure, otherwise Right containing difference.
    */
  case class DiffResponse(
                           result: DeferredResult[ResponseEntity[DiffsJson]],
                           contentDiff: Either[String, ContentDiff])
    extends Response {
    type R = DiffsJson
  }

}
