package org.content.diff

import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.mock.http.MockHttpOutputMessage

/**
  * Contains utility methods used only in integration tests.
  */
object ItHelper {

  /**
    * Converts object into a string presenting JSON object.
    *
    * @param obj              Object to convert.
    * @param messageConverter Converter used for conversion.
    * @return JSON as string.
    */
  def toJson(obj: AnyRef)(implicit messageConverter: MappingJackson2HttpMessageConverter): String = {
    val outputMessage = new MockHttpOutputMessage()
    messageConverter write(obj, MediaType.APPLICATION_JSON, outputMessage)

    outputMessage.getBodyAsString
  }
}
