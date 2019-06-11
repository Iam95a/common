package com.chen.common.im;

import com.chen.common.im.constant.Constant;
import com.chen.common.im.entity.User;
import com.chen.common.protobuf.RequestMessageProto;
import com.chen.common.protobuf.enums.TypeEnum;
import com.chen.common.redis.RedisKeys;
import com.chen.common.redis.RedisUtil;
import com.google.common.collect.ImmutableMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : goldgreat
 * @Description :
 * @Date :  2019/6/11 14:33
 */
public class ImHandler extends ChannelInboundHandlerAdapter {
    private static Map<Integer, User> onlineMap = new ConcurrentHashMap();
    private static Map<String, User> channelMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端上线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("客户端掉线");
        User user = channelMap.get(ctx.channel().id().asShortText());
        onlineMap.remove(user.getUserId());
        channelMap.remove(ctx.channel().id().asShortText());
    }

    private void sendSuccess(String cmd, Long msgId, Map<String, String> param, ChannelHandlerContext ctx) {
        RequestMessageProto.RequestMessage.Builder builder = RequestMessageProto.RequestMessage.newBuilder();
        builder.setCommand(cmd);
        builder.setMsgId(msgId);
        builder.getParamsMap().putAll(param);
        builder.setCode(200);
        ctx.writeAndFlush(builder.build());
    }

    private void sendFail(String cmd, Long msgId, Map<String, String> param, ChannelHandlerContext ctx) {
        RequestMessageProto.RequestMessage.Builder builder = RequestMessageProto.RequestMessage.newBuilder();
        builder.setCommand(cmd);
        builder.setMsgId(msgId);
        builder.getParamsMap().putAll(param);
        builder.setCode(400);
        ctx.writeAndFlush(builder.build());
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof RequestMessageProto.RequestMessage) {
            RequestMessageProto.RequestMessage requestMessage = (RequestMessageProto.RequestMessage) msg;
            String command = requestMessage.getCommand();
            if (command.equals(Constant.CMD_LOGIN)) {
                RequestMessageProto.RequestMessage.User loginUser = requestMessage.getUser();
                String nickname = loginUser.getNickname();
                String password = loginUser.getPassword();
                Map<String, String> userMap = RedisUtil.getRedisUtil().hgetall(nickname);
                User user = User.map2User(userMap);
                if (user == null) {
                    //那么走用户注册的路线
                    Long userId = getId();
                    user = new User();
                    user.setUserId(userId);
                    user.setNickname(nickname);
                    user.setPassword(DigestUtils.md5Hex(password));
                    user.setChannel(ctx.channel());
                    RedisUtil.getRedisUtil().hmset(nickname, User.user2Map(user));
                    channelMap.put(ctx.channel().id().asShortText(), user);
                    onlineMap.put(userId.intValue(), user);
                    sendSuccess(Constant.CMD_LOGIN, requestMessage.getMsgId(), ImmutableMap.of("userId", userId + ""), ctx);
                } else {
                    if (user.getPassword().equals(DigestUtils.md5Hex(password))) {
                        //密码校验通过
                        channelMap.put(ctx.channel().id().asShortText(), user);
                        onlineMap.put(user.getUserId().intValue(), user);
                        sendSuccess(Constant.CMD_LOGIN, requestMessage.getMsgId(),
                                ImmutableMap.of("userId", user.getUserId() + ""), ctx);

                    } else {
                        //密码错误  直接关了算了
                        sendFail(Constant.CMD_LOGIN, requestMessage.getMsgId(), new HashMap<>(0), ctx);
                    }
                }
            } else if (requestMessage.getCommand().equals(Constant.CMD_SINGLE)) {
                RequestMessageProto.RequestMessage.SingleMessage singleMessage = requestMessage.getSingleMessage();
                long receiverId = singleMessage.getReceiverId();
                User receiverUser = onlineMap.get(receiverId);
                if (receiverUser != null) {
                    if (receiverUser.getChannel().isOpen()) {
                        receiverUser.getChannel().writeAndFlush(requestMessage);
                    } else {
                        //todo  用户不在线  放到哪里去  上线时候取

                    }
                } else {
                    //todo  用户不在线

                }
            }
        } else {
            System.out.println(msg);
        }
    }

    private long getId() {
        return RedisUtil.getRedisUtil().incrBy(RedisKeys.USER_ID, 1);
    }

}
