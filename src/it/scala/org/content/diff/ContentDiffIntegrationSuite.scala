package org.content.diff

import java.nio.charset.Charset

import org.content.diff.ItHelper.toJson
import org.hamcrest.Matchers.is
import org.hamcrest.collection.IsCollectionWithSize.hasSize
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSpec}
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.context.{ContextConfiguration, TestContextManager}
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.{asyncDispatch, get, put}
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.{jsonPath, status}
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.{MockMvc, MvcResult}
import org.springframework.web.context.WebApplicationContext

/**
  * Integration test suite to test ContentDiff service.
  */
@ContextConfiguration(classes = Array(classOf[ContentDiffConfig]))
@WebAppConfiguration
@RunWith(classOf[JUnitRunner])
class ContentDiffIntegrationSuite extends FunSpec with BeforeAndAfterAll {

  @Autowired
  val webContext: WebApplicationContext = null

  @Autowired
  implicit val messageConverter: MappingJackson2HttpMessageConverter = null

  var mockMvc: MockMvc = _

  override def beforeAll(): Unit = {
    new TestContextManager(this.getClass).prepareTestInstance(this)
    mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build()
  }

  describe("Content diff service") {
    val contentType = new MediaType(
      MediaType.APPLICATION_JSON.getType,
      MediaType.APPLICATION_JSON.getSubtype,
      Charset.forName("utf8"))

    val id = "1"
    val validContentJson1 = new ContentJson("AAAAAA==")
    val validContentJson2 = new ContentJson("AQABAQ==")
    val validContentJson3 = new ContentJson("AAA=")
    val invalidContentJson = new ContentJson("AAA==")
    val nullContentJson = new ContentJson(null)

    it("should return 400 Bad Request when trying to put invalid data 'AAA==' on left endpoint") {
      val result: MvcResult = mockMvc
        .perform(put("/v1/diff/" + id + "/left")
          .contentType(contentType)
          .content(toJson(invalidContentJson)))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isBadRequest)
    }

    it("should return 404 Not Found when trying to get diff and there is no content on both endpoints") {
      val result: MvcResult = mockMvc
        .perform(get("/v1/diff/" + id)
          .contentType(contentType))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isNotFound)
    }

    it("should return 201 Created when trying to put valid data 'AAAAAA==' on left endpoint") {
      val result: MvcResult = mockMvc
        .perform(put("/v1/diff/" + id + "/left")
          .contentType(contentType)
          .content(toJson(validContentJson1)))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isCreated)
    }

    it("should return 404 Not Found when trying to get diff and there is no content on right endpoint") {
      val result: MvcResult = mockMvc
        .perform(get("/v1/diff/" + id)
          .contentType(contentType))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isNotFound)
    }

    it("should return 201 Created when trying to put valid data 'AAAAAA==' on right endpoint") {
      val result: MvcResult = mockMvc
        .perform(put("/v1/diff/" + id + "/right")
          .contentType(contentType)
          .content(toJson(validContentJson1)))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isCreated)
    }

    it("should return 200 OK when trying to get diff and content on both endpoints is equal") {
      val result: MvcResult = mockMvc
        .perform(get("/v1/diff/" + id)
          .contentType(contentType))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.diffResultType", is("Equals")))
        .andExpect(jsonPath("$.diffs", hasSize(0)))
    }

    it("should return 201 Created when trying to update right endpoint with valid data 'AQABAQ=='") {
      val result: MvcResult = mockMvc
        .perform(put("/v1/diff/" + id + "/right")
          .contentType(contentType)
          .content(toJson(validContentJson2)))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isCreated)
    }

    it("should return 200 OK when trying to get diff and content on both endpoints have same size but do not match") {
      val result: MvcResult = mockMvc
        .perform(get("/v1/diff/" + id)
          .contentType(contentType))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.diffResultType", is("ContentDoNotMatch")))
        .andExpect(jsonPath("$.diffs", hasSize(2)))
        .andExpect(jsonPath("$.diffs[0].offset", is("0")))
        .andExpect(jsonPath("$.diffs[0].length", is("1")))
        .andExpect(jsonPath("$.diffs[1].offset", is("2")))
        .andExpect(jsonPath("$.diffs[1].length", is("2")))
    }

    it("should return 201 Created when trying to update right endpoint with valid data 'AAA='") {
      val result: MvcResult = mockMvc
        .perform(put("/v1/diff/" + id + "/right")
          .contentType(contentType)
          .content(toJson(validContentJson3)))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isCreated)
    }

    it("should return 400 Bad Request when trying to put invalid data 'AAA==' on right endpoint") {
      val result: MvcResult = mockMvc
        .perform(put("/v1/diff/" + id + "/right")
          .contentType(contentType)
          .content(toJson(invalidContentJson)))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isBadRequest)
    }

    it("should return 200 OK when trying to get diff and content on both endpoint do not match in size") {
      val result: MvcResult = mockMvc
        .perform(get("/v1/diff/" + id)
          .contentType(contentType))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isOk)
        .andExpect(jsonPath("$.diffResultType", is("SizeDoNotMatch")))
        .andExpect(jsonPath("$.diffs", hasSize(0)))
    }

    it("should return 400 Bad Request when trying to put null data on left endpoint") {
      val result: MvcResult = mockMvc
        .perform(put("/v1/diff/" + id + "/left")
          .contentType(contentType)
          .content(toJson(nullContentJson)))
        .andReturn()

      mockMvc.perform(asyncDispatch(result))
        .andExpect(status().isBadRequest)
    }
  }
}
