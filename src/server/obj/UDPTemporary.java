package server.obj;

import client.obj.SerializeConnectTask;
import protocol.Command;
import protocol.Parse;
import server.abs.IOperate;
import server.abs.IThreadInterface;
import utils.LOG;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Created by user on 2017/6/6.
 */
public class UDPTemporary extends Thread{
    private SerializeConnectTask connectTask;
    //本地suocket地址
    private InetSocketAddress socketAddress;
    private Selector selector = null;
    private DatagramChannel channel;
    private ServerCLI clientA;
    private ServerCLI clientB;
    private IThreadInterface manager;
    private ByteBuffer buffer;
    public UDPTemporary(InetSocketAddress socketAddress, SerializeConnectTask task, IOperate operate, IThreadInterface manager) {
            this.socketAddress = socketAddress;
            this.connectTask = task;
            clientA = operate.getClientByMac(task.getSourceMac());
            try {
                clientB = operate.getClientByMac(task.getDestinationMac());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (clientA==null || clientB==null) return;
            this.manager = manager;
            this.connectTask.setServerTempUDP(socketAddress.getAddress().getAddress(),socketAddress.getPort());
            this.connectTask.setComplete(1);
            manager.putUseConnect(socketAddress.getPort(),this);
            buffer = ByteBuffer.allocate(Parse.buffSize);
            start();
    }

    /**
     * 通知连接>>客户端A
     */
    private void notifyClientAConnect() {
        clientA.getWrite().notifyConnect(Command.Server.queryConnectUdp_source,connectTask);
        //clientB.getWrite().notifyConnect(Command.Server.queryConnectUdp_der,connectTask);
    }

    @Override
    public void run() {
        //创建连接
        try {
            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(socketAddress);
            channel.register(selector, SelectionKey.OP_READ);
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifyClientAConnect();
        //开始读取
        try {
            while (selector.select() > 0){
                Iterator iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key;
                        key = (SelectionKey) iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            handler(key);
                        }
                }
                //
            }
            //
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * udp 心跳 : {30,长度,mac地址}
     *
     */
    private void handler(SelectionKey key) {
        try {
            DatagramChannel sc = (DatagramChannel) key.channel();
            buffer.clear();
            InetSocketAddress address = (InetSocketAddress) sc.receive(buffer);
            //协议类型,数据长度,信息序列化对象
            LOG.E("UDP "+this+" 接受到数据:"+ address+" ----> "+buffer);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
