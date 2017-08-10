package org.content.diff

import akka.actor.{ActorSystem, CoordinatedShutdown}
import akka.testkit.TestActorRef
import org.content.diff.ContentDiffMessages._
import org.junit.runner.RunWith
import org.mockito.Mockito.{doNothing, never, verify}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}
import org.springframework.http.ResponseEntity
import org.springframework.web.context.request.async.DeferredResult

/**
  * Test suite for [[ContentProcessor]] instances.
  */
@RunWith(classOf[JUnitRunner])
class ContentProcessorActorSuite extends FunSuite with BeforeAndAfterAll with Matchers with MockitoSugar {
  implicit val system = ActorSystem("ContentProcessorTestSystem")

  trait TestFixture {
    val replyService: ReplyService = mock[ReplyService]

    val contentProcessorRef = TestActorRef(new ContentProcessor(replyService))
    val contentProcessor: ContentProcessor = contentProcessorRef.underlyingActor
  }

  test("Left request - right empty") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[Unit]]()
      val response = LeftRightResponse(result, Right(()))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAAAAA=="

      contentProcessorRef ! LeftRequest(result, id, Some(content))

      contentProcessor.left.get(id) should be(Some(content))
      contentProcessor.right.get(id) should be(None)
      contentProcessor.diffs.get(id) should be(None)

      verify(replyService) ! response
    }
  }

  test("Left request - right non empty") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[Unit]]()
      val response = LeftRightResponse(result, Right(()))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAAAAA=="
      contentProcessor.right put(id, content)

      contentProcessorRef ! LeftRequest(result, id, Some(content))

      contentProcessor.left.get(id) should be(Some(content))
      contentProcessor.right.get(id) should be(Some(content))
      contentProcessor.diffs.get(id) should be(Some(Equal))

      verify(replyService) ! response
    }
  }

  test("Left request with null data") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[Unit]]()
      val response = LeftRightResponse(result, Left("null content"))

      doNothing().when(replyService) ! response

      val id = "1"

      contentProcessorRef ! LeftRequest(result, id, None)

      contentProcessor.left.get(id) should be(None)
      contentProcessor.right.get(id) should be(None)
      contentProcessor.diffs.get(id) should be(None)

      verify(replyService) ! response
    }
  }

  test("Left request with invalid data") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[Unit]]()
      val response = LeftRightResponse(result, Left("invalid content"))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAA=="
      val rightContent = "AAAAAA=="
      contentProcessor.right put(id, rightContent)

      contentProcessorRef ! LeftRequest(result, id, Some(content))

      contentProcessor.left.get(id) should be(None)
      contentProcessor.right.get(id) should be(Some(rightContent))
      contentProcessor.diffs.get(id) should be(None)

      verify(replyService) ! response
    }
  }

  test("Right request - left empty") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[Unit]]()
      val response = LeftRightResponse(result, Right(()))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAAAAA=="

      contentProcessorRef ! RightRequest(result, id, Some(content))

      contentProcessor.left.get(id) should be(None)
      contentProcessor.right.get(id) should be(Some(content))
      contentProcessor.diffs.get(id) should be(None)

      verify(replyService) ! response
    }
  }

  test("Right request - left not empty") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[Unit]]()
      val response = LeftRightResponse(result, Right(()))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAAAAA=="
      contentProcessor.left put(id, content)

      contentProcessorRef ! RightRequest(result, id, Some(content))

      contentProcessor.left.get(id) should be(Some(content))
      contentProcessor.right.get(id) should be(Some(content))
      contentProcessor.diffs.get(id) should be(Some(Equal))

      verify(replyService) ! response
    }
  }

  test("Right request with null data") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[Unit]]()
      val response = LeftRightResponse(result, Left("null content"))

      doNothing().when(replyService) ! response

      val id = "1"

      contentProcessorRef ! RightRequest(result, id, None)

      contentProcessor.left.get(id) should be(None)
      contentProcessor.right.get(id) should be(None)
      contentProcessor.diffs.get(id) should be(None)

      verify(replyService) ! response
    }
  }

  test("Right request with invalid data") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[Unit]]()
      val response = LeftRightResponse(result, Left("invalid content"))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAA=="

      contentProcessorRef ! RightRequest(result, id, Some(content))

      contentProcessor.left.get(id) should be(None)
      contentProcessor.right.get(id) should be(None)
      contentProcessor.diffs.get(id) should be(None)

      verify(replyService) ! response
    }
  }

  test("Diff request - success") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[DiffsJson]]()
      val response = DiffResponse(result, Right(Equal))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAAAAA=="

      contentProcessor.left put(id, content)
      contentProcessor.right put(id, content)
      contentProcessor.diffs put(id, Equal)

      contentProcessorRef ! DiffRequest(result, id)

      verify(replyService) ! response
    }
  }

  test("Diff request - failure") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[DiffsJson]]()
      val response = DiffResponse(result, Left("no content to compare"))

      doNothing().when(replyService) ! response

      val id = "1"

      contentProcessorRef ! DiffRequest(result, id)

      verify(replyService) ! response
    }
  }

  test("Diff request - failure when only left content is present") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[DiffsJson]]()
      val response = DiffResponse(result, Left("no content to compare"))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAAAAA=="

      contentProcessor.left put(id, content)

      contentProcessorRef ! DiffRequest(result, id)

      verify(replyService) ! response
    }
  }

  test("Diff request - failure when only right content is present") {
    new TestFixture {
      val result = new DeferredResult[ResponseEntity[DiffsJson]]()
      val response = DiffResponse(result, Left("no content to compare"))

      doNothing().when(replyService) ! response

      val id = "1"
      val content = "AAAAAA=="

      contentProcessor.right put(id, content)

      contentProcessorRef ! DiffRequest(result, id)

      verify(replyService) ! response
    }
  }

  test("Unsupported message") {
    new TestFixture {
      contentProcessorRef ! new String("unsupported meesage")

      verify(replyService, never())
    }
  }

  override def afterAll(): Unit = {
    CoordinatedShutdown(system).run()
  }
}
