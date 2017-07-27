package cn.com.talkbackdemo.obser;

/**
 * Created by wang l on 2017/7/3.
 */

public interface AuxObservable {

    void registerObserver(AuxObserver observer);

    void removeObserver(AuxObserver observer);

    void notifityDataChanged();
}

