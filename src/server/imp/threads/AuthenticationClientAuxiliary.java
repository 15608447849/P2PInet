package server.imp.threads;

import protocol.Command;
import protocol.Parse;
import server.abs.IServer;
import server.abs.IThread;
import server.obj.IParameter;
import utils.LOG;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Selector;

/**
 * Created by user on 2017/6/9.
 * UDP认证辅助服务器
 */
public class AuthenticationClientAuxiliary extends IThread{
    private InetSocketAddress localAddress;
    private InetSocketAddress serverAddress;
    private DatagramChannel channel;
    private ByteBuffer buffer;
    private SendUDPMessageThread sths;
    public AuthenticationClientAuxiliary(IServer server) {
        super(server);
        IParameter param = (IParameter) server.getParam("param");
        localAddress = param.udpLocalAddress_Sec;
        serverAddress = param.udpLocalAddress_Main;
        launch();
    }
    @Override
    protected void action() {
        try {
            channel = DatagramChannel.open().bind(localAddress);
            channel.configureBlocking(false);
            buffer = ByteBuffer.allocate(1+4+4); // 最大: 协议, 指定IP,指定端口.
            LOG.I("启动 UDP认证 辅助服务器 ,本地地址 - :"+localAddress);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sths = new SendUDPMessageThread();

        while (isRun){
          receiveUdpMessage();
        }


    }

    private void receiveUdpMessage() {
        try {
            buffer.clear();
            InetSocketAddress address = (InetSocketAddress) channel.receive(buffer);
            if (address != null) {
                buffer.flip();
                byte command = buffer.get(0);
                if (command == Command.UDPAuthentication.turn_full_cone_check){
                    LOG.I("UDP认证辅助服务器 收到 "+address+" 得转发任务.");
                    byte[] ipBytes = new byte[4];
                    buffer.position(1);
                    buffer.get(ipBytes);
                    byte[] portBytes = new byte[4];
                    buffer.position(5);
                    buffer.get(portBytes);
                    address = new InetSocketAddress(InetAddress.getByAddress(ipBytes), Parse.bytes2int(portBytes));
                    buffer.clear();
                    buffer.put(Command.UDPAuthentication.check_full_nat_resp);
                    buffer.flip();
                    channel.send(buffer,address);
                    LOG.I("转发目标: "+ address+ " "+ buffer+"  消息已发送.");
                }
                if (command == Command.UDPAuthentication.client_query_nat_address){
                    byte[] ipBytes = address.getAddress().getAddress();
                    byte[] portBytes = Parse.int2bytes(address.getPort());
                    buffer.clear();
                    buffer.put(Command.UDPAuthentication.send_client_nat_address);
                    buffer.put(ipBytes);
                    buffer.put(portBytes);
                    buffer.flip();
                    channel.send(buffer,address);
                    LOG.I("UDP认证辅助服务器 收到 "+address+" 请求.返回对方Nat地址." + buffer +" 已发送.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class SendUDPMessageThread extends Thread{
        private final ByteBuffer buf = ByteBuffer.allocate(1);

        public SendUDPMessageThread() {
            buf.clear();
            buf.put(Command.UDPAuthentication.udp_auxiliaty);
            buf.flip();
            this.start();
        }

        @Override
        public void run() {
            while (isRun){
                //发送消息到服务器
                synchronized (this){
                    if (channel!=null && channel.isOpen()){
                            buf.rewind();
                        try {
                            channel.send(buf,serverAddress);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        this.wait(1000*30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }








}
