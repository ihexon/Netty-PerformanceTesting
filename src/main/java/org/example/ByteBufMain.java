package org.example;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;


public class ByteBufMain {
    final byte[] CONTENT = new byte[1024];
    int loop = 1800000;


    public static void main(String[] args) {
       ByteBufMain byteBufMain = new ByteBufMain();
       byteBufMain.PooledByteBufAllocatorTest(); // Should faster
       byteBufMain.UnpooledTest(); // Should slower
    }



    private void UnpooledTest(){
        ByteBuf byteBuf = null;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < loop; i++) {
            byteBuf = Unpooled.directBuffer(1024);
            byteBuf.writeBytes(CONTENT);
            byteBuf.release();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Unpooled.directBuffer:"+(endTime-startTime));
    }

    private void PooledByteBufAllocatorTest(){
        ByteBuf poolBuffer = null;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < loop; i++) {
            poolBuffer = PooledByteBufAllocator.DEFAULT.directBuffer(1024);
            poolBuffer.writeBytes(CONTENT);
            poolBuffer.release();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("PooledByteBufAllocator.DEFAULT.directBuffer:"+(endTime-startTime));
    }
}
