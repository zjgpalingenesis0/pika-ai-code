package com.zjg.pikaaicodebackend;

import dev.langchain4j.community.store.embedding.redis.spring.RedisEmbeddingStoreAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@MapperScan("com.zjg.pikaaicodebackend.mapper")
@SpringBootApplication(exclude = {RedisEmbeddingStoreAutoConfiguration.class})
@EnableAspectJAutoProxy(exposeProxy = true)
//@SpringBootApplication
public class PikaAiCodeBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(PikaAiCodeBackendApplication.class, args);
	}

}
