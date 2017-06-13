package client.translate;

import utils.LOG;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousFileChannel;

/**
 * Created by user on 2017/6/13.
 */
public abstract class DataImp extends Thread{

   public static final int OVER_MAX = 1000; //超时时间 最大次数
   public static final int OVER_INIT = 0; //初始化
   public static final int overTime = 30; //单次超时时间

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

    public void setAction(TranslateAction action) {
        this.action = action;
    }


    @Override
    public void run() {
        //如果未关联


        //判断类型
        if (element.type == DataElement.UPLOAD){
                //等待开始上传命令 - 超时结束
                //收到命令 - 数据传输,超时结束
            if (waitLoaderNotify()){
               if (translateUp()){
                   if (action!=null){
                       action.translateSuccess(element);
                   }
               }else{
                   if (action!=null){
                       action.error(new IllegalStateException("数据传输超时"));
                   }
               }
            }else{
                if (action!=null){
                    action.error(new IllegalStateException("超时,未收到下载通知"));
                }
            }
        }else
        if (element.type == DataElement.DOWNLOAD){
                //通知开始上传 ->进入数据流接受状态
            if (notifyUploader()){
                if (translateDown()){
                    if (action!=null){
                        action.translateSuccess(element);
                    }
                }else{
                    if (action!=null){
                        action.error(new IllegalStateException("数据传输超时"));
                    }
                }
            }
        }else{
            if (action!=null){
                action.error(new IllegalStateException("未知的数据传输元素类型:"+ element.type));
            }
        }

        if (action!=null){
            action.onOver(element);
        }
    }

    protected boolean waitLoaderNotify(){
        return false;
    }
    protected boolean translateUp(){
        return false;
    }

    protected boolean notifyUploader(){
        return false;
    }
    protected boolean translateDown(){
        return false;
    }

    protected void closeFileChannel(AsynchronousFileChannel fileChannel){
        if (fileChannel!=null){
            try {
                fileChannel.close();
                LOG.I("关闭文件流.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
