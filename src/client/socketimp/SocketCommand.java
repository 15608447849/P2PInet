package client.socketimp;

import client.obj.SerializeConnectTask;
import client.obj.SerializeSource;
import protocol.Command;
import protocol.Parse;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by user on 2017/6/2.
 */
public class SocketCommand {
    private SocketManager manager;

    public SocketCommand(SocketManager manager) {
        this.manager = manager;
    }

    /**
     * 发送心跳
     *  1 + 数据长度 + 数据块
     */
    //发送心跳
    public void sendHeartbeat() throws IOException {
        if (manager.isValid()){
            byte[] ip = manager.info.getLocalAddress().getAddress().getAddress();
            byte[] port =   Parse.int2bytes (manager.info.getLocalAddress().getPort());
            byte[] mac = manager.info.getLocalMac();
            byte[] length = Parse.int2bytes(ip.length+port.length+mac.length);
            ByteBuffer buffer = ByteBuffer.allocate(1+length.length+ip.length+port.length+mac.length);
            buffer.clear();
            buffer.put(Command.Client.heartbeat);
            buffer.put(length);
            buffer.put(ip);
            buffer.put(port);
            buffer.put(mac);
            buffer.flip();
            manager.socket.write(buffer);
        }
    }

    /**
     * 资源同步 {协议号,数据长度,mac,资源对象}
     * @param source
     */
    public void synchronizationSource(SerializeSource source) throws IOException {
        if (manager.isValid()){
            byte[] mac = manager.info.getLocalMac();//mac地址
            source.setInitiatorMacAddress(manager.info.getLocalMac());//设置资源同步发起者MAC
            byte[] sourceArr = Parse.sobj2Bytes(source);//资源
            byte[] length = Parse.int2bytes(sourceArr.length);//长度
            ByteBuffer buffer = ByteBuffer.allocate(1+length.length+sourceArr.length);
            buffer.clear();
            buffer.put(Command.Client.synchronizationSource);
            buffer.put(length);
            buffer.put(sourceArr);
            buffer.flip();
            manager.socket.write(buffer);
        }
    }

    /**
     * 发起连接建立请求
     * @param connTask
     */
    public void connectSourceClient(SerializeConnectTask connTask) throws IOException {
        if (manager.isValid()){
            byte[] mac = manager.info.getLocalMac();//mac地址
            connTask.setRequestHostMac(manager.info.getLocalMac());
            byte[] sourceArr = Parse.sobj2Bytes(connTask);//资源
            byte[] length = Parse.int2bytes(sourceArr.length);//长度
            ByteBuffer buffer = ByteBuffer.allocate(1+length.length+sourceArr.length);
            buffer.clear();
            buffer.put(Command.Client.connectSourceClient);//搭桥请求
            buffer.put(length);
            buffer.put(sourceArr);
            buffer.flip();
            manager.socket.write(buffer);
        }
    }


}
