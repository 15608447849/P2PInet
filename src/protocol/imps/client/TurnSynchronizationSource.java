package protocol.imps.client;

import client.obj.SerializeConnectTask;
import client.obj.SerializeSource;
import client.socketimp.SocketManager;
import protocol.Execute;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;

import java.util.HashMap;

/**
 * Created by user on 2017/6/5.
 * 服务器通知客户端 检查是否存在 某资源,不存在进行同步
 */
public class TurnSynchronizationSource implements Execute.IAction{
    @Override
    public void action(Intent intent) {
        try {
            HashMap<String,Object> map = intent.getMap();

            byte[] sourceBytes =  (byte[]) map.get(Parse._localSourceBytes);
            SerializeSource source = (SerializeSource) Parse.bytes2Sobj(sourceBytes);

            SocketManager manager = intent.getSourceManager();
            boolean flag = manager.sourceManager.ergodicResource(source);
            if (flag) return;
            //不存在,请求服务器 同步资源
            //创建连接对象
            SerializeConnectTask connTask = new SerializeConnectTask(source);
            //发送到服务器
            manager.commander.connectSourceClient(connTask);
            LOG.I("收到同步请求:[ "+source +" ] ,创建同步任务成功.已发送服务器." );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
