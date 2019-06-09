package com.chen.common.im;

import com.chen.common.im.entity.User;
import com.chen.common.protobuf.SingleMessageProto;
import com.chen.common.protobuf.enums.TypeEnum;
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

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImServer {
    private static Map<Integer, User> map = new ConcurrentHashMap();

    public static void main(String[] args) throws Exception {
        NioEventLoopGroup group = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(8888))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new ProtobufVarint32FrameDecoder())
                                .addLast(new ProtobufEncoder())
                                .addLast(new ProtobufDecoder(SingleMessageProto.SingleMessage.getDefaultInstance()))
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                        if (msg instanceof SingleMessageProto.SingleMessage) {
                                            SingleMessageProto.SingleMessage singleMessage = (SingleMessageProto.SingleMessage) msg;
                                            if (singleMessage.getType() == TypeEnum.LOGIN.getCode()) {
                                                User user = new User();
                                                user.setUserId(1);
                                                user.setNickname(singleMessage.getNickname());
                                                map.put(user.getUserId(), user);
                                                ctx.writeAndFlush(getReturnMessage(singleMessage, user));
                                            }
                                        } else {
                                            System.out.println(msg);
                                        }
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
