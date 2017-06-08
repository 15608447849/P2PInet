package protocol.imps.server;

import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import server.obj.CLI;
import server.obj.IParameter;
import utils.LOG;

import java.util.HashMap;

/**
 * Created by user on 2017/6/1.
 * 客户端TCP心跳
 */
public class Heartbeat implements Excute.IAction {
    @Override
    public void action(Intent intent) {
        try {
        HashMap<String,Object> map = intent.getMap();
        //获取客户端 mac
        byte[] macBytes =  (byte[]) map.get(Parse._macBytes);
        //更新客户端信息
        CLI client = intent.getServerCLI();
        client.setMacAddress(macBytes);
        client.updateTime();
        if (!client.isAuthentication() && !client.isSendAuthentication()){
            IParameter parameter = intent.getIparam();
            int port1 = parameter.udpLocalAddress1.getPort();
            int port2 = parameter.udpLocalAddress2.getPort();
            //通知认证net类型
            client.getWrite().notifyAuthentication(Parse.int2bytes(port1),Parse.int2bytes(port2));
            client.setAuthentication(1);
        }
        if (!client.isExist()){
                //不在队列中,添加到队列
                intent.getOperate().putCLI(client);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
