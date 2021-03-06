package client.translate;
import protocol.Command;
import protocol.Parse;
import utils.LOG;
import utils.MD5Util;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.DatagramChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by user on 2017/6/13.
 */
public class DataDownload extends DataImp{

    private ArrayList<Integer> recList = new ArrayList<>();
    private long time ;
    //进度
    private long position = 0;
    public DataDownload(DataElement element) {
        super(element);
    }

    @Override
    protected void sureMTU() {
        LOG.I("检测MTU值.");
        resetTime();
        ByteBuffer buffer = ByteBuffer.allocate(Parse.DATA_BUFFER_MAX_ZONE);
        SocketAddress address = null;

            while (isNotTimeout()){
                buffer.clear();
                try {
                    address = getChannel().receive(buffer);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (checkAddress(address)){
                    buffer.flip();
                    if (buffer.get(0) == Command.UDPTranslate.mtuCheck){
                        buffer.rewind();
                        sendDataToAddress(buffer);
                    }
                    if (buffer.get(0) == Command.UDPTranslate.mtuSure){
                        buffer.position(1);
                        mtuValue = buffer.getInt();
                        LOG.I("确定MTU大小:"+ mtuValue);
                        return;
                    }
                }
            }
            state = ERROR;
            LOG.E("检测超时.");

    }

    @Override
    protected void slice() {
        if (state == ERROR) return;
        super.slice();
        //发送切片成功,开始接收数据
        ByteBuffer buffer = ByteBuffer.allocate(1);
        buffer.clear();
        buffer.put(Command.UDPTranslate.sliceSure);
        buffer.flip();

            sendDataToAddress(buffer);
            LOG.I("切片大小:"+sliceUnitMap.size() +",已发送切片成功应答.");

    }




    @Override
    protected void translation() {
        if (state == ERROR) return;
        state = SEND;
        //传输数据 - 异步发送
        AsynchronousFileChannel fileChannel = null;
        try {
            fileChannel = AsynchronousFileChannel.open(element.downloadFileTemp, StandardOpenOption.WRITE);
            while (state!=OVER && state!=ERROR){

                if (state == SEND){
                    querySend();
                }
                if (state == RECEIVE){
                    receiveData(fileChannel);
                }
                if (state == OVER){
                    checkComplete(fileChannel);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            closeFileChannel(fileChannel);
        }
    }

    /**
     * 检测完成
     * @param fileChannel
     */
    private void checkComplete(AsynchronousFileChannel fileChannel) {
        if (sliceUnitMap.isEmpty() || position==element.fileLength ){
            if (MD5Util.isSaveMD5(element.downloadFileTemp.toFile(),element.downloadFileMD5)){
                closeFileChannel(fileChannel);
                waitTime(1);
                //通知结束
                ByteBuffer buffer = ByteBuffer.allocate(1);
                buffer.clear();
                buffer.put(Command.UDPTranslate.over);
                buffer.flip();
                sendDataToAddress(buffer);
                LOG.I("下载成功.重命名: "+element.downloadFileTemp.toFile().renameTo(element.downloadFile.toFile())+" ,已通知任务完成.");
                return;
            }
        }
        LOG.I("剩余分片 数量:"+sliceUnitMap.size() +" 当前进度值:"+position+",文件实际大小:"+element.fileLength);
        state = SEND;
    }

    private void querySend() {

            ByteBuffer sendBuf = ByteBuffer.allocate(mtuValue);
            sendBuf.clear();
            LOG.E("发送传输请求. 已接收的分片数 : "+ recList.size());
            if (recList.size()>0){
                Iterator<Integer> itr = recList.iterator();
                int index = -1;
                while (itr.hasNext()){
                    index = itr.next();
                    itr.remove();
                    if (sendBuf.remaining()<4){
                        //发送
                        sendBuf.flip();
                        sendDataToAddress(sendBuf);
                        sendBuf.clear();
                        waitTime();
                    }
                    if (sendBuf.position()==0) {
                        sendBuf.put(Command.UDPTranslate.receiveSlice);
                    }
                    if (sendBuf.remaining()>=4){
                        sendBuf.putInt(index);
                    }
                }

                sendBuf.flip();
                sendDataToAddress(sendBuf);
            }

            waitTime();
            sendBuf.clear();
            sendBuf.put(Command.UDPTranslate.send);
            sendBuf.flip();
            sendDataToAddress(sendBuf);
            state = RECEIVE;
    }

    /**
     * 接受数据流
     * @param fileChannel
     */
    private void receiveData(AsynchronousFileChannel fileChannel) {
        if (fileChannel==null || !fileChannel.isOpen() ||  !getChannel().isOpen()){
            state = ERROR;
            return;
        }
            resetTime();
            SocketAddress address = null;
            final DatagramChannel channel = getChannel();
            ByteBuffer recBuf = null;
            int count;
            while (state==RECEIVE &&  channel.isOpen()){
                recBuf = ByteBuffer.allocate(mtuValue);
                recBuf.clear();
                try {
                    address = channel.receive(recBuf);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (checkAddress(address) ){
                    recBuf.flip();
                    if (recBuf.remaining()<4) continue;
                    resetTimeAndCount();
                    count = recBuf.getInt();
                    if (sliceUnitMap.containsKey(count)) {
                        fileChannel.write(recBuf, sliceUnitMap.get(count), count, this);
                    }else if (count==-1){
                        state = OVER;
                    }
                }
                if (!isNotTimeout()){
                    if (overTimeCount<OVER_MAX){ //没有超过超时次数
                        overTimeCount++;
                        LOG.E("接受数据应答超时.请求重新发送");
                        state = SEND;
                    }else{
                        LOG.E("接受数据超时且超过超时请求次数.");
                        state = ERROR;
                    }
                }
            }
    }
    private ReentrantLock lock = new ReentrantLock();
    @Override
    public void completed(Integer integer, Object o) {
        try{
            lock.lock();
            if (position==0) time=System.currentTimeMillis();
            position+=integer;
            int index = (int) o;
            recList.add(index);
            //已写入
            sliceUnitMap.remove(index); //移除下标
            if (position == element.fileLength){
                LOG.I("当前进度: "+ String.format("%.2f%%",((double)position / (double) element.fileLength)*100)+((position==element.fileLength)?" 耗时:"+(System.currentTimeMillis()-time):"."));
            }
            if (sliceUnitMap.size() == 0 || position==element.fileLength){
                state = OVER;
            }
        }finally {
            lock.unlock();
        }

    }
}
