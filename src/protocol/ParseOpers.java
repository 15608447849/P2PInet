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

        }
        if (protocol == Command.Client.authenticationSucceed){//客户端认证成功
            authenticationSuccessed(map,data);
        }
        if (protocol == Command.Server.authenticationNetType){
            //认证
            authentication(map,data);
        }
        //同步资源
        if (protocol == Command.Client.synchronizationSource){
            synchronizedSource(map,data);
        }
        //服务器下发资源信息
        if (protocol == Command.Server.trunSynchronizationSource){
            turnSynchronizationSource(map,data);
        }
        //资源需求客户端 请求 服务器 建立与 资源源 的连接
        if (protocol == Command.Client.connectSourceClient){
            connectSourceClient(map,data);
        }
        //资源源 收到服务器发送的 UDP端口信息,建立连接
        if (protocol == Command.Server.queryConnectUdp_source){
            queryConnectUdp_source(map,data);
        }
        //索求资源客户端 收到 服务器发来 的 udp连接请求,建立UDP连接
        if (protocol == Command.Server.queryConnectUdp_der){
            queryConnectUdp_der(map,data);
        }
    }

    /**
     * 客户端认证,收到UDP端口
     */
    private static void authentication(HashMap<String, Object> map, byte[] data) {
        map.put(_udpPort,data);
    }
    /**
     * 客户端认证成功
     * */
    private static void authenticationSuccessed(HashMap<String, Object> map, byte[] data){
        //  [mac,nat-type] - [6+4]
        byte[] macBytes = new byte[6];
        System.arraycopy(data, 0, macBytes, 0, 6);
        byte[] natTypeBytes = new byte[4];
        System.arraycopy(data, 6, natTypeBytes, 0, 4);
        map.put(_macBytes, macBytes);
        map.put(_natTypeBytes,natTypeBytes);
    }
    /**
     * 索求资源客户端收到服务器的连接请求
     */
    private static void queryConnectUdp_der(HashMap<String, Object> map, byte[] data) {
        map.put(_connectTaskBytes,data);
    }

    /**
     * 资源客户端收到服务器的连接请求
     */
    private static void queryConnectUdp_source(HashMap<String, Object> map, byte[] data) {
        map.put(_connectTaskBytes,data);
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
    private static void turnSynchronizationSource(HashMap<String, Object> map, byte[] data) {
        //复用方法 - 因为本来就转发
        map.put(_localSourceBytes, data);
    }

    /**
     * 同步数据
     * {"source对象"}
     */
    private static void synchronizedSource(HashMap<String, Object> map,byte[] data) {
        map.put(_localSourceBytes, data);
    }



}
