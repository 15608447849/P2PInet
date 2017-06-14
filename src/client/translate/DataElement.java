package client.translate;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by user on 2017/6/13.
 */
public class DataElement {

    public static final int  UPLOAD = 1;
    public static final int  DOWNLOAD = 2;
    //传输类型
    public int type;

    public DatagramChannel channel ;//当前通道对象.
    public InetSocketAddress toAddress ;//对端

    public ByteBuffer buf1;//1500-20-8
    public ByteBuffer buf2;//8字节

    //上传文件路径
    public Path uploadFilePath;
    //文件完整的MD5值
    public String uploadFileMD5;

    public Path downloadFileTemp;
    public Path downloadFile;
    public String downloadFileMD5;

    public long fileLength;
    public DataElement(int type) {
        this.type = type;
    }








}
