package com.zjg.pikaaicodebackend.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 封装AI返回的内容
 */
@Description("生成多个代码文件的结果")
@Data
public class MultiFileCodeResult implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    /**
     * 生成的代码
     */
    @Description("HTML代码")
    private String htmlCode;

    @Description("CSS代码")
    private String cssCode;

    @Description("JS代码")
    private String jsCode;
    /**
     * 描述
     */
    @Description("生成代码的描述")
    private String description;
}
