package com.zjg.pikaaicodebackend.core;

import com.zjg.pikaaicodebackend.ai.AiCodeGeneratorService;
import com.zjg.pikaaicodebackend.ai.model.HtmlCodeResult;
import com.zjg.pikaaicodebackend.ai.model.MultiFileCodeResult;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

import static com.zjg.pikaaicodebackend.exception_.ErrorCode.SYSTEM_ERROR;

/**
 * AI代码生成外观类，组合生成和保存功能
 */
@Service
@Slf4j
public class AiCodeGeneratorFacade {
    @Resource
    private AiCodeGeneratorService aiCodeGeneratorService;

    /**
     * 统一接口，根据类型生成并保存代码
     * @param userMessage
     * @return
     */
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(SYSTEM_ERROR, "生成代码类型不存在");
        }
        return switch(codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCode(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCode(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型: " + codeGenTypeEnum.getValue();
                throw new BusinessException(SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一接口，根据类型生成并保存代码 (流式)
     * @param userMessage
     * @param codeGenTypeEnum
     * @return
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(SYSTEM_ERROR, "生成代码类型不存在");
        }
        return switch(codeGenTypeEnum) {
            case HTML -> generateAndSaveHtmlCodeStream(userMessage);
            case MULTI_FILE -> generateAndSaveMultiFileCodeStream(userMessage);
            default -> {
                String errorMessage = "不支持的生成类型: " + codeGenTypeEnum.getValue();
                throw new BusinessException(SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 生成HTML模式的代码并保存
     * @param userMessage
     * @return
     */
    private File generateAndSaveHtmlCode(String userMessage) {
        HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
        return CodeFileSaver.saveHtmlCode(result);
    }

    /**
     * 生成多文件模式的代码并保存
     * @param userMessage
     * @return
     */
    private File generateAndSaveMultiFileCode(String userMessage) {
        MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
        return CodeFileSaver.saveMultiFileCode(result);
    }

    /**
     * 生成HTML模式的代码并保存（流式）
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveHtmlCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
        //当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        //实时收集代码片段
        return result.doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    //流式输出完成后保存代码
                    try {
                        String completeHtmlCode = codeBuilder.toString();
                        HtmlCodeResult htmlCodeResult = CodeParser.parseHtmlCode(completeHtmlCode);
                        //保存代码到文件
                        File saveDir = CodeFileSaver.saveHtmlCode(htmlCodeResult);
                        log.info("保存成功，路径为: {}", saveDir.getAbsoluteFile());
                    } catch (Exception e) {
                        log.error("保存失败, {}", e.getMessage());
                    }
                });
    }

    /**
     * 生成多文件模式的代码并保存(流式)
     * @param userMessage
     * @return
     */
    private Flux<String> generateAndSaveMultiFileCodeStream(String userMessage) {
        Flux<String> result = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
        //当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        //实时收集代码片段
        return result.doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    //流式输出完成后保存代码
                    try {
                        String completeMultiFileCode = codeBuilder.toString();
                        MultiFileCodeResult multiFileCodeResult = CodeParser.parseMultiFileCode(completeMultiFileCode);
                        //保存代码到文件
                        File saveDir = CodeFileSaver.saveMultiFileCode(multiFileCodeResult);
                        log.info("保存成功，路径为: {}", saveDir.getAbsoluteFile());
                    } catch (Exception e) {
                        log.error("保存失败, {}", e.getMessage());
                    }
                });
    }
}
