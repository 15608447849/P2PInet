package server.imp.threads;

import client.socketimp.PortManager;
import protocol.Command;
import protocol.Parse;
import server.abs.IServer;
import server.abs.IThread;
import server.obj.IParameter;
import utils.LOG;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

/**
 * Created by user on 2017/6/8.
 * 打开两个Udp连接
 * 帮助客户端 认证 net类型
 */
public class NetAuthenticationHelper extends IThread {
    private Selector selector;
    private DatagramChannel channel1;
    private DatagramChannel channel2;
    private InetSocketAddress remoteUdpServer;//认证辅助服务器地址
    private ByteBuffer byteBuffer;
    private final CheckAuxiliary checkThread = new CheckAuxiliary();
    public NetAuthenticationHelper(IServer server) {
        super(server);
        IParameter parameter = (IParameter) server.getParam("param");
        InetSocketAddress address2 = new InetSocketAddress(PortManager.get().getPort());
        try {
            init(parameter.udpLocalAddress_Main,address2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  void init(InetSocketAddress address1,InetSocketAddress address2) throws IOException {
        this.selector = Selector.open();
        this.channel1 = DatagramChannel.open().bind(address1);
        this.channel2 = DatagramChannel.open().bind(address2);

        this.channel1.configureBlocking(false);
        this.channel2.configureBlocking(false);

        this.channel1.register(selector, SelectionKey.OP_READ);
        this.channel2.register(selector, SelectionKey.OP_READ);
        //最大数据 : 标识符+IP+PORT = 1+4+4 = 9;
        this.byteBuffer = ByteBuffer.allocate(Parse.NAT_BUFFER_ZONE);
        launch();
        LOG.I("UDP NET 认证服务 ,启动.");
    }

    @Override
    protected void action() {
        while (isRun){
            //开始读取
            try {

                if (selector.select() > 0){
                    Iterator iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key;
                        key = (SelectionKey) iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            handlerMessage(key);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 第一步：检测客户端是否有能力进行UDP通信以及客户端是否位于NAT后？
     * 客户端建立UDP socket然后用这个socket向服务器的(IP-1,Port-1)发送数据包, 要求服务器返回客户端的IP和Port. ( c -> 65 , s -> 66,ip,port)
     *
     * 第二步：检测客户端NAT是否是Full Cone NAT？
     * 客户端要求服务器用其他端口发送数据给客户端.无法接受到服务器的回应,说明客户端的NAT不是一个Full Cone NAT,继续检测.
     *
     * 第三步：检测客户端NAT是否是Symmetric NAT？
     * 客户端向第二个端口发送udp数据包,返回客户端 natip natport .  如果和第一个端口返回的nat信息不同.则 说明时 symm nat.无法进行UDP穿越, 需要用服务器中转数据.
     *
     * 第四步：检测客户端NAT是否是Restricted Cone NAT还是Port Restricted Cone NAT？
     * 客户端建立UDP socket然后用这个socket向服务器的(IP-1,Port-1)发送数据包要求服务器用IP-1和一个不同于Port-1的端口发送一个UDP 数据包响应客户端, 客户端发送请求后立即开始接受数据包，
     * 无法接受到服务器的回应，则说明客户端是一个Port Restricted Cone NAT，如果能够收到服务器的响应则说明客户端是一个Restricted Cone NAT。以上两种NAT都可以进行UDP-P2P通信。
     *
     */
    //处理
    private void handlerMessage(SelectionKey key) {
        try {
            DatagramChannel sc = (DatagramChannel) key.channel();
            byteBuffer.clear();
            InetSocketAddress socketAddress = (InetSocketAddress) sc.receive(byteBuffer);
            byteBuffer.flip();
            byte tag = byteBuffer.get(0);
            if (tag == Command.UDPAuthentication.udp_auxiliaty){
                checkThread.updateTime = System.currentTimeMillis();
                if (remoteUdpServer == null || !remoteUdpServer.equals(socketAddress) ){
                    remoteUdpServer = socketAddress;
                    LOG.I("[UDP类型认证] 辅助服务器已连接 , "+ socketAddress);
                }

            }

            //客户端判断网关信息
            if (tag == Command.UDPAuthentication.client_query_nat_address){

                byte[] ipBytes = socketAddress.getAddress().getAddress();
                byte[] portBytes = Parse.int2bytes(socketAddress.getPort());
                byteBuffer.clear();
                byteBuffer.put(Command.UDPAuthentication.send_client_nat_address);
                byteBuffer.put(ipBytes);
                byteBuffer.put(portBytes);
                if (remoteUdpServer!=null){
                    ipBytes = remoteUdpServer.getAddress().getAddress();
                    portBytes = Parse.int2bytes(remoteUdpServer.getPort());
                    byteBuffer.put(ipBytes);
                    byteBuffer.put(portBytes);
                }
                byteBuffer.flip();
                sc.send(byteBuffer,socketAddress);
            }
            //判断nat - full cone
            if (tag == Command.UDPAuthentication.check_full_nat){
                if (remoteUdpServer!=null){
                    //通知认证辅助服务器,转发消息
                    byteBuffer.clear();
                    byteBuffer.put(Command.UDPAuthentication.turn_full_cone_check);
                    //目标客户端的ip+port
                    byteBuffer.put(socketAddress.getAddress().getAddress());
                    byteBuffer.put(Parse.int2bytes(socketAddress.getPort()));
                    byteBuffer.flip();
                    sc.send(byteBuffer,remoteUdpServer);
                }else{
                    LOG.I("UDP认证辅助服务器未连接.");
                }
            }
            //客户端判断 Restricted Cone NAT还是Port Restricted Cone NAT
            if (tag == Command.UDPAuthentication.check_restricted_nat){
                //使用另外一个端口回应
                byteBuffer.clear();
                byteBuffer.put(Command.UDPAuthentication.check_restricted_nat_resp);
                byteBuffer.flip();
                channel2.send(byteBuffer,socketAddress);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 检测UDP认证服务器的辅助服务器心跳是否过期
     */
    private class CheckAuxiliary extends Thread{
        public volatile long updateTime ;
        private final long timeSun = 1000 * 10;
        public CheckAuxiliary() {
            this.start();
        }

        @Override
        public void run() {
            while (isRun){
                if ( (System.currentTimeMillis() - updateTime) >=  timeSun){
                    if (remoteUdpServer!=null){
                        remoteUdpServer = null;
                        LOG.I("UDP认证辅助服务器断开连接 .");
                    }
                }
                synchronized (this){
                    try {
                        this.wait(timeSun);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }


}
