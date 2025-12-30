package com.zjg.pikaaicodebackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.zjg.pikaaicodebackend.ai.model.MultiFileCodeResult;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ErrorCode;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;

/**
 * 多文件代码保存器
 */
public class MultiFileSaverTemplate extends CodeFileSaverTemplate<MultiFileCodeResult> {

    @Override
    protected void saveFiles(MultiFileCodeResult result, String baseDirPath) {
        writeToFile(baseDirPath, "index.html", result.getHtmlCode());
        writeToFile(baseDirPath, "style.css", result.getCssCode());
        writeToFile(baseDirPath, "script.js", result.getJsCode());
    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.MULTI_FILE;
    }

    @Override
    protected void validInput(MultiFileCodeResult result) {
        super.validInput(result);
        String htmlCode = result.getHtmlCode();
//        String cssCode = result.getCssCode();
//        String jsCode = result.getJsCode();
//        if (StrUtil.hasBlank(htmlCode, cssCode, jsCode)) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码不能为空");
//        }
        if (StrUtil.isBlank(htmlCode)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "代码不能为空");
        }
    }
}
