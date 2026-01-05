package com.zjg.pikaaicodebackend.ai;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zjg.pikaaicodebackend.service.ChatHistoryService;
import dev.langchain4j.community.store.memory.chat.redis.RedisChatMemoryStore;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 创建AI服务代理
 */
@Slf4j
@Configuration
public class AiCodeGeneratorServiceFactory {

    @Resource
    private ChatModel chatModel;

    @Resource
    private StreamingChatModel streamingChatModel;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<Long, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("Ai服务实例被移除，appId: {}, 原因: {}", key, cause);
            })
            .build();

    @Resource
    private ChatHistoryService chatHistoryService;

    /**
     * 根据appId获取服务（带缓存）
     * @param appId
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(Long appId) {
        return serviceCache.get(appId, this::createAiCodeGeneratorService);
    }

    /**
     * 创建新的AI服务实例
     * @param appId
     * @return
     */
    private AiCodeGeneratorService createAiCodeGeneratorService(Long appId) {
//        return AiServices.create(AiCodeGeneratorService.class, chatModel);
        //根据appId获取服务
        log.info("为appId: {} 创建新的AI Service实例", appId);
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .id(appId)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
        //从数据库加载历史对话到记忆中
        chatHistoryService.loadChatHistoryToMemory(appId, chatMemory, 10);
        return AiServices.builder(AiCodeGeneratorService.class)
                .chatModel(chatModel)
                .streamingChatModel(streamingChatModel)
                .chatMemory(chatMemory)
                //根据id构建独立的对话记忆
//                .chatMemoryProvider(memoryId -> MessageWindowChatMemory
//                        .builder()
//                        .id(memoryId)
//                        .chatMemoryStore(redisChatMemoryStore)
//                        .maxMessages(20)
//                        .build())
                .build();
    }

    /**
     * 提供一个默认的Bean
     * @return
     */
    @Bean
    public AiCodeGeneratorService aiCodeGeneratorService() {
        return getAiCodeGeneratorService(0L);
    }
}
