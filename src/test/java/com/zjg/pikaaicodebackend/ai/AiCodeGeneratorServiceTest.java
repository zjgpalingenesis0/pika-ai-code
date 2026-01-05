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
//        String userMessage = "做一个程序员wanfeng的工作记录小工具";
        String userMessage = "做一个wanfeng的每周计划表";
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        Assertions.assertNotNull(result);
    }

    @Test
    void generateMultiFileCode() {
//        String userMessage = "做一个程序员wanfeng的留言板";
        String userMessage = "做一个wanfeng的每周计划表";
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        Assertions.assertNotNull(result);
    }

    @Test
    void testChatMemory() {
//        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCodeMemory(1, "做个程序员wanfeng的工具网站，总代码量不超过 20 行");
//        Assertions.assertNotNull(result);
//        result = aiCodeGeneratorService.generateHtmlCodeMemory(1, "不要生成网站，告诉我你刚刚做了什么？");
//        Assertions.assertNotNull(result);
//        result = aiCodeGeneratorService.generateHtmlCodeMemory(2, "做个程序员wanfeng的工具网站，总代码量不超过 20 行");
//        Assertions.assertNotNull(result);
//        result = aiCodeGeneratorService.generateHtmlCodeMemory(2, "不要生成网站，告诉我你刚刚做了什么？");
//        Assertions.assertNotNull(result);
    }

}