package client.Threads;

import client.obj.Info;
import client.socketimp.PortManager;
import client.socketimp.SocketManager;
import protocol.Command;
import protocol.Parse;
import utils.LOG;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import static utils.NetworkUtil.*;

/**
 * Created by user on 2017/6/8.
 */
public class AuthenticationThread extends Thread{

    private static final int CHECK_NAT_INFO = 1;
    private static final int CHECK_FULL_CONE = 2;
    private static final int CHECK_SYMMETRIC = 3;
    private static final int CHECK_RESTRICTED_CONE = 4;
    private static final int COUNT_INIT = 0;
    private static final int OVER_TIME_COUNT = 15;
    private static final int EXIT_COUNT = -1;


    public DatagramChannel channel;
    public InetSocketAddress server1Address;
    public InetSocketAddress server2Address;

    public InetSocketAddress local;
    private int temp = CHECK_NAT_INFO;
    private ByteBuffer byteBuffer;
    private SocketManager manager;
    private Info info;
    private int count = COUNT_INIT;//超时计数
    private InetSocketAddress nat1MapperAddress;//端口1 在nat上的映射
    private InetSocketAddress nat2MapperAddress;//端口2 在nat上的映射


    public AuthenticationThread(InetSocketAddress address,SocketManager manager) throws IOException {
        this.server1Address = address;
        this.manager = manager;
        this.info = manager.info;
        int localPort = PortManager.get().getPort();
        this.local = new InetSocketAddress(info.getLocalAddress().getAddress(),localPort);
        channel = DatagramChannel.open().bind(local);
        channel.configureBlocking(false);
        byteBuffer = ByteBuffer.allocate(1+4+4+4+4);
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
            while (count!=EXIT_COUNT && count<= OVER_TIME_COUNT){
                count++;
                sendMessage();
                receiveMessage();
                waitLoop();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        LOG.I("结束认证. ");
        close();
        if (info.isNatAuthentic()){
            //通知客户端已认证.
            manager.commander.sendAuthenticationSucceed();
        }
    }

    private void close() {
        try {
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //发送消息
    private void sendMessage() throws IOException {
        if (temp== CHECK_NAT_INFO){sendNetInfo();}
        if (temp == CHECK_FULL_CONE){sendFullCheck();}
        if (temp == CHECK_SYMMETRIC){sendSymmetricCheck();}
        if (temp == CHECK_RESTRICTED_CONE){  sendPortRestrictedCheck();}
    }

    //发送 NAT 信息
    private void sendNetInfo() throws IOException {
        LOG.I("检测是否在NAT后.");
        byteBuffer.clear();
        byteBuffer.put(Command.UDPAuthentication.client_query_nat_address);
        byteBuffer.flip();
        channel.send(byteBuffer, server1Address);
        LOG.I("请求服务器发送 nat信息., address: "+server1Address);
    }

    //检测 full core net
    private void sendFullCheck() throws IOException {
        LOG.I("检测FULL CONE NAT.");
        byteBuffer.clear();
        byteBuffer.clear();
        byteBuffer.put(Command.UDPAuthentication.check_full_nat);
        byteBuffer.flip();
        channel.send(byteBuffer, server1Address);
        LOG.I("请求服务器检测 full nat 检测结果. address: "+server1Address);
    }

    private void sendSymmetricCheck() throws IOException  {
        LOG.I("检测SYMMETRIC NAT.");
            byteBuffer.clear();
            byteBuffer.put(Command.UDPAuthentication.client_query_nat_address);
            byteBuffer.flip();
            channel.send(byteBuffer,server2Address);
            LOG.I("请求服务器检测 symmetric nat, address: " + server2Address);

    }
    private void sendPortRestrictedCheck() throws IOException {
        LOG.I("检测PORT RESTRICTED NAT.");
        byteBuffer.clear();
        byteBuffer.put(Command.UDPAuthentication.check_restricted_nat);
        byteBuffer.flip();
        channel.send(byteBuffer,server1Address);
        LOG.I("请求服务器检测 symmetric nat, address: " + server1Address);
    }











    /**接受消息*/
    private void receiveMessage() throws IOException{
        byteBuffer.clear();
        SocketAddress address = channel.receive(byteBuffer);
        if (address!=null){
            count = COUNT_INIT;//重置超时计数
            byteBuffer.flip();
            byte tag = byteBuffer.get(0);
            if (tag==Command.UDPAuthentication.send_client_nat_address){
                byte[] ipByte = new byte[4];
                byte[] portByte = new byte[4];
                byteBuffer.position(1);
                byteBuffer.get(ipByte);
                byteBuffer.position(5);
                byteBuffer.get(portByte);
                if (temp == CHECK_NAT_INFO){ //检测是否存在NAT.
                    nat1MapperAddress = new InetSocketAddress(InetAddress.getByAddress(ipByte), Parse.bytes2int(portByte));
                    if (nat1MapperAddress.equals(local)){
                        LOG.I("客户端不在NAT之后.");
                        info.natType = NotNat;
                        count=-1;
                    }else{
                        LOG.I("内网: "+ local +"-----> NAT: "+ nat1MapperAddress +"\n 请求服务器检测NAT的类型.");
                        if (byteBuffer.limit()>9){
                            //接收第二个服务器IP地址.-
                            byteBuffer.position(9);
                            byteBuffer.get(ipByte);
                            byteBuffer.position(13);
                            byteBuffer.get(portByte);
                            server2Address =  new InetSocketAddress(InetAddress.getByAddress(ipByte), Parse.bytes2int(portByte));
                            LOG.I("UDP认证辅助服务器: "+ server2Address);
                            temp = CHECK_FULL_CONE;//判断net类型->full检测
                        }else{
                            count=-1;
                            LOG.I("服务器无法检测NAT类型.");
                        }
                    }
                }

                if (temp == CHECK_SYMMETRIC){ //检测NAT类型 - symmetric
                    nat2MapperAddress = new InetSocketAddress(InetAddress.getByAddress(ipByte), Parse.bytes2int(portByte));
                    LOG.I("nat mapper 1 = " + nat1MapperAddress);
                    LOG.I("nat mapper 2 = " + nat2MapperAddress);
                    if (nat1MapperAddress.equals(nat2MapperAddress)){
                        //继续检测 Restricted_Cone_NAT 和 Port_Restricted_Cone_NAT
                        temp = CHECK_RESTRICTED_CONE;
                    }else{
                        LOG.I("客户端是Nat类型:SYMMETRIC");
                        info.natType = Symmetric_NAT;
                        count=-1;
                    }
                }
            }

            if (tag == Command.UDPAuthentication.check_full_nat_resp){
                info.natType = Full_Cone_NAT;
                LOG.I("客户端Nat类型:FUll CONE");
                count = -1;
            }
            if (tag == Command.UDPAuthentication.check_restricted_nat_resp){
                info.natType = Restricted_Cone_NAT;
                LOG.I("客户端Nat类型:RESTRICTED CONE");
                count = -1;
            }
        }else{
            LOG.I("超时次数  - "+count);
            if (count == OVER_TIME_COUNT){
                if (temp == CHECK_NAT_INFO){
                    LOG.I("服务器无响应 或 NAT规则阻止一切UDP通讯.");
                }
                if (temp == CHECK_FULL_CONE){
                    count = COUNT_INIT;
                    temp = CHECK_SYMMETRIC;//检测symmetric类型
                    LOG.I("检测FULL CONE失败.");
                }
                if (temp == CHECK_SYMMETRIC){
                    LOG.I("服务器检测SYMMETRIC无响应.");
                }
                if (temp == CHECK_RESTRICTED_CONE){
                    info.natType = Port_Restricted_Cone_NAT;
                    LOG.I("客户端Nat类型:PORT RESTRICTED CONE");
                }
            }
        }
    }










}
