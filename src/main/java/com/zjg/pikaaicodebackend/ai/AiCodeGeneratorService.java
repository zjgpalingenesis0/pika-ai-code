package com.zjg.pikaaicodebackend.ai;

import com.zjg.pikaaicodebackend.ai.model.HtmlCodeResult;
import com.zjg.pikaaicodebackend.ai.model.MultiFileCodeResult;
import dev.langchain4j.service.SystemMessage;

/**
 * 生成代码的服务接口
 */
public interface AiCodeGeneratorService {
    /**
     * 生成HTML代码
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-html-system-prompt.txt")
    HtmlCodeResult generateHtmlCode(String userMessage);

    /**
     * 生成多文件代码
     * @param userMessage
     * @return
     */
    @SystemMessage(fromResource = "prompt/codegen-multi-file-system-prompt.txt")
    MultiFileCodeResult generateMultiFileCode(String userMessage);
}
