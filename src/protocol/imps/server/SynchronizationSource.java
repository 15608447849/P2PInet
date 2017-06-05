package protocol.imps.server;

import client.obj.SerializeSource;
import protocol.Excute;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;
import utils.NetUtil;

import java.util.HashMap;

/**
 * Created by user on 2017/6/2.
 * 同步资源
 * {2,长度,资源对象bytes}
 * 向 除Mac之外的所有客户端发送资源 (没有mac地址的客户端 - 跳过)
 *
 */
public class SynchronizationSource implements Excute.IAction{
    @Override
    public void action(Intent intent) {

        try {
            HashMap<String,Object> map = intent.getMap();
            byte[] sourceBytes =  (byte[]) map.get(Parse._localSourceBytes);
            SerializeSource source = (SerializeSource) Parse.bytes2Sobj(sourceBytes);
            byte[] macBytes =  source.getInitiatorMacAddress();
            LOG.I("同步资源请求 : [ "+source +" ]");
           //转发同步资源到其他客户端
            intent.getOperate().turnSynchronizationSource(macBytes,sourceBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }
}
