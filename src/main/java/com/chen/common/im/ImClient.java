package com.chen.common.im;

import com.chen.common.protobuf.SingleMessageProto;
import com.chen.common.protobuf.enums.TypeEnum;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImClient {
    public static int msgId = 1;
    public static int userId = 0;
    public static Map<Integer, SingleMessageProto.SingleMessage> sendMap = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 8888))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                            sc.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                            sc.pipeline().addLast(new ProtobufDecoder(SingleMessageProto.SingleMessage.getDefaultInstance()));
                            sc.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                            sc.pipeline().addLast(new ProtobufEncoder());
                            sc.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    SingleMessageProto.SingleMessage singleMessage = getLoginMessage();
                                    sendMap.put(singleMessage.getMsgId(), singleMessage);
                                    ctx.writeAndFlush(getLoginMessage());
                                }

                                @Override
                                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                    if (msg instanceof SingleMessageProto.SingleMessage) {
                                        SingleMessageProto.SingleMessage singleMessage = (SingleMessageProto.SingleMessage) msg;
                                        if (singleMessage.getType() == TypeEnum.RE_LOGIN.getCode()) {
                                            //说明是登陆回复
                                            userId = singleMessage.getUserId();
                                            SingleMessageProto.SingleMessage sendSingleMessage = sendMap.get(singleMessage.getMsgId());
                                        }
                                    }
                                }

                                private SingleMessageProto.SingleMessage getLoginMessage() {
                                    SingleMessageProto.SingleMessageOrBuilder builder = SingleMessageProto.SingleMessage.newBuilder();
                                    ((SingleMessageProto.SingleMessage.Builder) builder).setMsgId(msgId++);
                                    ((SingleMessageProto.SingleMessage.Builder) builder).setContent("nihao");
                                    ((SingleMessageProto.SingleMessage.Builder) builder).setNickname("chen");
//                                            ((SingleMessageProto.SingleMessage.Builder) builder).setSender("chen"
                                    ((SingleMessageProto.SingleMessage.Builder) builder).setPassword("123456");
                                    ((SingleMessageProto.SingleMessage.Builder) builder).setType(TypeEnum.LOGIN.getCode());
                                    return ((SingleMessageProto.SingleMessage.Builder) builder).build();
                                }


                            });
                        }
                    });
            ChannelFuture f = b.connect().sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }
}
