package com.zjg.pikaaicodebackend.core;

import com.zjg.pikaaicodebackend.ai.AiCodeGeneratorService;
import com.zjg.pikaaicodebackend.ai.model.HtmlCodeResult;
import com.zjg.pikaaicodebackend.ai.model.MultiFileCodeResult;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ThrowUtils;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.io.File;

import static com.zjg.pikaaicodebackend.exception_.ErrorCode.SYSTEM_ERROR;

/**
 * AI代码生成外观类，组合生成和保存功能
 */
@Service
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

}
