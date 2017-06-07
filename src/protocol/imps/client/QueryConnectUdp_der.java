package protocol.imps.client;

import client.Threads.Translate;
import client.Threads.ClientB;
import client.obj.SerializeConnectTask;
import client.obj.SerializeSource;
import client.socketimp.PortManager;
import client.socketimp.SocketManager;
import protocol.Excute.IAction;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by user on 2017/6/6.
 */
public class QueryConnectUdp_der implements IAction {
    @Override
    public void action(Intent intent) {
        try{
            LOG.I("索求资源客户端(clientB) 收到服务器的UDP连接请求.");
            HashMap<String,Object> map = intent.getMap();
            byte[] sourceBytes =  (byte[]) map.get(Parse._connectTaskBytes);
            SerializeConnectTask connectTask = (SerializeConnectTask) Parse.bytes2Sobj(sourceBytes);
            if (connectTask.getCompele() == 2){
                // 随机分配一个未使用的端口
                int port = PortManager.get().getPortToAdd();
                SocketManager manager = intent.getSourceManager();
                byte[] ip = manager.info.getLocalAddress().getAddress().getAddress();
                InetSocketAddress localSocket = new InetSocketAddress(InetAddress.getByAddress(ip),port);
                InetSocketAddress serverSocket = connectTask.getServerTempUDP();
                InetSocketAddress sourceSocket = connectTask.getSrcNET();
                SerializeSource source = connectTask.getSource();
                Translate translate = new Translate(Translate.HOLDER_CLIENT_B);
                translate.setMac(manager.info.getLocalMac());
                translate.setLocalSokcet(localSocket);
                translate.setServerSocket(serverSocket);
                translate.setTerminalSocket(sourceSocket);
                translate.setResource(source);
                translate.checkServerIp(manager.info.getServerAddress());
                new ClientB(translate);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
