package protocol;

/**
 * Created by user on 2017/5/31.
 * [数据类型,数据长度,数据块]
 * byte,int,object
 */
public class Command {
    /**
     * 客户端发送 1-100
     * 客户端-客户端交互
     * 201-256
     */
    public static class Client{
        /**
         * 心跳
         * {命令1,数据长度,客户端本地ip,客户端本地端口,客户端mac字节}
         *  1.更新当前时间毫秒数 (毫秒数> 30 * 1000) ,断开连接
         */
        public static final byte heartbeat = 1;
        /**
         * 通知-同步资源
         * sync
         *{命令2,数据长度,Source对象bytes}
         *
         */
        public static final byte synchronizationSource = 2;
        /**
         * 资源请求者请求通讯服务器,对资源源 发起连接,(搭桥开始)
         * {命令3,数据长度,connectTask对象}
         */
        public static final byte connectSourceClient = 3;
    }

    /**
     * 服务端发送 101-200
     */
    public static class Server{
        /**
         * 转发资源同步请求
         *  {命令101,长度,资源需求主机MAC,资源名} -> 除这个mac之外的所有客户端
         */
        public static final byte trunSynchronizationSource = 101;
    }
}
