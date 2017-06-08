package client.Threads;

import client.obj.Info;
import client.socketimp.PortManager;
import client.socketimp.SocketManager;
import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.NetUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Created by user on 2017/6/8.
 */
public class AuthenticationThread extends Thread{

    public DatagramChannel channel;
    public InetSocketAddress server_p1;
    public InetSocketAddress server_p2;
    public InetSocketAddress local;
    private int temp = 0;
    private ByteBuffer byteBuffer;
    private SocketManager manager;
    private Info info;
    private int count = 1;//超时计数
    private InetSocketAddress nat1address;
    private InetSocketAddress nat2address;
    public AuthenticationThread(InetSocketAddress address1,InetSocketAddress address2,SocketManager manager) throws IOException {
        this.server_p1 = address1;
        this.server_p2 = address2;
        this.manager = manager;
        this.info = manager.info;
        int localPort = PortManager.get().getPort();
        this.local = new InetSocketAddress(info.getLocalAddress().getAddress(),localPort);
        channel = DatagramChannel.open().bind(local);
        channel.configureBlocking(false);
        byteBuffer = ByteBuffer.allocate(9);
        start();
    }

    //超时等待
    private void waitLoop() {
        synchronized (this){
            try {
                this.wait(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        LOG.I("开始认证NET信息. ");
        try {
            while (count>0 && count<=10){
                sendMessage();
                receiveMessage();
                waitLoop();
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.I("结束认证. ");
        close();
        //通知客户端已认证.
        manager.commander.sendAuthenticationSucceed();
    }

    private void close() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage() throws IOException {
        if (temp==0){
            sendNetInfo();
        }
        if (temp == 1){
            LOG.I("等待客户端 nat信息.");
        }
        if (temp == 2){
            sendFullCheck();
        }
        if (temp == 3){ LOG.I("等待客户端 full type 检测信息.");}
        if (temp == 4){
            sendSymmetricCheck();
        }

    }




    //接受消息
    private void receiveMessage() throws IOException{


        byteBuffer.clear();
        SocketAddress address = channel.receive(byteBuffer);
        if (address!=null){
            count = 0;//重置超时计数
            byteBuffer.flip();
            byte tag = byteBuffer.get(0);
            if (tag==Command.UDPAuthentication.send_client_nat_address){
                byte[] ipByte = new byte[4];
                byte[] portByte = new byte[4];
                byteBuffer.position(1);
                byteBuffer.get(ipByte);
                byteBuffer.position(5);
                byteBuffer.get(portByte);
                if (temp == 1){
                    nat1address = new InetSocketAddress(InetAddress.getByAddress(ipByte), Parse.bytes2int(portByte));
                    if (nat1address.equals(local)){
                        LOG.I("客户端没有NET.");
                        info.setNetType(Info.No_Net);
                        count=-1;
                    }else{
                        LOG.I("内网信息: "+ local +" net信息: "+nat1address);

                        temp = 2;//判断net类型->full检测
                    }
                }
                if (temp == 4){
                    nat2address = new InetSocketAddress(InetAddress.getByAddress(ipByte), Parse.bytes2int(portByte));
                    LOG.I("nat mapper 1 = " +nat1address);
                    LOG.I("nat mapper 2 = " +nat2address);
                    if (nat1address.equals(nat2address)){
                        LOG.I("客户端不是Symmetric nat");
                        info.setNetType(Info.Restricted_Cone_NAT_or_Port_Restricted_Cone_NAT);
                    }else{
                        LOG.I("客户端是Symmetric nat");
                        info.setNetType(Info.Symmetric_NAT);
                    }
                    count=-1;
                    LOG.I("结束检测.");
                }
            }

            if (tag == Command.UDPAuthentication.check_full_nat_resp){
                info.setNetType(Info.Full_Cone_NAT);
                LOG.I("客户端Nat类型: FUll CONE NAT");
                count = -1;
            }

        }else{
            LOG.I("超时  - "+count);
            if (temp==3){
                //等待接收
                if (count==10) {
                    //代表不是full net.
                    LOG.I("客户端nat 不是 FUll CONE NAT.");
                    count = 0;
                    //继续下一步
                    temp = 4;
                }
            }
        }
    }




    //发送 NET 信息
    private void sendNetInfo() throws IOException {
        byteBuffer.clear();
        byteBuffer.put(Command.UDPAuthentication.client_query_nat_address);
        byteBuffer.flip();
        channel.send(byteBuffer,server_p1);
        temp = 1; //等待net信息
        LOG.I("请求服务器发送 nat信息.");
    }
    //检测 full core net
    private void sendFullCheck() throws IOException {
        byteBuffer.clear();
        byteBuffer.clear();
        byteBuffer.put(Command.UDPAuthentication.check_full_nat);
        byteBuffer.flip();
        channel.send(byteBuffer,server_p1);
        temp = 3; //等待full消息检测.
        LOG.I("请求服务器检测 full nat 检测结果.");
    }
    private void sendSymmetricCheck() throws IOException  {
        byteBuffer.clear();
        byteBuffer.put(Command.UDPAuthentication.client_query_nat_address);
        byteBuffer.flip();
        channel.send(byteBuffer,server_p2);
        LOG.I("请求服务器检测 symmetric nat.");
    }
}
