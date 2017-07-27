package cn.com.talkbackdemo.obser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wang l on 2017/7/3.
 */

public class AuxObservableIml<T> implements AuxObservable {

    private List<AuxObserver> mObserverList = new ArrayList<>();
    private T mT;

    @Override
    public void registerObserver(AuxObserver observer) {
        if (!mObserverList.contains(observer))
            mObserverList.add(observer);
    }

    @Override
    public void removeObserver(AuxObserver observer) {
        int i = mObserverList.indexOf(observer);
        if (i >= 0)
            mObserverList.remove(i);
    }

    @Override
    public void notifityDataChanged() {
        for (AuxObserver auxObserver : mObserverList) {
            notifityDataChanged(auxObserver);
        }
    }

    public void notifityDataChanged(AuxObserver auxObserver) {
        auxObserver.upData(mT);
    }

    public void sendMsg(T t){
        this.mT = t;
        notifityDataChanged();
    }
}
