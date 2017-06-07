package protocol.imps.client;

import client.Threads.Translate;
import client.Threads.ClientA;
import client.obj.SerializeConnectTask;
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
 * 服务器请求 资源客户端 建立UDP连接 .
 *  单独开启一个线程>开始连接UDP, 服务器UDP信息在对象中传递
 */
public class QueryConnectUdp_source implements IAction {
    @Override
    public void action(Intent intent) {
        try{
            HashMap<String,Object> map = intent.getMap();

            byte[] sourceBytes =  (byte[]) map.get(Parse._connectTaskBytes);
            SerializeConnectTask connectTask = (SerializeConnectTask) Parse.bytes2Sobj(sourceBytes);
            LOG.I("资源客户端收到服务器的UDP连接请求:[ "+connectTask +" ]");
            if (connectTask.getCompele() == 1){
                //随机分配一个未使用的端口
                int port = PortManager.get().getPortToAdd();
                SocketManager manager = intent.getSourceManager();
                byte[] ip = manager.info.getLocalAddress().getAddress().getAddress();
                InetSocketAddress localSocket = new InetSocketAddress(InetAddress.getByAddress(ip),port);
                InetSocketAddress serverSocket = connectTask.getServerTempUDP();
                Translate tanslate = new Translate();
                    tanslate.setMac(manager.info.getLocalMac());
                    tanslate.setLocalSokcet(localSocket);
                    tanslate.setServerSocket(serverSocket);
                new ClientA(manager.info.getLocalMac(),localSocket,serverSocket);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
