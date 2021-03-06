package server.imp.threads;

import server.abs.IOperate;
import server.abs.IServer;
import server.obj.CLI;
import utils.LOG;

import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/1.
 */
public class P2POperate extends Thread implements IOperate {
    /**
     * try{
     lock.lock();

     }finally {
     lock.unlock();
     }
     */
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * 有效客户端实例队列
     *
     */
    private final HashSet<CLI> set = new HashSet<>();

    //监听超时时间
    private long time = 30 * 1000L;
    /***
     * 服务器
     */
    private IServer server;

    public P2POperate(int time) {
        this.time = time * 1000L;
        start();
    }

    @Override
    public void run() {
    //启动检查,30秒检查一次客户端列表
        while (true){
            synchronized (this){
                try {
                    this.wait(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                checkSet();
            }
        }
    }

    /**
     * 检测队列
     * 可以手动调用 - 删除连接对象
     */
    public void checkSet() {
        try{
            lock.lock();
            long curTime = System.currentTimeMillis();
            if (set.size()>0){
                Iterator<CLI> iterator = set.iterator();
                CLI client;
                while (iterator.hasNext()){
                    client = iterator.next();
                    if((curTime - client.getUpdateTime()) > time){
                        iterator.remove();
                        client.close();//关闭连接
                        LOG.E("移除客户端:"+client.getMac()+" 在线客户端数量: "+set.size());
                    }
                }
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void setServer(IServer server) {
        this.server = server;
    }

    @Override
    public IServer getServer() {
        return server;
    }

    //查看是否认证,是否已存在,如果不存在,添加到队列.
    @Override
    public void putCLI(CLI client) {
        try{
            lock.lock();
            if (client.isValid() && client.isAuthentication() && !client.isExist()){
                boolean flag = set.add(client);
                client.setExist(flag);
                if (flag){
                    LOG.E("添加客户端:"+client.getMac()+" 在线客户端数量: "+set.size());
                }
            }
            //LOG.I("添加:"+client+" ,当前队列大小:"+set.size());
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void turnSynchronizationSource(byte[] macBytes, byte[] sourceName) {
        try{
            lock.lock();
//            LOG.I("同步资源,终端数量 :"+ set.size() +" \n "+ set);
            if (set.size()==0) return;
            Iterator<CLI> iterator = set.iterator();
            while (iterator.hasNext()){
                //发送消息 - 同步资源
                iterator.next().getWrite().synchronizationSourceIssued(macBytes,sourceName);
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public CLI getClientByMac(String mac) {
        try{
            lock.lock();
            final Iterator<CLI> iterator = set.iterator();
            CLI client;
            while (iterator.hasNext()){
                client = iterator.next();
                if (client.getMac().equals(mac)){
                   return client;
                }
            }
            return null;
        }finally {
            lock.unlock();
        }
    }

}
