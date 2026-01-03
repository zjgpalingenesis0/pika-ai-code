package com.zjg.pikaaicodebackend.model.enums_;

import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * 消息类型枚举
 *
 * @author wanfeng
 */
@Getter
public enum MessageTypeEnum {

    USER("用户消息", "user"),
    AI("AI消息", "ai");

    private final String text;
    private final String value;

    MessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value获取枚举
     *
     * @param value 枚举值的value
     * @return 枚举值
     */
    public static MessageTypeEnum getEnumByValue(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        for (MessageTypeEnum item : MessageTypeEnum.values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return null;
    }
}
