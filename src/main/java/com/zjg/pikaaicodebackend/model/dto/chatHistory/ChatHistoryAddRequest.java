package com.zjg.pikaaicodebackend.model.dto.chatHistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 对话历史添加请求
 *
 * @author wanfeng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型（user/ai）
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;
}
