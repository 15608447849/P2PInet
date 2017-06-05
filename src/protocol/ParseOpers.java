package protocol;

import java.util.HashMap;

import static protocol.Parse.*;

/**
 * Created by user on 2017/6/5.
 * 数据解析实现
 */
public class ParseOpers {

    protected static void selectTo(byte protocol,HashMap<String, Object> map, int length, byte[] data) throws Exception{
        if (protocol == Command.Client.heartbeat) {//心跳
            ParseOpers.hearbeat(map,data);
        }
        //同步资源
        if (protocol == Command.Client.synchronizationSource){
            synchronizedSource(map,data);
        }
        //服务器下发资源信息
        if (protocol == Command.Server.trunSynchronizationSource){
            trunSynchronizationSource(map,data);
        }
        if (protocol == Command.Client.connectSourceClient){
            connectSourceClient(map,data);
        }
    }

    /**
     * 客户端发起的搭桥请求
     * {connectTask对象}
     */
    private static void connectSourceClient(HashMap<String, Object> map, byte[] data) {
        map.put(_connectTaskBytes, data);
    }

    /**
     * 服务器下发资源,检查同步
     * {"source对象"}
     */
    private static void trunSynchronizationSource(HashMap<String, Object> map,byte[] data) {
        //复用方法 - 因为本来就转发
        synchronizedSource(map,data);
    }

    /**
     * 同步数据
     * {"source对象"}
     */
    private static void synchronizedSource(HashMap<String, Object> map,byte[] data) {
        map.put(_localSourceBytes, data);
    }


    /**
     * 解析 客户端心跳数据包
     * {客户端本机ip,本机端口,本机mac}
     */
    private static void hearbeat(HashMap<String, Object> map, byte[] data) {
        //  [ip,port,mac] - [4byte+4byte+6byte]
        byte[] ipBytes = new byte[4];
        System.arraycopy(data, 0, ipBytes, 0, 4);
        byte[] portBytes = new byte[4];
        System.arraycopy(data, 4, portBytes, 0, 4);
        byte[] macBytes = new byte[6];
        System.arraycopy(data, 8, macBytes, 0, 6);
        map.put(_ipBytes, ipBytes);
        map.put(_portInt, Parse.bytes2int(portBytes));
        map.put(_macBytes, macBytes);
    }
}
