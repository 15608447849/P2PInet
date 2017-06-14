package utils;

import sun.nio.ch.FileChannelImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Created by user on 2017/6/12.
 */
public class MPrivilegedAction implements PrivilegedAction {
    private MappedByteBuffer bytebuffer;

    public MPrivilegedAction(MappedByteBuffer bytebuffer) {
        this.bytebuffer = bytebuffer;
        AccessController.doPrivileged(this);
    }

    @Override
    public Object run() {
        try {
            Method getCleanerMethod = null;
            getCleanerMethod = bytebuffer.getClass().getMethod("cleaner",new Class[0]);
            getCleanerMethod.setAccessible(true);
            sun.misc.Cleaner cleaner = null;
            cleaner = (sun.misc.Cleaner)getCleanerMethod.invoke(bytebuffer,new Object[0]);
            cleaner.clean();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            try {
                // 加上这几行代码,手动unmap
                Method m = FileChannelImpl.class.getDeclaredMethod("unmap",MappedByteBuffer.class);
                m.setAccessible(true);
                m.invoke(FileChannelImpl.class, bytebuffer);
            } catch (NoSuchMethodException e1) {
                e1.printStackTrace();
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();
            } catch (InvocationTargetException e1) {
                e1.printStackTrace();
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }
}
