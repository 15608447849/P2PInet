package utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by lzp on 2017/5/19.
 */
public class ClazzUtil {


    public static Object newInstance(String clazzName, Class[] clazzArr, Object[] paramArr){

        try {
            Class clazz =Class.forName(clazzName);
            Constructor constructor = clazz.getConstructor(clazzArr);
            Object object = constructor.newInstance(paramArr);
            return object;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void invokeMethod(Object object,String methodName,Class[] paramType,Object[] paramList){
        try {
            Class clazz = object.getClass();
            Method method = clazz.getMethod(methodName,paramType);
            method.invoke(object,paramList);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }




    //创建一个类并执行一个方法
    public static void createClazzInvokeMethod(String clazzName,String methodName,Class[] paramType,Object[] paramList){
        try {
            Class clazz  = Class.forName(clazzName);
            Method method = clazz.getMethod(methodName,paramType);
            method.invoke(clazz.newInstance(),paramList);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }catch (NoSuchMethodException e) {
            e.printStackTrace();
        }catch (ClassNotFoundException e) {
        e.printStackTrace();
        }
    }

}
