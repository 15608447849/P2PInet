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
        byte[] portBytes = (byte[])intent.getMap().get(Parse._udpPort);
        int port = Parse.bytes2int(portBytes);
        InetSocketAddress address = new InetSocketAddress(manager.info.getServerAddress().getAddress(),port);
        try {
            new AuthenticationThread(address,manager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
