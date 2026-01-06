package com.zjg.pikaaicodebackend.config;

import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "langchain4j.open-ai.reasoning-streaming-chat-model")
@Data
public class ReasoningStreamingChatModelConfig {

    private String baseUrl;

    private String apiKey;

    /**
     * 流式推理模型--用于vue项目生成
     * @return
     */
    @Bean
    public StreamingChatModel reasoningStreamingChatModel() {

        final String modelName = "deepseek-chat";
        final int maxTokens = 8192;
        //生产环境使用
//        final String modelName = "deepseek-reasoner";
//        final int maxTokens = 32768;

        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .maxTokens(maxTokens)
                .modelName(modelName)
                .logRequests(true)
                .logResponses(true)
                .build();
    }
}
