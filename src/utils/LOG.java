package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by user on 2017/5/22.
 */
public class LOG {
    private static final String TAG = "日志";
    public static final SimpleDateFormat dataFormat =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"); //
    public static void I(String msg){
        System.out.println(TAG+" ["+getCurrentData()+"] "+msg);
    }
    public static void E(String msg){
        System.err.println(TAG+" ["+getCurrentData()+"] "+msg);
    }
    //时间转换
    private static String getCurrentData(){
        return dataFormat.format(new Date());
    }
}
