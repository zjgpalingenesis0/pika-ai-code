package com.zjg.pikaaicodebackend.model.enums_;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

@Getter
public enum CodeGenTypeEnum {

    HTML("原生HTML模式", "html"),
    MULTI_FILE("原生多文件模式", "multi_file"),
    VUE_PROJECT("vue工程模式", "vue_project");

    private final String text;
    private final String value;

    CodeGenTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举类
     * @param value
     * @return
     */
    public static CodeGenTypeEnum getByValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (CodeGenTypeEnum codeGenTypeEnum : CodeGenTypeEnum.values()) {
            if (codeGenTypeEnum.getValue().equals(value)) {
                return codeGenTypeEnum;
            }
        }
        return null;
    }
}
