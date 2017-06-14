package client.translate;

import utils.LOG;

import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;
import java.util.concurrent.TimeUnit;

/**
 * Created by user on 2017/6/13.
 */
public abstract class DataImp extends Thread{

   public static final int OVER_MAX = 1000; //超时时间 最大次数
   public static final int OVER_INIT = 0; //初始化
   public static final int overTime = 30 * 1000; //单次超时时间

    protected DataElement element;
    protected TranslateAction action;
    protected int overTimeCount = 0;
    protected long len;
    public long sendCount = 0L; //当前发送次数
    public long recvCount = 0L; //当前发送次数
    public long position = 0L;//当前发送下标
    public byte cmd = 0;//命令
    public DataImp(DataElement element) {
        this.element = element;
    }
    public DataImp setAction(TranslateAction action) {
        this.action = action;
        return this;
    }
    @Override
    public void run() {
        //判断类型
        if (element.type == DataElement.UPLOAD){
                //等待开始上传命令 - 超时结束
                //收到命令 - 数据传输,超时结束
            if (!waitingNotify() || !translation()){
                    if (action!=null){
                        action.error(new IllegalStateException("数据传输超时超时."));
                    }
               }

        }else
        if (element.type == DataElement.DOWNLOAD){
                //通知开始上传 ->进入数据流接受状态
                if (!translation()){
                    if (action!=null){
                        action.error(new IllegalStateException("数据传输超时获取异常关闭."));
                    }
                }
        }if (action!=null){
            action.onComplete(element);
        }
    }

    protected boolean waitingNotify(){
        return false;
    }
    protected boolean translation(){
        return false;
    }


    protected void waitTime(){
        try {
            TimeUnit.MICROSECONDS.sleep(overTime );
        } catch (InterruptedException e) {
        }
    }

    protected void closeFileChannel(AsynchronousFileChannel fileChannel){
        if (fileChannel!=null && fileChannel.isOpen()){
            try {
                fileChannel.close();
                LOG.I("关闭文件流 - "+ fileChannel.isOpen());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
