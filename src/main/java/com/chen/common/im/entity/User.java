package com.chen.common.im.entity;

import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.util.Map;

@Data
public class User {
    private Integer userId;
    private String nickname;
    private String password;

    public static User map2User(Map<String, String> map) {
        if (MapUtils.isNotEmpty(map)) {
            User user = new User();
            user.setUserId(Integer.valueOf(map.getOrDefault("userId", "0")));
            user.setNickname(String.valueOf(map.getOrDefault("nickname", "")));
            user.setPassword(String.valueOf(map.get("password")));
            return user;
        } else {
            return null;
        }
    }
}
