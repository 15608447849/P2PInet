package client.socketimp;

import client.threads.TranslateThread;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/16.
 */
public class TranslateManager {
    private ArrayList<TranslateThread> threadList = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();
    public void add(TranslateThread thread){
        try {
            lock.lock();
            threadList.add(thread);
        }finally {
            lock.unlock();
        }
    }

    public void remove(TranslateThread thread){
        try {
            lock.lock();
            threadList.remove(thread);
        }finally {
            lock.unlock();
        }
    }
    public int size(){
        return threadList.size();
    }
}
