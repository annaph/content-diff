package org.content.diff

import java.util.concurrent.TimeUnit.SECONDS

import org.awaitility.Awaitility.await
import org.content.diff.ContentDiffMessages.{DiffResponse, LeftRightResponse, Response}
import org.content.diff.ReplyService.toReplyServiceOps
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{FunSuite, Matchers}
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult

import scala.collection.JavaConverters._

/**
  * Test suite for [[ReplyService]] instances.
  */
@RunWith(classOf[JUnitRunner])
class ReplyServiceSuite extends FunSuite with Matchers {

  trait TestFixture {
    val replyService = ReplyService()

    val putResult = new DeferredResult[ResponseEntity[Unit]]
    val getResult = new DeferredResult[ResponseEntity[DiffsJson]]
  }

  test("Publish message which results to reply with 201 HTTP code") {
    new TestFixture {
      val response: Response = LeftRightResponse(putResult, Right(()))

      replyService ! response

      await().timeout(12, SECONDS).until { () => putResult.hasResult }

      putResult.getResult.asInstanceOf[ResponseEntity[Unit]].getStatusCode.toString should be("201")
    }
  }

  test("Publish message which results to reply with 400 HTTP code") {
    new TestFixture {
      val response: Response = LeftRightResponse(putResult, Left("null content"))

      replyService ! response

      await().timeout(12, SECONDS).until { () => putResult.hasResult }

      putResult.getResult.asInstanceOf[ResponseEntity[Unit]].getStatusCode.toString should be("400")
    }
  }

  test("Publish message which results to reply with 200 HTTP code") {
    new TestFixture {
      val response: Response = DiffResponse(getResult, Right(Equal))

      replyService ! response

      await().timeout(12, SECONDS).until { () => getResult.hasResult }

      getResult.getResult.asInstanceOf[ResponseEntity[DiffsJson]].getStatusCode.toString should be("200")
      getResult.getResult.asInstanceOf[ResponseEntity[DiffsJson]].hasBody should be(true)
    }
  }

  test("Publish message which results to reply with 404 HTTP code") {
    new TestFixture {
      val response: Response = DiffResponse(getResult, Left("no content to compare"))

      replyService ! response

      await().timeout(12, SECONDS).until { () => getResult.hasResult }

      getResult.getResult.asInstanceOf[ResponseEntity[DiffsJson]].getStatusCode.toString should be("404")
    }
  }

  test("Convert Equal ContentDiff to DiffsJson") {
    val contentDiff: ContentDiff = Equal

    val diffsJson: DiffsJson = contentDiff.toDiffsJson

    diffsJson.getDiffResultType should be("Equals")
    diffsJson.getDiffs.asScala.toList should be(List())
  }

  test("Convert SizeDoNotMatch ContentDiff to DiffsJson") {
    val contentDiff: ContentDiff = SizeDoNotMatch

    val diffsJson: DiffsJson = contentDiff.toDiffsJson

    diffsJson.getDiffResultType should be("SizeDoNotMatch")
    diffsJson.getDiffs.asScala.toList should be(List())
  }

  test("Convert ContentDoNotMatch ContentDiff to DiffsJson") {
    val contentDiff: ContentDiff = ContentDoNotMatch(List(0 -> 2))

    val diffsJson: DiffsJson = contentDiff.toDiffsJson

    diffsJson.getDiffResultType should be("ContentDoNotMatch")
    diffsJson.getDiffs.asScala.toList.size should be(1)
    diffsJson.getDiffs.asScala.toList.head.getOffset should be("0")
    diffsJson.getDiffs.asScala.toList.head.getLength should be("2")
  }
}
