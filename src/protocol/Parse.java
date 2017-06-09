package protocol;

import utils.LOG;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * Created by user on 2017/5/31.
 * 数据解析
 */
public class Parse {

    /**
     * 数据块大小
     */
    public static final int buffSize = 1024;
    public static final String _protocol = "protocol";
    public static final String _length = "length";
    public static final String _dataBlock = "dataBlock";

    public static final String _udpPort = "udpPort";

    public static final String _ipBytes = "ipBytes";
    public static final String _portInt = "portInt";
    public static final String _macBytes = "macBytes";
    public static final String _natTypeBytes = "natTypeBytes";

    public static final String _localSourceBytes = "localSourceBytes";
    public static final String _connectTaskBytes = "connectTaskBytes";




    /**
     *
     * @param byteBuffer
     * @return map ->
     *  protocol - byte
     *  length - int
     *  dataBlock - byte[]
     *  heartbeat_ipBytes - byte[]
     *  heartbeat_portBytes- byte[]
     *  heartbeat_macBytes- byte[]
     *
     */
    public static HashMap<String,Object> message(final ByteBuffer byteBuffer){
        byteBuffer.flip();
        HashMap<String,Object> map = new HashMap<>();
        try {
            //获取协议, 获取数据长度, 获取数据块
            byte protocol = byteBuffer.get(0);//协议
//            LOG.E("协议编号:"+protocol);
            map.put(_protocol, protocol);
            //数据块
            byte[] data = null;
            //数据体长度
            int length = -1;

            if (byteBuffer.limit()>1){
                length = byteBuffer.getInt(1);
                data = new byte[length];
                //复制数据 current _ position = 5
                byteBuffer.position(5);
                byteBuffer.get(data,0,length);
            }

                //根据协议,解析数据
            ParseOpers.selectTo(protocol,map,length,data);
        }catch (Exception e){
//            e.printStackTrace();
//            LOG.E("解析客户端消息失败:"+e.getCause());
        }
//        LOG.I("解析消息结果:"+ map + "byteBuff:"+byteBuffer);
        return map.size()>0?map:null;
    }

    // byte[] -> int
    public static int bytes2int(byte[] nunberBytes) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.put(nunberBytes);
        byteBuffer.flip();
        int i = byteBuffer.getInt();
        return i;
    }
    public static byte[] int2bytes(int nunber){
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(nunber);
        byte[] bytes = byteBuffer.array();
        return bytes;
    }

    private static final String charsetCode = "GBK";
    /**
     * 字符串编码解码
    private final static Charset charset = Charset.forName(charsetCode);
    private final static CharsetDecoder decoder = charset.newDecoder();
    private final static CharsetEncoder encoder = charset.newEncoder();
     */
    public static byte[] string2bytes(String chararr) throws UnsupportedEncodingException {
        return chararr.getBytes(charsetCode);
    }

    public static String bytes2string(byte[] charBytes) throws UnsupportedEncodingException {
        return new String(charBytes,charsetCode);
    }
    /**
     * 对象转数组
     * @param obj
     * @return
     */
    public static byte[] sobj2Bytes(Object obj) throws IOException {
        byte[] bytes = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            bytes = bos.toByteArray ();
            oos.close();
            bos.close();
        return bytes;
    }

    /**
     * 数组转对象
     * @param bytes
     * @return
     */
    public static Object bytes2Sobj (byte[] bytes) throws IOException, ClassNotFoundException {
        Object obj = null;
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream (bis);
            obj = ois.readObject();
            ois.close();
            bis.close();
        return obj;
    }
}
