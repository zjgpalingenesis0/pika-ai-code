package com.zjg.pikaaicodebackend.core.saver;

import com.zjg.pikaaicodebackend.ai.model.HtmlCodeResult;
import com.zjg.pikaaicodebackend.ai.model.MultiFileCodeResult;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ErrorCode;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;

import java.io.File;
import java.util.HashMap;

/**
 * 代码保存执行器
 */
public class CodeSaverExecutor {

    private static final HtmlCodeSaverTemplate htmlCodeSaverTemplate = new HtmlCodeSaverTemplate();

    private static final MultiFileSaverTemplate multiFileSaverTemplate = new MultiFileSaverTemplate();

    /**
     * 根据代码生成类型保存
     * @param parsedResult   解析过的代码
     * @param codeGenTypeEnum
     * @return
     */
    public static File executorSaver(Object parsedResult, CodeGenTypeEnum codeGenTypeEnum) {
        return switch(codeGenTypeEnum) {
            case MULTI_FILE -> multiFileSaverTemplate.saveFile((MultiFileCodeResult) parsedResult);
            case HTML -> htmlCodeSaverTemplate.saveFile((HtmlCodeResult) parsedResult);
            default -> {
                String errorMessage = "不支持的代码生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }
}
