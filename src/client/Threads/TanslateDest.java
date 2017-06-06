package client.Threads;

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
public class TanslateDest extends Thread{

    private byte[] mac;
    private InetSocketAddress localSokcet;
    private InetSocketAddress serverSocket;
    private InetSocketAddress sourceSocket;
    private DatagramChannel channel;
    private ByteBuffer buffer;
    public TanslateDest(byte[] localMac, InetSocketAddress localSocket, InetSocketAddress serverSocket, InetSocketAddress sourceSocket) {
        this.mac = localMac;
        this.localSokcet = localSocket;
        this.serverSocket = serverSocket;
        this.sourceSocket = sourceSocket;
        start();
    }


    @Override
    public void run() {
        //打开udp
        try {
            channel = DatagramChannel.open();
            channel.bind(localSokcet);
            channel.configureBlocking(false);
            buffer = ByteBuffer.allocate(Parse.buffSize);
            LOG.I("打开数据接受UDP.");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //连接服务器
        connectServer();
        //连接资源客户端
        connectClientA();










    }



    /**
     * 发送心跳到服务器
     * 服务器收集Net并通知客户端A
     * 同时关闭socket
     */
    private void connectServer() {

        LOG.I("发送心跳到服务器.");
        boolean send = true;
        byte cmd = Command.Client.udpHeartbeat;
        while (send){
            //连接到服务器
            try {
                buffer.clear();
                buffer.put(cmd);
                buffer.put(mac);
                buffer.flip();
//                LOG.I("发送UDP心跳:"+ destSocket +" - "+ byteBuffer);
                channel.send(buffer,serverSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
//                LOG.I("开始等待...");
                buffer.clear();
                InetSocketAddress socket = (InetSocketAddress) channel.receive(buffer);
                if (socket != null){
//                    LOG.I("收到UDP信息:"+byteBuffer);
                    buffer.flip();
                    byte resultCommand = buffer.get(0);
                    if (resultCommand == Command.Server.udpServerReceiveHeartbeatSuccess){
                        send = false;
                        LOG.I("收到 服务器的心跳回执.");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 连接客户端A
     */
    private void connectClientA() {
        LOG.I("连接客户端A.");




    }








}
