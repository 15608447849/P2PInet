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
            synchronizedSource(map,length,data);
        }
    }

    /**
     * 同步数据
     */
    private static void synchronizedSource(HashMap<String, Object> map, int length, byte[] data) {
        byte[] macBytes = new byte[6];
        System.arraycopy(data, 0, macBytes, 0, 6);
        byte[] sourceBytes = new byte[length-6];
        System.arraycopy(data, 6, sourceBytes, 0, length-6);

        map.put(_macBytes, macBytes);
        map.put(_localSourceBytes, sourceBytes);
    }


    /**
     * 解析 客户端心跳数据包
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
