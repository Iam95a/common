package com.chen.common.im.spring.service;

import com.chen.common.protobuf.RequestMessageProto;
import io.netty.channel.Channel;

import java.util.Map;

public class BaseService {
    public void sendSuccess(String cmd, Long msgId, Map<String, String> param, Channel channel) {
        RequestMessageProto.RequestMessage.Builder builder = RequestMessageProto.RequestMessage.newBuilder();
        builder.setCommand(cmd);
        builder.setMsgId(msgId);
        builder.getParamsMap().putAll(param);
        builder.setCode(200);
        channel.writeAndFlush(builder.build());
    }

    public void sendFail(String cmd, Long msgId, Map<String, String> param, Channel channel) {
        RequestMessageProto.RequestMessage.Builder builder = RequestMessageProto.RequestMessage.newBuilder();
        builder.setCommand(cmd);
        builder.setMsgId(msgId);
        builder.getParamsMap().putAll(param);
        builder.setCode(400);
        channel.writeAndFlush(builder.build());
    }
}
