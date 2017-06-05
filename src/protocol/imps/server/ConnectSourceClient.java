package protocol.imps.server;

import client.obj.SerializeConnectTask;
import client.obj.SerializeSource;
import client.socketimp.SocketManager;
import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;

import java.util.HashMap;

/**
 * Created by user on 2017/6/5.
 * 客户端发起的搭桥请求
 * {connectTask对象}
 */
public class ConnectSourceClient implements Excute.IAction {
    @Override
    public void action(Intent intent) {
        try {
            HashMap<String,Object> map = intent.getMap();
            SerializeConnectTask connTask = (SerializeConnectTask) Parse.bytes2Sobj((byte[]) map.get(Parse._connectTaskBytes));
            LOG.I("建立连接请求: "+ connTask.getSourceMac()+ " -> "+ connTask.getDestinationMac());

            //打开一个随机端口...


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
