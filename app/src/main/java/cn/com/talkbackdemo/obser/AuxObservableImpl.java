package cn.com.talkbackdemo.obser;

/**
 * Created by wang l on 2017/7/4.
 */

public class AuxObservableImpl  {


    public void mian(){
        AuxObservableIml<String> observableIml = new AuxObservableIml<>();
        AuxObserver<String> observer = new AuxObserver<String>() {
            @Override
            public void upData(String s) {

            }
        };

        AuxObserver<String> observer1 = new AuxObserver<String>() {
            @Override
            public void upData(String s) {

            }
        };

        observableIml.registerObserver(observer);
        observableIml.registerObserver(observer1);
        observableIml.sendMsg("xixihaha");
    }
}
