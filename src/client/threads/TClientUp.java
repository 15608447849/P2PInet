package client.threads;

import client.translate.DataElement;
import client.translate.DataImp;
import client.translate.DataUpload;
import client.translate.TranslateAction;
import utils.LOG;

import java.nio.ByteBuffer;
import java.nio.file.Paths;

/**
 * Created by user on 2017/6/6.
 *  客户端文件上传
 */
public class TClientUp extends TranslateThread {
    public TClientUp(Translate translate) {
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
        LOG.I(TAG+ "数据上传....");
        //创建数据上传对象
        DataElement element = new DataElement(DataElement.UPLOAD);
            element.buf1 = translate.getBuffer();//数据发送
            element.buf2 =  ByteBuffer.allocate(8);//数据接受
            element.channel = translate.getChannel();//当前通道对象.
            element.toAddress = translate.getTerminalSocket();//对端
             String filePath = translate.getSourceManager().getHome()+translate.getResource().getPosition();
            element.uploadFilePath = Paths.get(filePath);
            element.uploadFileMD5 = translate.getResource().getMd5Hash();

        final DataImp upload =  new DataUpload(element);
        upload.setAction(new TranslateAction() {

            @Override
            public void translateSuccess(DataElement element) {
                LOG.I("传输成功 - "+ element);
            }

            @Override
            public void error(Exception e) {
                LOG.I("传输错误 - "+ e);
            }

            @Override
            public void onOver(DataElement element) {
                LOG.I("传输完成 - "+ element);
                synchronized (upload){
                    upload.notify();
                }
            }
        });
        upload.start();
        synchronized (upload){
            upload.wait();
        }

    }























}
