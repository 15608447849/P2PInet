package server.imp.threads;

import client.obj.SerializeConnectTask;
import protocol.Command;
import protocol.Parse;
import server.abs.IOperate;
import server.abs.IThreadInterface;
import server.obj.ServerCLI;
import utils.LOG;
import utils.NetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Created by user on 2017/6/6.
 */
public class UDPTemporary extends Thread{
    private volatile boolean flag = true;
    private SerializeConnectTask connectTask;
    //本地suocket地址
    private InetSocketAddress socketAddress;
    private Selector selector = null;
    private DatagramChannel channel;
    private ServerCLI clientA;
    private ServerCLI clientB;
    private IThreadInterface manager;
    private ByteBuffer buffer;
    private IOperate operate;
    public UDPTemporary(InetSocketAddress socketAddress, SerializeConnectTask task, IOperate operate, IThreadInterface manager) {
            this.socketAddress = socketAddress;
            this.connectTask = task;
            this.operate = operate;
            this.manager = manager;
            LOG.I("创建 udp临时连接线程, 地址:"+socketAddress);
            start();
    }
    //初始化
    private boolean init() throws IOException, ClassNotFoundException {

            clientA = operate.getClientByMac(connectTask.getDestinationMac());
            LOG.I("获取到 资源客户端 A:"+clientA);
            clientB = operate.getClientByMac(connectTask.getSourceMac());
            LOG.I("获取到 请求客户端 B:"+clientB);

        if (clientA==null || clientB==null) {
            //LOG.I("无法创建UDP连接中间请求,客户端不存在 - "+clientA+" <> "+ clientB);
            return false;
        }

        this.connectTask.setServerTempUDP(socketAddress.getAddress().getAddress(),socketAddress.getPort());
        this.connectTask.setComplete(1);
        manager.putUseConnect(socketAddress.getPort(),this);
        buffer = ByteBuffer.allocate(Parse.buffSize);
        return true;
    }

    /**
     * 通知连接>>客户端A
     *  -等待客户端A连接.
     */
    private void notifyClientAConnect() {
        boolean f = clientA.getWrite().notifyConnect(Command.Server.queryConnectUdp_source,connectTask);
        LOG.I(this+ " 通知客户端A :"+clientA+" 连接UDP. "+socketAddress + (f?" 成功":" 失败"));
    }
    private void notifyClientBConnect() {
        boolean f = clientB.getWrite().notifyConnect(Command.Server.queryConnectUdp_der,connectTask);
        LOG.I(this+ " 通知客户端B :"+clientB+" 连接UDP. "+socketAddress + (f?" 成功":" 失败"));
    }


    @Override
    public void run() {
        try {
            //初始化
            if (!init()) return;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //创建连接
        try {
            selector = Selector.open();
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.socket().bind(socketAddress);
            channel.register(selector, SelectionKey.OP_READ);
            LOG.I(this+"打开 UDP 监听.");
        } catch (IOException e) {
            e.printStackTrace();
        }
        //通知A
        notifyClientAConnect();
        //开始读取
        try {

            while (flag && selector.select() > 0){
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




    private void handler(SelectionKey key) {
        try {
            DatagramChannel sc = (DatagramChannel) key.channel();
            buffer.clear();
            InetSocketAddress address = (InetSocketAddress) sc.receive(buffer);
            //协议类型,数据长度,信息序列化对象
//            LOG.E("UDP "+this+" 接受到数据:"+ address+" ----> "+buffer);
            buffer.flip();
            byte cmd = buffer.get(0);
//             LOG.E("UDP "+this+" 接受到数据:"+ address+" ----> "+buffer+ " 命令:"+ cmd);
            if (cmd == Command.Client.udpHeartbeat){
                byte[] mac = new byte[6];
                buffer.position(1);
                buffer.get(mac,0,6);
//                LOG.I("对方MAC: "+ NetUtil.macByte2String(mac));

                if (connectTask.getCompele()==1){
                    //记录net信息
                    if (NetUtil.macByte2String(mac).equals(clientA.getMac())){

                        connectTask.setSrcNET(address.getAddress().getAddress(),address.getPort());
                        connectTask.setComplete(2); //填充了源 net信息
                        LOG.I("设置 源 net 信息,成功. "+connectTask.getCompele());
                    }
                    if (connectTask.getCompele() == 2){
                        //回复源
                        buffer.clear();
                        buffer.put(Command.Server.udpServerReceiveHeartbeatSuccess);
                        buffer.flip();
                        channel.send(buffer,address);
                        //通知客户端B
                        notifyClientBConnect();
                    }
                    return;
                }

                //设置客户端B的net信息
                if (connectTask.getCompele()==2){
                    if (NetUtil.macByte2String(mac).equals(clientB.getMac())){
                        connectTask.setDesNET(address.getAddress().getAddress(),address.getPort());
                        connectTask.setComplete(3);
                        LOG.I("设置 目的地 net 信息,成功. "+connectTask.getCompele());
                    }
                    if (connectTask.getCompele() == 3){
                        //回复目的地-客户端B :
                        buffer.clear();
                        buffer.put(Command.Server.udpServerReceiveHeartbeatSuccess);
                        buffer.flip();
                        channel.send(buffer,address);
                    }
                    return;
                }

                if (connectTask.getCompele() == 3){
                    //通知客户端A ,b的信息
                    if (NetUtil.macByte2String(mac).equals(clientA.getMac())){
                        buffer.clear();
                        byte[] data = Parse.sobj2Bytes(connectTask);
                        int len = data.length;
                        byte[] lenBytes = Parse.int2bytes(len);
                        buffer.put(Command.Server.udpSourceDestNetAddress);
                        buffer.put(lenBytes);
                        buffer.put(data);
                        buffer.flip();
                        LOG.I("客户端A,UDP : "+ address + "   "   +buffer);
                        //通知客户端A
                        channel.send(buffer,address);
                        closeTask();
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //结束任务
    private void closeTask() {

            flag = false;
        //关闭socket,返回端口
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            manager.removePort(connectTask.getServerTempUDP().getPort());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        LOG.I("搭桥完成.关闭任务.");
    }
}
