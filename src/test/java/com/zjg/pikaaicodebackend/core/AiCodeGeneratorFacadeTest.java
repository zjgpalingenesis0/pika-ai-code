package com.zjg.pikaaicodebackend.core;

import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;


    final Long appId = 364595819594248192L;
    @Test
    void generateAndSaveCode() {

        File file = aiCodeGeneratorFacade.generateAndSaveCode("做一个wanfeng的每周计划表", CodeGenTypeEnum.MULTI_FILE, appId);
        Assertions.assertNotNull(file);
    }

    @Test
    void generateAndSaveCodeStream() {
        Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream("wanfeng笔记本", CodeGenTypeEnum.HTML, appId);
        //阻塞等待所有数据收集完成
        List<String> result = codeStream.collectList().block();
        //验证结果
        Assertions.assertNotNull(result);
        String completeContent = String.join("", result);
        Assertions.assertNotNull(completeContent);
    }
}