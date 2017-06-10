package protocol.imps.client;

import client.Threads.TClientLoad;
import client.Threads.TClientUp;
import client.Threads.Translate;
import client.obj.SerializeConnectTask;
import client.socketimp.PortManager;
import client.socketimp.SocketManager;
import protocol.Execute;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;
import utils.NetworkUtil;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by user on 2017/6/10.
 * 客户端收到服务器的UDP连接请求
 */
public class queryClientConnectUDPService implements Execute.IAction {
    @Override
    public void action(Intent intent) {
        try {
            LOG.I("客户端 - 收到服务器的UDP连接请求.");
            SocketManager manager = intent.getSourceManager();
            //连接UDP.
            HashMap<String,Object> map = intent.getMap();
            byte[] sourceBytes =  (byte[]) map.get(Parse._connectTaskBytes);
            SerializeConnectTask connectTask = (SerializeConnectTask) Parse.bytes2Sobj(sourceBytes);

            InetSocketAddress localSocket = new InetSocketAddress(
                    InetAddress.getByAddress(manager.info.getLocalAddress().getAddress().getAddress()),
                    PortManager.get().getPortToAdd()
            );

            InetSocketAddress serverSocket = new InetSocketAddress(
                    manager.info.getServerAddress().getAddress(),
                    connectTask.getServerTempUDP().getPort()
            );
            Translate translate = null;

            //判断开启 资源下载连接还是资源上传连接
            if (connectTask.getDestinationMac().equals( manager.info.getLocalString())) {
                //资源下载.
                translate = new Translate(Translate.HOLDER_CLIENT_DOWN);
            }else
            if (connectTask.getSourceMac().equals(manager.info.getLocalString())){
                //资源上传
                translate = new Translate(Translate.HOLDER_CLIENT_UP);
            }
            translate.setResource( connectTask.getSource());
            translate.setMac(manager.info.getLocalMac());
            translate.setLocalSocket(localSocket);
            translate.setServerSocket(serverSocket);
            translate.start();
            //结束
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
