package com.chen.common.im.spring.cache;

import com.chen.common.im.entity.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserCache {
    public static Map<Long, User> onlineUser = new ConcurrentHashMap<>();
    public static Map<String, User> channelUser = new ConcurrentHashMap<>();
}
