/*                     __                                               *\
**     ________ ___   / /  ___     content-diff                         **
**    / __/ __// _ | / /  / _ |    (c) 2017                             **
**  __\ \/ /__/ __ |/ /__/ __ |                                         **
** /____/\___/_/ |_/____/_/ | |                                         **
**                          |/                                          **
\*                                                                      */

package org.content.diff

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.{Bean, DependsOn}

/**
  * Spring configuration class used to create beans at runtime.
  */
@SpringBootApplication
class ContentDiffConfig {

  /**
    * Creates new [[ReplyService]] object.
    *
    * @return Reply service.
    */
  @Bean(Array("reply-service"))
  @Qualifier("reply-service")
  def replyService(): ReplyService =
  ReplyService()

  /**
    * Creates new [[ContentDiffService]] object.
    *
    * @param replyService Reply service.
    * @return ContentDiff service.
    */
  @Bean(Array("content-diff-service"))
  @Qualifier("content-diff-service")
  @DependsOn(Array("reply-service"))
  def contentDiffService(@Qualifier("reply-service") replyService: ReplyService): ContentDiffService =
  ContentDiffService(replyService)

  /**
    * Creates new [[ContentDiffController]] object.
    *
    * @param contentDiffService ContentDiff service.
    * @return ContentDiff controller.
    */
  @Bean
  @Qualifier("content-diff-controller")
  @DependsOn(Array("content-diff-service"))
  def contentDiffController(@Qualifier("content-diff-service") contentDiffService: ContentDiffService): ContentDiffController = {
    ContentDiffController(contentDiffService)
  }
}

/**
  * Application entry point.
  */
object ContentDiffApplication extends App {
  SpringApplication run classOf[ContentDiffConfig]
}
