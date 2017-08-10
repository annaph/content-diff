package org.content.diff

import akka.Done
import akka.actor.{ActorRef, ActorSystem, CoordinatedShutdown, Props}
import org.content.diff.TestHelper.{DiffActor, LeftActor, RightActor}
import org.junit.runner.RunWith
import org.mockito.Mockito.{verify, when}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FunSuite, Matchers}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{Await, Promise}

/**
  * Test suite for [[ContentDiffService]] instances.
  */
@RunWith(classOf[JUnitRunner])
class ContentDiffControllerSuite extends FunSuite with BeforeAndAfterAll with Matchers with MockitoSugar {
  implicit val system = ActorSystem("ContentDiffControllerTestSystem")

  trait TestFixture {
    val contentDiffService: ContentDiffService = mock[ContentDiffService]
    val restController = ContentDiffController(contentDiffService)
  }

  test("Left endpoint") {
    new TestFixture {
      val leftPromise: Promise[Done] = Promise()
      val leftActor: ActorRef = system.actorOf(Props(classOf[LeftActor], leftPromise), "left-actor")

      when(contentDiffService.contentProcessor) thenReturn leftActor

      restController updateLeftContent("1", new ContentJson("AAAAAA=="))

      Await ready(leftPromise.future, 12 seconds)

      verify(contentDiffService).contentProcessor
    }
  }

  test("Right endpoint") {
    new TestFixture {
      val rightPromise: Promise[Done] = Promise()
      val rightActor: ActorRef = system.actorOf(Props(classOf[RightActor], rightPromise), "right-actor")

      when(contentDiffService.contentProcessor) thenReturn rightActor

      restController updateRightContent("1", new ContentJson("AQABAQ=="))

      Await ready(rightPromise.future, 12 seconds)

      verify(contentDiffService).contentProcessor
    }
  }

  test("Diff endpoint") {
    new TestFixture {
      val diffPromise: Promise[Done] = Promise()
      val diffActor: ActorRef = system.actorOf(Props(classOf[DiffActor], diffPromise), "diff-actor")

      when(contentDiffService.contentProcessor) thenReturn diffActor

      restController getDiff "3"

      Await ready(diffPromise.future, 12 seconds)

      verify(contentDiffService).contentProcessor
    }
  }

  override def afterAll(): Unit = {
    CoordinatedShutdown(system).run()
  }
}
