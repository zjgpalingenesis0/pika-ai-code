package com.zjg.pikaaicodebackend.core;

import com.zjg.pikaaicodebackend.ai.AiCodeGeneratorService;
import com.zjg.pikaaicodebackend.ai.model.HtmlCodeResult;
import com.zjg.pikaaicodebackend.ai.model.MultiFileCodeResult;
import com.zjg.pikaaicodebackend.core.parser.CodeParserExecutor;
import com.zjg.pikaaicodebackend.core.saver.CodeSaverExecutor;
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
            case HTML -> {
                HtmlCodeResult result = aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeSaverExecutor.executorSaver(result, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeSaverExecutor.executorSaver(result, CodeGenTypeEnum.MULTI_FILE);
            }
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
            case HTML -> {
                Flux<String> htmlCodeStream = aiCodeGeneratorService.generateHtmlCodeStream(userMessage);
                yield processCodeStream(htmlCodeStream, CodeGenTypeEnum.HTML);
            }
            case MULTI_FILE -> {
                Flux<String> multiFileCodeStream = aiCodeGeneratorService.generateMultiFileCodeStream(userMessage);
                yield processCodeStream(multiFileCodeStream, CodeGenTypeEnum.MULTI_FILE);
            }
            default -> {
                String errorMessage = "不支持的生成类型: " + codeGenTypeEnum.getValue();
                throw new BusinessException(SYSTEM_ERROR, errorMessage);
            }
        };
    }



    /**
     * 通用流式代码处理方法
     * @param codeStream
     * @param codeGenTypeEnum
     * @return
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenTypeEnum) {
        //当流式返回生成代码完成后，再保存代码
        StringBuilder codeBuilder = new StringBuilder();
        //实时收集代码片段
        return codeStream.doOnNext(codeBuilder::append)
                .doOnComplete(() -> {
                    //流式输出完成后保存代码
                    try {
                        String completeCode = codeBuilder.toString();
                        //执行器解析代码
                        Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenTypeEnum);
                        //执行器保存代码
                        File savedDir = CodeSaverExecutor.executorSaver(parsedResult, codeGenTypeEnum);
                        log.info("保存成功，路径为: {}", savedDir.getAbsoluteFile());
                    } catch (Exception e) {
                        log.error("保存失败, {}", e.getMessage());
                    }
                });
    }
}
