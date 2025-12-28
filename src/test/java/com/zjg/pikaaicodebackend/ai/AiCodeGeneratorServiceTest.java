package com.zjg.pikaaicodebackend.ai;

import com.zjg.pikaaicodebackend.ai.model.HtmlCodeResult;
import com.zjg.pikaaicodebackend.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AiCodeGeneratorServiceTest {

    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    @Test
    void generateHtmlCode() {
        String userMessage = "做一个程序员wanfeng的工作记录小工具";
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
        String userMessage = "做一个程序员wanfeng的留言板";
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        Assertions.assertNotNull(result);
    }
}