package cn.gasin.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.nio.charset.StandardCharsets;

public class NClientHandler extends ChannelHandlerAdapter {

    private ByteBuf byteBuf;

    public NClientHandler() {
        byteBuf = Unpooled.buffer(1024);
        byteBuf.writeBytes("你好, 这是clint的消息".getBytes(StandardCharsets.UTF_8));
    }

//    @Override
//    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        ctx.writeAndFlush(byteBuf);
//    }

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf responseByteBuf = (ByteBuf) msg;
//        byte[] arr = new byte[responseByteBuf.readableBytes()];
//        responseByteBuf.readBytes(arr);
//
//        // 输出响应.
//        System.out.println(new String(arr));
//    }
}
