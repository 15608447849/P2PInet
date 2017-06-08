package client.sourceimp;


import client.obj.SerializeSource;
import utils.FindFileVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/2.
 *  文件资源管理器
 */
public class SourceManager {
    private final ReentrantLock lock = new ReentrantLock();
//    private final HashSet<SerializeSource> list = new HashSet<>();
    private final FindFileVisitor fonder;
    public SourceManager(String homeDirs) {
        this.fonder = new FindFileVisitor(homeDirs);
    }

    public boolean ergodicResource(SerializeSource source){
        try{
            lock.lock();
            if (source==null) new NullPointerException("source is not exist.");
            List<File> list =  fonder.setQuerySource(source).ergodicAll();
            //没找到
            if (list.size()==0) return false;else return true;
        }finally {
            lock.unlock();
        }

    }

//    public boolean add(SerializeSource task){
//        try{
//            lock.lock();
//            if (task==null) return false;
//            return list.add(task);
//        }finally {
//            lock.unlock();
//        }
//    }
//    public boolean remove(SerializeSource task){
//        try{
//            lock.lock();
//            if (task==null) return false;
//            return list.remove(task);
//        }finally {
//            lock.unlock();
//        }
//    }

}
