package client.threads;

import client.translate.*;
import utils.LOG;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by user on 2017/6/6.
 * 客户端 文件下载
 */
public class TClientLoad extends TranslateThread {
    public TClientLoad(Translate translate) {
        super(translate);
        start();
    }

    @Override
    protected void openChannel() throws Exception {
        super.openChannel();
        LOG.I(TAG+"打开UDP管道.");
    }

    @Override
    protected void sendMessageToServer() throws Exception {
        LOG.I(TAG+"发送心跳到服务器.");
        super.sendMessageToServer();
    }




    @Override
    protected void sendMessageToTerminal() throws Exception {
        LOG.I(TAG+"终端信息: "+ translate.getTerminalSocket());
        super.sendMessageToTerminal();
    }

    @Override
    void translateData() throws Exception {
        LOG.I(TAG+ "数据下载....");
            //创建临时文件
        Path dirPath = translate.getSourceManager().getHome();
        String filePath =dirPath + translate.getResource().getPosition();
        String temFilePath = filePath+".tmp";
        File temp = new File(temFilePath);
        if (!temp.exists()){
            temp.createNewFile();
        }

        DataElement element = new DataElement(DataElement.DOWNLOAD);

            element.channel = translate.getChannel();//当前通道对象.
            element.toAddress = translate.getTerminalSocket();//对端
            element.downloadFileTemp = Paths.get(temFilePath);
            element.downloadFile = Paths.get(filePath);
            element.downloadFileMD5 = translate.getResource().getMd5();
            element.fileLength = translate.getResource().getSize();
            new DataDownload(element).setAction(new TranslateAction() {
            @Override
            public void error(Exception e) {
                LOG.I("传输错误 - "+ e);
            }
            @Override
            public void onComplete(DataElement element) {
                LOG.I("传输完成 - "+ element);
                synchronized (element){
                    element.notify();
                }
            }
        }).start();
        synchronized (element){
            element.wait();
        }
    }



}
