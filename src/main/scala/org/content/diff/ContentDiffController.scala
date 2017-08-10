/*                     __                                               *\
**     ________ ___   / /  ___     content-diff                         **
**    / __/ __// _ | / /  / _ |    (c) 2017                             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.content.diff

import org.content.diff.ContentDiffMessages.{DiffRequest, LeftRequest, RightRequest}
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation._
import org.springframework.web.context.request.async.DeferredResult

/**
  * REST controller that exposes three endpoints to accept client requests. Endpoints are:
  * <br> left endpoint - used to put/update left content
  * <br> right endpoint - used to put/update right content
  * <br> diff endpoint - used to get difference between left and right content.<br>
  *
  * <br>Uses [[ContentDiffService]] for further client request processing.
  *
  * @constructor Creates a new ContentDiff REST controller.
  * @param contentDiffService ContentDiff service.
  */
@RestController
class ContentDiffController(val contentDiffService: ContentDiffService) {

  /**
    * Endpoint to put/update left content.
    * Process incoming requests asynchronously.
    *
    * @param id      Content id.
    * @param content Content to put/update.
    * @return [[DeferredResult]] for asynchronous processing.
    */
  @RequestMapping(path = Array("/v1/diff/{id}/left"), method = Array(RequestMethod.PUT))
  def updateLeftContent(@PathVariable("id") id: String,
                        @RequestBody content: ContentJson): DeferredResult[ResponseEntity[Unit]] = {
    val result = new DeferredResult[ResponseEntity[Unit]]()
    contentDiffService.contentProcessor ! LeftRequest(result, id, Option(content.getData))

    result
  }

  /**
    * Endpoint to put/update right content.
    * Process incoming requests asynchronously.
    *
    * @param id      Content id.
    * @param content Content to put/update.
    * @return [[DeferredResult]] for asynchronous processing.
    */
  @RequestMapping(path = Array("/v1/diff/{id}/right"), method = Array(RequestMethod.PUT))
  def updateRightContent(@PathVariable("id") id: String,
                         @RequestBody content: ContentJson): DeferredResult[ResponseEntity[Unit]] = {
    val result = new DeferredResult[ResponseEntity[Unit]]()
    contentDiffService.contentProcessor ! RightRequest(result, id, Option(content.getData))

    result
  }

  /**
    * Endpoint used to get difference between left and right content with same ID.
    * Process incoming requests asynchronously.
    *
    * @param id Left and right content id.
    * @return [[DeferredResult]] for asynchronous processing.
    */
  @RequestMapping(path = Array("/v1/diff/{id}"), method = Array(RequestMethod.GET))
  def getDiff(@PathVariable("id") id: String): DeferredResult[ResponseEntity[DiffsJson]] = {
    val result = new DeferredResult[ResponseEntity[DiffsJson]]()
    contentDiffService.contentProcessor ! DiffRequest(result, id)

    result
  }
}

/** Factory for [[ContentDiffController]] instances. */
object ContentDiffController {

  /**
    * Creates new [[ContentDiffController]] object.
    *
    * @param contentDiffService ContentDiff service.
    * @return ContentDiff controller.
    */
  def apply(contentDiffService: ContentDiffService): ContentDiffController =
    new ContentDiffController(contentDiffService)
}
