package client.socketimp;

import protocol.Command;
import protocol.Excute;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;

/**
 * Created by user on 2017/6/2.
 * 定时发送心跳
 */
public class SocketHandler extends Thread implements CompletionHandler<Integer, ByteBuffer> {
    private SocketManager manager;
    private long time = 15 * 1000L;
    public SocketHandler(SocketManager manager,int loopTime) {
        this.manager = manager;
        this.time = loopTime * 1000L ;
        start();
    }

    @Override
    public void completed(Integer i, ByteBuffer byteBuffer) {
//           LOG.I("读取到内容 ,长度:"+i+" , "+byteBuffer);
       if (i==-1){
           manager.closeConnect();
           manager.connectServer();
       }else{
           HashMap<String,Object> map = Parse.message(byteBuffer);
           //读取
           read(byteBuffer);
           //处理
           handlerMessage(map);
       }
    }
    //读取
    public void read(ByteBuffer byteBuffer) {
        byteBuffer.clear();
        manager.socket.read(byteBuffer,byteBuffer,this);
    }
    //处理消息
    private boolean handlerMessage(HashMap<String, Object> map) {
        if (map==null) return false;
        return Excute.handlerMessage(Excute.CLIENT,new Object[]{map,manager});
    }

    @Override
    public void failed(Throwable throwable, ByteBuffer asynchronousSocketChannel) {

    }



    @Override
    public void run() {
        while (time > 0){
            try {
                manager.commander.sendHeartbeat();
                synchronized (this){
                    wait(time);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}

