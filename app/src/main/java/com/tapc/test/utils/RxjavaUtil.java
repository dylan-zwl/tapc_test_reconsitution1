package com.tapc.test.utils;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Administrator on 2017/3/31.
 */

public class RxjavaUtil {
    public static Disposable interval(long initialDelay, long period, TimeUnit unit, Consumer consumer) {
        return Observable.interval(initialDelay, period, unit).subscribeOn(Schedulers.io()).observeOn
                (AndroidSchedulers.mainThread()).subscribe(consumer);
    }

    public static void dispose(Disposable disposable) {
        if (disposable != null) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
    }

    public static Disposable create(ObservableOnSubscribe<String> source, Consumer consumer) {
        return Observable.create(source).subscribeOn(Schedulers.io()).observeOn
                (AndroidSchedulers.mainThread()).subscribe(consumer);
    }
}
