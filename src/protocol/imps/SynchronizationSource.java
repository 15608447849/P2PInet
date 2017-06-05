package protocol.imps;

import client.obj.SerializeSource;
import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import protocol.ParseOpers;
import server.obj.ServerCLI;
import utils.LOG;
import utils.NetUtil;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;

/**
 * Created by user on 2017/6/2.
 * 同步资源
 * {2,长度,mac,资源对象bytes}
 * 向 除Mac之外的所有客户端发送资源 (没有mac地址的客户端 - 跳过)
 *
 */
public class SynchronizationSource implements Excute.IAction{
    @Override
    public void action(Intent intent) {

        try {
            HashMap<String,Object> map = intent.getMap();
            byte[] macBytes =  (byte[]) map.get(Parse._macBytes);
            byte[] sourceBytes =  (byte[]) map.get(Parse._localSourceBytes);
            SerializeSource source = (SerializeSource) Parse.bytes2Sobj(sourceBytes);
            LOG.I(NetUtil.macByte2String(macBytes)+ " 要求同步资源 : [ "+source +" ]");
           //转发同步资源到其他客户端
            intent.getOperate().turnSynchronizationSource(macBytes,sourceBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
