package com.zjg.pikaaicodebackend.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppAddRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 初始化提示词
     */
    private String initPrompt;
}