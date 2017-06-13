package protocol.imps.client;

import client.threads.AuthenticationThread;
import client.socketimp.SocketManager;
import protocol.Execute;
import protocol.Intent;
import protocol.Parse;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by user on 2017/6/8.
 */
public class AuthenticationNet implements Execute.IAction {
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
