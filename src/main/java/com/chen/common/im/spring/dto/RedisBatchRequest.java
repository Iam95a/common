package com.chen.common.im.spring.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author : Richard
 * @Description :
 * @Date : 2018/6/6
 */
public class RedisBatchRequest {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HincrbyRequest {
        private String key;
        private String field;
        private long value;
    }
}
