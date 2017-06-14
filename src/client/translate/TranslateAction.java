package client.translate;

/**
 * Created by user on 2017/6/13.
 */
public interface TranslateAction {
    void error(Exception e);
    void onComplete(DataElement element);
}
