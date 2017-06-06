package client.socketimp;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/6.
 * 端口管理
 */
public class PortManager {
    private final HashSet<Integer> usePort = new HashSet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private int min = 2000,max = 2550;
    //设置范围
    public void setRanger(int min,int max){
        this.max = max;
        this.min = min;
    }
    public void addPort(int port){
        try{
            lock.lock();
            usePort.add(port);
        }finally {
            lock.unlock();
        }
    }
    public void removePort(int port){

        try {
            lock.lock();
            usePort.remove(port);
        }finally {
            lock.unlock();
        }
    }

    private boolean checkPort(int port){
        try{
            lock.lock();
            return  usePort.contains(port);
        }finally {
            lock.unlock();
        }
    }
    public int getPort(){
       int p = min + (int)(Math.random() * ((max - min) + 1));
       return checkPort(p)?getPort():p;
    }

    public int getPortToAdd(){
        int i = getPort();
        addPort(i);
        return i;
    }

    private PortManager(){}
    private static class Holder{
        private static PortManager manager = new PortManager();
    }
    public static PortManager get(){
        return Holder.manager;
    }
}
