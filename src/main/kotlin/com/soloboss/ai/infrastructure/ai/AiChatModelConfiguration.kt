package com.soloboss.ai.infrastructure.ai

import org.springframework.ai.chat.model.ChatModel
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class AiChatModelConfiguration {
    @Bean
    @Primary
    fun primaryChatModel(
        @Qualifier("anthropicChatModel") anthropicChatModel: ChatModel,
    ): ChatModel = anthropicChatModel
}
