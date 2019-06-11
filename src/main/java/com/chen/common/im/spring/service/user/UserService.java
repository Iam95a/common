package com.chen.common.im.spring.service.user;


import com.chen.common.protobuf.RequestMessageProto;
import io.netty.channel.Channel;

public interface UserService {
    void login(String nickname, String password, Channel channel, RequestMessageProto.RequestMessage requestMessage);

    long getId();
}
