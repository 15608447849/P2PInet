package protocol.imps.server;

import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import server.obj.ServerCLI;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by user on 2017/6/1.
 * 服务器 -> 客户端
 *
 * 1.获取客户端信息
 * 2.本地查询是否在列表 ,不在添加
 * 3.返回信息
 *
 */
public class Heartbeat implements Excute.IAction {
    @Override
    public void action(Intent intent) {
        try {
        HashMap<String,Object> map = intent.getMap();
        //获取客户端 ip
        byte[] ipBytes = (byte[]) map.get(Parse._ipBytes);
        //获取客户端 port
            int port  =  (int) map.get(Parse._portInt);
        //获取客户端 mac
        byte[] macBytes =  (byte[]) map.get(Parse._macBytes);
        InetAddress address = InetAddress.getByAddress(ipBytes);
        InetSocketAddress clientLocalAddress = new InetSocketAddress(address,port);
        ServerCLI client = intent.getServerCLI();
        client.setLocal(clientLocalAddress,macBytes);
        intent.getOperate().putCLI(client);//更新客户端信息
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
