package protocol.imps.server;

import client.obj.SerializeConnectTask;
import protocol.Execute;
import protocol.Intent;
import protocol.Parse;
import server.abs.IThreadInterface;
import utils.LOG;

import java.util.HashMap;

/**
 * Created by user on 2017/6/5.
 * 客户端发起的搭桥请求
 * {connectTask对象}
 */
public class ConnectSourceClient implements Execute.IAction {
    @Override
    public void action(Intent intent) {
        try {
            HashMap<String,Object> map = intent.getMap();
            byte[] objBytes = (byte[]) map.get(Parse._connectTaskBytes);
            SerializeConnectTask connTask = (SerializeConnectTask) Parse.bytes2Sobj(objBytes);
            LOG.I("建立点对点连接请求: ["+ connTask.getDownloadHostMac()+ "] ============> ["+ connTask.getUploadHostMac()+"] ,"+"数据连接对象大小: "+ objBytes.length);
            IThreadInterface udpManager = (IThreadInterface) intent.getOperate().getServer().getParam("udp");
            udpManager.putNewTask(connTask);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
