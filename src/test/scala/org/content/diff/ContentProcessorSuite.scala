package org.content.diff

import java.util.Base64

import org.content.diff.ContentProcessor.toContentProcessorOps
import org.content.diff.TestHelper.transform
import org.junit.runner.RunWith
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Prop}
import org.scalatest.junit.JUnitRunner
import org.scalatest.prop.Checkers
import org.scalatest.{FunSuite, Matchers}

/**
  * Test suite for [[ContentProcessor]] companion object.
  */
@RunWith(classOf[JUnitRunner])
class ContentProcessorSuite extends FunSuite with Matchers with Checkers {

  implicit val contentAr: Arbitrary[(String, String)] = Arbitrary {
    for {
      size <- Gen.choose(1, 100)
      bytes1 <- Gen.containerOfN[Array, Byte](size, arbitrary[Byte])
      bytes2 <- Gen.containerOfN[Array, Byte](size, arbitrary[Byte])
      leftContent = Base64.getEncoder.encodeToString(bytes1)
      rightContent = Base64.getEncoder.encodeToString(bytes2)
    } yield leftContent -> rightContent
  }

  test("Contents equal") {
    val leftContent1 = "AAAAAA=="
    val rightContent1 = "AAAAAA=="

    val actual1: ContentDiff = leftContent1 diff rightContent1

    actual1 should be(Equal)

    val leftContent2 = "AA=="
    val rightContent2 = "AA=="

    val actual2: ContentDiff = leftContent2 diff rightContent2

    actual2 should be(Equal)
  }

  test("Sizes do not match") {
    val leftContent1 = "AAAAAA=="
    val rightContent1 = "AAA="

    val actual1: ContentDiff = leftContent1 diff rightContent1

    actual1 should be(SizeDoNotMatch)

    val leftContent2 = "AAA="
    val rightContent2 = "AAAAAA=="

    val actual2: ContentDiff = leftContent2 diff rightContent2

    actual2 should be(SizeDoNotMatch)
  }

  test("Contents do not match") {
    val leftContent1 = "AAAAAA=="
    val rightContent1 = "AQABAQ=="

    val actual1: ContentDiff = leftContent1 diff rightContent1

    actual1 should be(ContentDoNotMatch(List(0 -> 1, 2 -> 2)))

    val leftContent2 = "AAAAAA=="
    val rightContent2 = "AAABAQ=="

    val actual2: ContentDiff = leftContent2 diff rightContent2

    actual2 should be(ContentDoNotMatch(List(2 -> 2)))

    val leftContent3 = "AA=="
    val rightContent3 = "AQ=="

    val actual3: ContentDiff = leftContent3 diff rightContent3

    actual3 should be(ContentDoNotMatch(List(0 -> 1)))
  }

  test("Contents with same size") {
    val propDiff: Prop = forAll(contentAr.arbitrary) { (contents: (String, String)) =>
      val (leftContent, rightContent) = contents

      (leftContent diff rightContent: @unchecked) match {
        case Equal =>
          leftContent == rightContent
        case ContentDoNotMatch(diffs) =>
          transform(leftContent, rightContent, diffs) == leftContent
      }
    }

    check(propDiff)
  }

  test("Content valid") {
    val content = "AAAAAA=="
    val valid: Boolean = content.isValid

    valid should be(true)
  }

  test("Content invalid") {
    val content = "AAA=="
    val valid: Boolean = content.isValid

    valid should be(false)
  }
}
