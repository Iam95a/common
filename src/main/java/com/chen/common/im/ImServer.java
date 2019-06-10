package com.chen.common.im;

import com.chen.common.im.entity.User;
import com.chen.common.protobuf.SingleMessageProto;
import com.chen.common.protobuf.enums.TypeEnum;
import com.chen.common.redis.RedisKeys;
import com.chen.common.redis.RedisUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.apache.commons.codec.digest.DigestUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImServer {
    private static Map<Integer, User> onlineMap = new ConcurrentHashMap();
    private static Map<String, User> channelMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8888))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        //protobuf的顺序还是蛮重要的  具体为什么需要查一下资料
                        sc.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                        sc.pipeline().addLast(new ProtobufDecoder(SingleMessageProto.SingleMessage.getDefaultInstance()));
                        sc.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                        sc.pipeline().addLast(new ProtobufEncoder());
                        sc.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("客户端上线");
                            }

                            @Override
                            public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                                System.out.println("客户端掉线");
                                User user = channelMap.get(sc.id().asShortText());
                                onlineMap.remove(user.getUserId());
                                channelMap.remove(sc.id().asShortText());
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                if (msg instanceof SingleMessageProto.SingleMessage) {
                                    SingleMessageProto.SingleMessage singleMessage = (SingleMessageProto.SingleMessage) msg;
                                    if (singleMessage.getType() == TypeEnum.LOGIN.getCode()) {
                                        String password = singleMessage.getPassword();
                                        String nickname = singleMessage.getNickname();
                                        Map<String, String> userMap = RedisUtil.getRedisUtil().hgetall(nickname);
                                        User user = User.map2User(userMap);
                                        if (user == null) {
                                            //那么走用户注册的路线
                                            Long userId = getId();
                                            user = new User();
                                            user.setUserId(userId.intValue());
                                            user.setNickname(nickname);
                                            user.setPassword(DigestUtils.md5Hex(password));
                                            user.setChannel(sc);
                                            RedisUtil.getRedisUtil().hmset(nickname, User.user2Map(user));
                                            channelMap.put(sc.id().asShortText(), user);
                                            onlineMap.put(userId.intValue(), user);
                                            SingleMessageProto.SingleMessage.Builder builder = SingleMessageProto.SingleMessage.newBuilder();
                                            builder.setSender(0);
                                            builder.setMsgId(singleMessage.getMsgId());
                                            builder.setType(TypeEnum.RE_LOGIN.getCode());
                                            builder.setUserId(user.getUserId());
                                            builder.setNickname(nickname);
                                            sc.writeAndFlush(builder.build());
                                        } else {
                                            if (user.getPassword().equals(DigestUtils.md5Hex(password))) {
                                                //密码校验通过
                                                channelMap.put(sc.id().asShortText(), user);
                                                onlineMap.put(user.getUserId().intValue(), user);
                                                SingleMessageProto.SingleMessage.Builder builder = SingleMessageProto.SingleMessage.newBuilder();
                                                builder.setSender(0);
                                                builder.setMsgId(singleMessage.getMsgId());
                                                builder.setType(TypeEnum.RE_LOGIN.getCode());
                                                builder.setUserId(user.getUserId());
                                                builder.setNickname(nickname);
                                                sc.writeAndFlush(builder.build());
                                            } else {
                                                //密码错误  直接关了算了
                                                ctx.close();
                                            }
                                        }
                                    } else if (singleMessage.getType() == TypeEnum.SINGLE.getCode()) {
                                        int senderId = singleMessage.getSender();
                                        int receiver = singleMessage.getReceiver();
                                        User receiverUser = onlineMap.get(receiver);
                                        if (receiverUser == null) {

                                        } else {
                                            receiverUser.getChannel().writeAndFlush(singleMessage);
                                        }
                                    } else if (singleMessage.getType() == TypeEnum.ALL.getCode()) {
                                        for (Integer integer : onlineMap.keySet()) {
                                            onlineMap.get(integer).getChannel().writeAndFlush(singleMessage);
                                        }
                                    }
                                } else {
                                    System.out.println(msg);
                                }
                            }

                            private long getId() {
                                return RedisUtil.getRedisUtil().incrBy(RedisKeys.USER_ID, 1);
                            }

                            private SingleMessageProto.SingleMessage getReturnMessage(SingleMessageProto.SingleMessage singleMessage, User user) {
                                SingleMessageProto.SingleMessageOrBuilder builder = SingleMessageProto.SingleMessage.newBuilder();
                                ((SingleMessageProto.SingleMessage.Builder) builder).setMsgId(singleMessage.getMsgId());
                                ((SingleMessageProto.SingleMessage.Builder) builder).setUserId(user.getUserId());
                                ((SingleMessageProto.SingleMessage.Builder) builder).setType(TypeEnum.LOGIN.getCode());
                                return ((SingleMessageProto.SingleMessage.Builder) builder).build();
                            }

                        });
                    }
                });
        ChannelFuture f = serverBootstrap.bind().sync();

        System.out.println("server started ");
        f.channel().closeFuture().sync();
    }
}
