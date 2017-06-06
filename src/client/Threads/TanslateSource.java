package client.Threads;

import client.obj.SerializeConnectTask;
import protocol.Command;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/6/6.
 */
public class TanslateSource extends Thread {

    private byte[] macBytes;
    private DatagramChannel datagramChannel;
    private InetSocketAddress localSocket;
    private InetSocketAddress destSocket;
    private ByteBuffer byteBuffer;
    public TanslateSource(byte[] mac,InetSocketAddress localSocket,InetSocketAddress destSocket) {
        this.macBytes = mac;
        this.localSocket = localSocket;
        this.destSocket = destSocket;
        start();
    }


    @Override
    public void run() {

        //创建连接
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.bind(localSocket);
            datagramChannel.configureBlocking(false);
            byteBuffer = ByteBuffer.allocate(Parse.buffSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendUDPHeartbeat();
        //等待服务器告知客户端B的消息
        waitClientBInfo();
        //连接客户端B
        connectClientB();
    }




    //{30资源源心跳协议,mac}
    private void sendUDPHeartbeat() {
        LOG.I("发送UDP心跳.");
        boolean send = true;
        byte cmd = Command.Client.udpHeartbeat;
        while (send){
            //连接到服务器
            try {
                byteBuffer.clear();
                byteBuffer.put(cmd);
                byteBuffer.put(macBytes);
                byteBuffer.flip();
//                LOG.I("发送UDP心跳:"+ destSocket +" - "+ byteBuffer);
                datagramChannel.send(byteBuffer,destSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
//                LOG.I("开始等待...");
                byteBuffer.clear();
                InetSocketAddress socket = (InetSocketAddress) datagramChannel.receive(byteBuffer);
                if (socket != null){
                    send = false;
//                    LOG.I("收到UDP信息:"+byteBuffer);
                    byteBuffer.flip();
                    byte resultCommand = byteBuffer.get(0);
                    if (resultCommand == Command.Server.udpServerReceiveHeartbeatSuccess){

                        LOG.I("收到 服务器的心跳回执.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 等待客户端B源
     */
    private void waitClientBInfo() {
        LOG.I("等待客户端B的信息中.");
        boolean isWait = true;
        while (isWait){
            try {
                byteBuffer.clear();
                InetSocketAddress socket = (InetSocketAddress) datagramChannel.receive(byteBuffer);
                if (socket != null){
                    LOG.I("收到UDP信息 :"+byteBuffer);
                    byteBuffer.flip();
                    byte resultCommand = byteBuffer.get(0);
                    if (resultCommand == Command.Server.udpSourceDestNetAddress){
                        isWait = false;
                        //获取数据
                        int len = byteBuffer.getInt(1);
                        byte[] data = new byte[len];
                        byteBuffer.position(len+1);
                        byteBuffer.get(data,0,len);
                        SerializeConnectTask connectTask = (SerializeConnectTask) Parse.bytes2Sobj(data);
                        if(connectTask.getCompele() == 3){
                            destSocket = connectTask.getDesNet();//目的地源
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * 连接客户端B
     */
    private void connectClientB() {
        LOG.I("连接客户端B - "+destSocket);

    }

















}
