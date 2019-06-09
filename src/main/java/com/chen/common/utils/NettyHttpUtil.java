package com.chen.common.utils;

import com.chen.common.http.entity.HttpRequestInfo;
import com.chen.common.http.util.HttpDownUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NettyHttpUtil {
    public static EventLoopGroup group = new NioEventLoopGroup();

    public static HttpResponse[] doHttpForResponse(String method, String url, Map<String, String> headers, String content, EventLoopGroup eventLoopGroup) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        HttpRequestInfo info = HttpDownUtil.buildRequest(method, url, headers, content);
        System.out.println(info);
        HttpResponse[] httpResponse = new HttpResponse[1];
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(info.requestProto().getHost(), info.requestProto().getPort())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        if (info.requestProto().getSsl()) {
                            sc.pipeline().addLast(HttpDownUtil.getSslContext().newHandler(sc.alloc(), info.requestProto().getHost(),
                                    info.requestProto().getPort()));
                        }
                        sc.pipeline().addLast("codec", new HttpClientCodec())
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
                                        if (o instanceof HttpResponse) {
                                            httpResponse[0] = (HttpResponse) o;
                                            ctx.channel().close();
                                            cdl.countDown();
                                        }
                                    }
                                });

                    }
                });
        ChannelFuture f = bootstrap.connect();
        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                f.channel().writeAndFlush(info);
            } else {
                System.out.println("连接失败");
                cdl.countDown();
            }
        });
        cdl.await(10, TimeUnit.SECONDS);
//        group.shutdownGracefully();
        return httpResponse;
    }

    public static List<String> doHttpForBody(String method, String url, Map<String, String> headers, String content, EventLoopGroup eventLoopGroup) throws Exception {
        CountDownLatch cdl = new CountDownLatch(1);
        HttpRequestInfo info = HttpDownUtil.buildRequest(method, url, headers, content);
        System.out.println(info);
        List<String> list = new ArrayList<>(1);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .remoteAddress(info.requestProto().getHost(), info.requestProto().getPort())
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        if (info.requestProto().getSsl()) {
                            sc.pipeline().addLast(HttpDownUtil.getSslContext().newHandler(sc.alloc(), info.requestProto().getHost(),
                                    info.requestProto().getPort()));
                        }
                        sc.pipeline().addLast("codec", new HttpClientCodec())
                                .addLast(new ChannelInboundHandlerAdapter() {
                                    @Override
                                    public void channelRead(ChannelHandlerContext ctx, Object o) throws Exception {
                                        if (o instanceof HttpContent) {
                                            String result = ((ByteBuf) (((HttpContent) o).content())).toString(CharsetUtil.UTF_8);
                                            list.add(result);
                                            cdl.countDown();
                                            ctx.channel().close();
                                        }
                                    }
                                });

                    }
                });
        ChannelFuture f = bootstrap.connect();
        f.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                f.channel().writeAndFlush(info);
            } else {
                System.out.println("连接失败");
                cdl.countDown();
            }
        });
        cdl.await(10, TimeUnit.SECONDS);
//        group.shutdownGracefully();
        return list;
    }

    public static void main(String[] args) throws Exception {
//        long start=System.currentTimeMillis();
        Map<String, String> map = new HashMap<>();
        map.put("range", "bytes=0-0");
        HttpResponse[] o = doHttpForResponse("get", "https://notepad-plus-plus.org/repository/7.x/7.6.6/npp.7.6.6.Installer.exe",
                map, null, group);
        System.out.println(o);
//        System.out.println(list.get(0));
//        long end=System.currentTimeMillis();
//        System.out.println(start-end);
        group.shutdownGracefully();

    }


}
