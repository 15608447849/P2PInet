package protocol.imps.client;

import client.Threads.AuthenticationThread;
import client.socketimp.SocketManager;
import protocol.Command;
import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;
import utils.MD5Util;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/6/8.
 */
public class AuthenticationNet implements Excute.IAction {
    @Override
    public void action(Intent intent) {
        SocketManager manager = intent.getSourceManager();
        //会收到 服务器 udp 端口号
        byte[] portBytes = (byte[])intent.getMap().get(Parse._udpPort1);
        int port1 = Parse.bytes2int(portBytes);
        portBytes = (byte[])intent.getMap().get(Parse._udpPort2);
        int port2 = Parse.bytes2int(portBytes);
        InetSocketAddress address1 = new InetSocketAddress(manager.info.getServerAddress().getAddress(),port1);
        InetSocketAddress address2 = new InetSocketAddress(manager.info.getServerAddress().getAddress(),port2);

        try {
            new AuthenticationThread(address1,address2,manager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
