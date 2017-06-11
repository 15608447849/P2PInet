package protocol.imps.client;

import client.Threads.Translate;
import client.obj.SerializeConnectTask;
import client.socketimp.PortManager;
import client.socketimp.SocketManager;
import protocol.Execute;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;

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
                    connectTask.getServerTempAddress().getPort()
            );
            Translate translate = null;

            //判断开启 资源下载连接还是资源上传连接
            if (connectTask.getUploadHostMac().equals(manager.info.getLocalMacString())) {
                //资源下载.
                translate = new Translate(Translate.HOLDER_CLIENT_UP);
            }else
            if (connectTask.getDownloadHostMac().equals(manager.info.getLocalMacString())){
                //资源上传
                translate = new Translate(Translate.HOLDER_CLIENT_DOWN);
            }

            if (translate==null) return;
            LOG.I("客户端 - 收到服务器的UDP连接请求,分配角色: "+ translate.getHolderTypeName());
            translate.setResource( connectTask.getSource());
            translate.setMac(manager.info.getLocalMac());
            translate.setLocalSocket(localSocket);
            translate.setServerSocket(serverSocket);
            translate.setSourceManager(manager.sourceManager);
            translate.start();
            //结束
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
