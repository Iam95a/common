package com.chen.common.im;

import com.chen.common.im.constant.Constant;
import com.chen.common.im.entity.User;
import com.chen.common.protobuf.RequestMessageProto;
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
import org.apache.commons.lang3.RandomUtils;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ImClient {
    public static Channel channel;

    public static User user;

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            startClient(RandomUtils.nextInt(100, 200) + "", "123456", group);
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void startClient(String nickname, String password, EventLoopGroup group) throws Exception {
        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress("127.0.0.1", 8888))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        sc.pipeline().addLast(new ProtobufVarint32FrameDecoder());
                        sc.pipeline().addLast(new ProtobufDecoder(RequestMessageProto.RequestMessage.getDefaultInstance()));
                        sc.pipeline().addLast(new ProtobufVarint32LengthFieldPrepender());
                        sc.pipeline().addLast(new ProtobufEncoder());
                        sc.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                            private void sendLoginRequest(ChannelHandlerContext ctx) {
                                RequestMessageProto.RequestMessage.Builder builder = RequestMessageProto.RequestMessage.newBuilder();
                                builder.setCommand(Constant.CMD_LOGIN);
                                RequestMessageProto.RequestMessage.User.Builder userBuilder = RequestMessageProto.RequestMessage.User.newBuilder();
                                userBuilder.setNickname(nickname);
                                userBuilder.setPassword(password);
                                builder.setUser(userBuilder.build());
                                ctx.writeAndFlush(builder.build());
                            }

                            @Override
                            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                channel = ctx.channel();
                                sendLoginRequest(ctx);
                            }

                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


                            }


                        });
                    }
                });
        ChannelFuture f = b.connect().sync();
        f.channel().closeFuture().sync();

    }
}
