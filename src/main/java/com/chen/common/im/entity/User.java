package com.chen.common.im.entity;

import io.netty.channel.Channel;
import lombok.Data;
import org.apache.commons.collections4.MapUtils;

import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    private Long userId;
    private String nickname;
    private String password;
    private Long createTime;
    private Channel channel;

    public static User map2User(Map<String, String> map) {
        if (MapUtils.isNotEmpty(map)) {
            User user = new User();
            user.setUserId(Long.parseLong(map.getOrDefault("userId", "0")));
            user.setNickname(String.valueOf(map.getOrDefault("nickname", "")));
            user.setPassword(String.valueOf(map.get("password")));
            user.setCreateTime(Long.parseLong(map.get("createTime")));
            return user;
        } else {
            return null;
        }
    }

    public static Map<String, String> user2Map(User user) {
        if (user != null) {
            Map<String, String> map = new HashMap<>(3);
            map.put("userId", user.getUserId() + "");
            map.put("nickname", user.getNickname());
            map.put("password", user.getPassword());
            map.put("createTime", System.currentTimeMillis() + "");
            return map;
        } else {
            return null;
        }
    }
}
