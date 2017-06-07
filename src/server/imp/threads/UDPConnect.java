package server.imp.threads;

import client.obj.SerializeConnectTask;
import server.abs.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/6.
 * 循环检测队列新增的 connectTask对象,帮助UDP连接 管理UDP连接
 */
public class UDPConnect extends IThread implements IThreadInterface {

    private final ReentrantLock lock = new ReentrantLock();

    //新增加的连接对象
    private final LinkedList<SerializeConnectTask> newConnectList = new LinkedList<>();

    //正在使用的socketList
    private final HashMap<Integer,UDPTemporary> useUDPConnect = new HashMap<>();
    private IOperate operate;
    private byte[] ipBytes;
    //UDP端口范围
    private int startUdpPort = 1000,endUdpPort = 65535,tcpPort;

    private volatile boolean isLoop;
    public UDPConnect(IServer server) {
        super(server);
        isLoop = true;
        operate = (IOperate) server.getParam("operate");
        IParameter param = (IParameter)server.getParam("param");
        tcpPort = param.getPort();
        ipBytes = param.getIpBytes();
        launch();//启动自己
    }


    @Override
    protected void action() {
        while (isLoop){
            checkTask();
        }
    }
    //如果检测到队列有新对象
    private void checkTask() {

        try {
           lock.lock();
            Iterator<SerializeConnectTask> iterator = newConnectList.iterator();
            while (iterator.hasNext()){
                assignWork(iterator.next());
                iterator.remove();
            }
        } finally {
            lock.unlock();
        }

    }
    //分配工作 1 随机获取一个未使用的UDP端口 ,2 创建UDP临时连接对象 3 端口-对象添加映射
    private void assignWork(SerializeConnectTask task) {
        createTemp(assignPort(),task);
    }

    private void createTemp(int port,SerializeConnectTask task) {
        try {
            InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByAddress(ipBytes),port);
            new UDPTemporary(socketAddress,task,operate,this);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    //分配端口
    private int assignPort() {
        int randomPort = startUdpPort + (int)(Math.random() * ((endUdpPort - startUdpPort) + 1));
        if (randomPort == tcpPort) randomPort = assignPort();
        return useUDPConnect.containsKey(randomPort)?assignPort():randomPort;
    }


    @Override
    public void setPort(int start, int end) {
        this.startUdpPort = start;
        this.endUdpPort = end;
    }

    @Override
    public void putNewTask(SerializeConnectTask task) {
        try{
            lock.lock();
            newConnectList.add(task);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void putUseConnect(int port, UDPTemporary udpTemporary) {
        try{
            lock.lock();
            useUDPConnect.put(port,udpTemporary);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void removePort(int port) {
        try{
            lock.lock();
            useUDPConnect.remove(port);
        }finally {
            lock.unlock();
        }
    }

}
