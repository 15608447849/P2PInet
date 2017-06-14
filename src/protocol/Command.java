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
        /**
         * 认证
         */
        public static final byte authenticationSucceed = 10;
    }

    /**
     * 服务端发送 101-200
     */
    public static class Server{
        /**
         * 通知认证NET类型
         */
        public static final byte authenticationNetType = 100;
        /**
         * 转发资源同步请求
         *  {命令101,长度,资源需求主机MAC,资源名} -> 除这个mac之外的所有客户端
         */
        public static final byte turnSynchronizationSource = 101;
        /**
         * 请求客户端连接
         */
        public static final byte queryClientConnectUDPService = 102;
    }


    public static class UDPAuthentication{
        public static final byte client_query_nat_address = 65;
        public static final byte send_client_nat_address = 66;

        //检测类型 full net
        public static final byte check_full_nat = 68;
        public static final byte check_full_nat_resp = 69;

        //检测 port restricted nat
        public static final byte check_restricted_nat = 78;
        public static final byte check_restricted_nat_resp = 79;

        //udp辅助服务器发送的消息
        public static final byte udp_auxiliaty = 70;
        //udp检测 - 转发消息到 辅助服务器
        public static final byte turn_full_cone_check = 71;
    }

    public static class UDPTranslate{
        /**
         * 客户端发送心跳 - 包含客户端mac
         */
        public static final byte udpHeartbeat = 30;
        /**
         * 服务器回应
         */
        public static final byte serverHeartbeatResp = 31;
        /**
         * 客户端接受到服务器命令的回应
         */
        public static final byte clientReceiveResp = 32;
        /**
         * 客户端之间的握手包
         */
        public static final byte shakePackage = 33;
        /**
         * 握手回执
         */
        public static final byte shakePackage_resp = 34;
        /**
         * 检测MTU
         */
        public static final byte mtuCheck = 40;


    }

}
