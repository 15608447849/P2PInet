package protocol;

import utils.ClazzUtil;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/1.
 */
public class Excute {
    protected static final String clsPrefix = "protocol.imps.";
    private static final String motherName = "action";
    private static final Class[] classType = new Class[]{Intent.class};
    protected final HashMap<Byte,String> map = new HashMap();
    private final ReentrantLock lock = new ReentrantLock();
    public boolean obtain(byte key,Intent intent){
        try{
            lock.lock();
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
    public static class Client extends Excute{
        private Client(){
//            LOG.I("客户端处理指令集合:"+map);
        }
       private static class Holder{
           private static Client instance = new Excute.Client();
       }
        public static Client get(){
           return Holder.instance;
        }
    }

    /**
     *服务端处理命令
     */
    public static class Server extends Excute{
        private Server(){
            map.put(Command.Client.heartbeat,clsPrefix+"Heartbeat");//tcp心跳
            map.put(Command.Client.synchronizationSource,clsPrefix+"SynchronizationSource");//资源同步
//            LOG.I("服务器处理指令集合:"+map);
        }
        private static class Holder{
            private static Server instance = new Excute.Server();
        }
        public static Server get(){
            return Holder.instance;
        }
    }
    public interface IAction{
        void action(Intent intent);
    }
}
