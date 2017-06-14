package client.socketimp;

import client.obj.SerializeConnectTask;
import client.obj.SerializeSource;
import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.NetworkUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;

/**
 * Created by user on 2017/6/2.
 */
public class SocketCommand implements CompletionHandler<Integer,Void>{
    private SocketManager manager;

    public SocketCommand(SocketManager manager) {
        this.manager = manager;
    }

    @Override
    public void completed(Integer integer, Void aVoid) {

    }

    @Override
    public void failed(Throwable throwable, Void aVoid) {
            throwable.printStackTrace();
            manager.reConnection();
    }

    /**
     * 发送心跳
     *  1
     *
     *
     *   byte[] ip = manager.info.getLocalAddress().getAddress().getAddress();
     byte[] port =   Parse.int2bytes (manager.info.getLocalAddress().getPort());
     byte[] mac = manager.info.getLocalMac();
     byte[] length = Parse.int2bytes(ip.length+port.length+mac.length);
     ByteBuffer buffer = ByteBuffer.allocate(1+length.length+ip.length+port.length+mac.length);
     buffer.clear();

     buffer.put(length);
     buffer.put(ip);
     buffer.put(port);
     buffer.put(mac);
     */
    //发送心跳
    public void sendHeartbeat() throws IOException {
        if (manager.isValid()){
            ByteBuffer buffer = ByteBuffer.allocate(1);
            buffer.put(Command.Client.heartbeat);
            buffer.flip();
           manager.socket.write(buffer,null,this);
        }
    }

    /**
     * 资源同步 {协议号,数据长度,mac,资源对象}
     * @param source
     */
    public void synchronizationSource(SerializeSource source) throws IOException {
        if (manager.isValid()){
            byte[] mac = manager.info.getLocalMac();//mac地址
            source.setUploaderMac(mac);//设置资源同步发起者MAC
            byte[] sourceArr = Parse.sobj2Bytes(source);//资源
            byte[] length = Parse.int2bytes(sourceArr.length);//长度
            ByteBuffer buffer = ByteBuffer.allocate(1+length.length+sourceArr.length);
            buffer.clear();
            buffer.put(Command.Client.synchronizationSource);
            buffer.put(length);
            buffer.put(sourceArr);
            buffer.flip();
            manager.socket.write(buffer,null,this);
            LOG.I("通知服务器,终端同步资源: "+ source);
        }
    }

    /**
     * 发起连接建立请求
     * @param connTask
     */
    public void connectSourceClient(SerializeConnectTask connTask) throws IOException {
        if (manager.isValid()){
            byte[] mac = manager.info.getLocalMac();//mac地址
            connTask.setDownloadMac(mac);
            byte[] sourceArr = Parse.sobj2Bytes(connTask);//资源
            byte[] length = Parse.int2bytes(sourceArr.length);//长度
            ByteBuffer buffer = ByteBuffer.allocate(1+length.length+sourceArr.length);
            buffer.clear();
            buffer.put(Command.Client.connectSourceClient);//搭桥请求
            buffer.put(length);
            buffer.put(sourceArr);
            buffer.flip();
            manager.socket.write(buffer,null,this);
        }
    }

    //认证成功
    public void sendAuthenticationSucceed() {
        if (manager.isValid()){
            ByteBuffer byteBuffer = ByteBuffer.allocate(1+4+6+4); // 协议+数据长度+mac+nat类型
            byteBuffer.put(Command.Client.authenticationSucceed);
            byteBuffer.put(Parse.int2bytes(10));//数据长度10
            byteBuffer.put(manager.info.getLocalMac());
            byteBuffer.put(Parse.int2bytes(manager.info.natType));
            byteBuffer.flip();
            manager.socket.write(byteBuffer,null,this);
        }
    }
}
