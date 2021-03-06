package protocol;

import client.socketimp.SocketManager;
import server.obj.CLI;
import utils.ClazzUtil;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/1.
 */
public class Execute {
    protected static final String clsPrefix = "protocol.imps.";
    private static final String motherName = "action";
    private static final Class[] classType = new Class[]{Intent.class};
    protected final HashMap<Byte,String> map = new HashMap();
    private final ReentrantLock lock = new ReentrantLock();

    /**处理客户端消息
     * 使用反射
     * */
    public boolean obtain(Intent intent){
        try{
            lock.lock();
            byte key = intent.getCommand();
//            LOG.I("协议编号:"+ key +" \n" + map );
            if (map.containsKey(key)){
                //反射构建对象并且调用方法 ,成功返回true
                String  clazzName = map.get(key);
                Object[] param = new Object[]{intent};
                ClazzUtil.createClazzInvokeMethod(clazzName,motherName,classType,param);
                return true;
            }
            return false;
        }finally {
            lock.unlock();
        }
    }
    /**
     * 客户端处理命令
     */
    static class Client extends Execute {
        private static final String suffix = "client.";
        private Client(){
            //认证net类型
            map.put(Command.Server.authenticationNetType,clsPrefix+suffix+"AuthenticationNet");
            //服务器下发同步资源
            map.put(Command.Server.turnSynchronizationSource,clsPrefix+suffix+"TurnSynchronizationSource");
            //客户端收到 服务器的UDP连接请求
            map.put(Command.Server.queryClientConnectUDPService,clsPrefix+suffix+"queryClientConnectUDPService");

//            LOG.I("客户端处理指令集合:"+map);
        }
       private static class Holder{
           private static Client instance = new Execute.Client();
       }
        public static Client get(){
           return Holder.instance;
        }
    }

    /**
     *服务端处理命令
     */
     static class Server extends Execute {
         private static final String suffix = "server.";
        private Server(){
            map.put(Command.Client.authenticationSucceed,clsPrefix+suffix+"AuthenticationSucceed");//tcp心跳
            map.put(Command.Client.heartbeat,clsPrefix+suffix+"Heartbeat");//tcp心跳
            map.put(Command.Client.synchronizationSource,clsPrefix+suffix+"SynchronizationSource");//资源同步
            map.put(Command.Client.connectSourceClient,clsPrefix+suffix+"ConnectSourceClient");//请求建立连接
//            LOG.I("服务器处理指令集合:"+map);
        }
        private static class Holder{
            private static Server instance = new Execute.Server();
        }
        public static Server get(){
            return Holder.instance;
        }
    }
    public interface IAction{
        void action(Intent intent);
    }


    public static final int SERVER = 0;
    public static final int CLIENT = 1;

    //处理命令
    public static boolean handlerMessage(int type,Object... objects){
        if (type == SERVER){
            return clientMessage(objects);
        }
        if (type == CLIENT){
            return serverMessage(objects);
        }
        return false;
    }

    /**
     * 客户端处理服务器发来的消息
     * @param objects
     * @return
     */
    private static boolean serverMessage(Object[] objects) {
        HashMap<String, Object> map = (HashMap<String, Object>) objects[0];
        byte command = (byte) map.get(Parse._protocol);//命令
        SocketManager socketManager = (SocketManager) objects[1];//管理器
        Intent intent = new Intent();
        intent.putCommand(command);
        intent.putMap(map);
        intent.putSocketManager(socketManager);
        return Execute.Client.get().obtain(intent);
    }

    /**
     * 服务器处理客户端发来的消息
     * @param objects
     * @return
     */
    private static boolean clientMessage(Object[] objects) {

        HashMap<String, Object> map = (HashMap<String, Object>) objects[0];
        byte command = (byte) map.get(Parse._protocol);//命令
        CLI client = (CLI) objects[1];
        Intent intent = client.getIntent();
            intent.putCommand(command);
            intent.putMap(map);
        return Execute.Server.get().obtain(intent);
    }







}
