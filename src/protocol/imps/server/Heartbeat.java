package protocol.imps.server;

import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import server.obj.CLI;
import server.obj.IParameter;

/**
 * Created by user on 2017/6/1.
 * 客户端TCP心跳
 */
public class Heartbeat implements Excute.IAction {
    @Override
    public void action(Intent intent) {
        try {
        //更新客户端时间
        CLI client = intent.getServerCLI();
        client.updateTime();
        if (!client.isAuthentication() && !client.isSendAuthentication()){
            IParameter parameter = intent.getIparam();
            int port = parameter.udpLocalAddress_Main.getPort();
            //通知认证net类型
            client.getWrite().notifyAuthentication(Parse.int2bytes(port));
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
