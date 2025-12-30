package com.zjg.pikaaicodebackend.core.saver;

import cn.hutool.core.util.StrUtil;
import com.zjg.pikaaicodebackend.ai.model.HtmlCodeResult;
import com.zjg.pikaaicodebackend.exception_.BusinessException;
import com.zjg.pikaaicodebackend.exception_.ErrorCode;
import com.zjg.pikaaicodebackend.model.enums_.CodeGenTypeEnum;

import java.io.File;
import java.util.concurrent.BrokenBarrierException;

/**
 * HTML代码保存器
 */
public class HtmlCodeSaverTemplate extends CodeFileSaverTemplate<HtmlCodeResult> {


    @Override
    protected void saveFiles(HtmlCodeResult result, String baseDirPath) {

        writeToFile(baseDirPath, "index.html", result.getHtmlCode());

    }

    @Override
    protected CodeGenTypeEnum getCodeType() {
        return CodeGenTypeEnum.HTML;
    }

    @Override
    protected void validInput(HtmlCodeResult result) {
        super.validInput(result);
        if (StrUtil.isBlank(result.getHtmlCode())) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "html代码不能为空");
        }
    }
}
