package com.chen.common.im.spring.service.user.Impl;

import com.chen.common.im.AppContext;
import com.chen.common.im.constant.Constant;
import com.chen.common.im.entity.User;
import com.chen.common.im.spring.cache.UserCache;
import com.chen.common.im.spring.service.BaseService;
import com.chen.common.im.spring.service.redis.RedisService;
import com.chen.common.im.spring.service.user.UserService;
import com.chen.common.protobuf.RequestMessageProto;
import com.chen.common.redis.RedisKeys;
import com.google.common.collect.ImmutableMap;
import io.netty.channel.Channel;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends BaseService implements UserService {

    @Autowired
    private RedisService redisService;


    @Override
    public void login(String nickname, String password, Channel channel, RequestMessageProto.RequestMessage requestMessage) {
        Map<String, String> userMap = AppContext.getContext().getBean(RedisService.class).hgetAll(nickname);
        User user = User.map2User(userMap);
        if (user == null) {
            //那么走用户注册的路线
            Long userId = getId();
            user = new User();
            user.setUserId(userId);
            user.setNickname(nickname);
            user.setPassword(DigestUtils.md5Hex(password));
            user.setChannel(channel);
            AppContext.getContext().getBean(RedisService.class).hmset(nickname, User.user2Map(user));
            UserCache.channelUser.put(channel.id().asShortText(), user);
            UserCache.onlineUser.put(user.getUserId(), user);
            sendSuccess(Constant.CMD_LOGIN, requestMessage.getMsgId(), ImmutableMap.of("userId", user.getUserId() + ""), channel);
        } else {
            if (user.getPassword().equals(DigestUtils.md5Hex(password))) {
                //密码校验通过
                UserCache.channelUser.put(channel.id().asShortText(), user);
                UserCache.onlineUser.put(user.getUserId(), user);
                sendSuccess(Constant.CMD_LOGIN, requestMessage.getMsgId(), ImmutableMap.of("userId", user.getUserId() + ""), channel);
            } else {
                //密码错误  直接关了算了
                //密码错误  直接关了算了
                sendFail(Constant.CMD_LOGIN, requestMessage.getMsgId(), new HashMap<>(0), channel);
            }
        }
    }

    @Override
    public long getId() {
        return redisService.incr(RedisKeys.USER_ID, 1);
    }
}
