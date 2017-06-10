package protocol.imps.server;

import client.obj.SerializeSource;
import protocol.Execute;
import protocol.Intent;
import protocol.Parse;
import utils.LOG;

import java.util.HashMap;

/**
 * Created by user on 2017/6/2.
 * 同步资源
 * {2,长度,资源对象bytes}
 * 向 除Mac之外的所有客户端发送资源 (没有mac地址的客户端 - 跳过)
 *
 */
public class SynchronizationSource implements Execute.IAction{
    @Override
    public void action(Intent intent) {

        try {
            if (!intent.getServerCLI().isExist()){
                //拒绝
                LOG.I("同步资源请求 -> 拒绝 -> 客户端: "+intent.getServerCLI());
                return;
            }
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
