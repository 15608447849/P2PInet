package client.translate;

/**
 * Created by user on 2017/6/13.
 */
public interface TranslateAction {
    void connectSuccess(DataElement element);
    void translateSuccess(DataElement element);
    void error(Exception e);
    void onOver(DataElement element);


}
