package com.oddcn.screensharetobrowser;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.internal.operators.flowable.FlowableOnBackpressureError;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by eggsy on 17-1-6.
 */
public class RxBus {
    private static RxBus instance;
    private Subject<Object> subjectBus;
    private FlowableProcessor<Object> processorBus;

    public static RxBus getDefault() {
        if (instance == null) {
            synchronized (RxBus.class) {
                if (instance == null) {
                    RxBus tempInstance = new RxBus();
                    tempInstance.subjectBus = PublishSubject.create().toSerialized();
                    tempInstance.processorBus = PublishProcessor.create().toSerialized();
                    instance = tempInstance;
                }
            }
        }
        return instance;
    }

    public Disposable register(Class eventType, Consumer observer) {
        return toObserverable(eventType).subscribe(observer);
    }

    public Disposable register(Class eventType, Consumer observer, Scheduler scheduler) {
        return toObserverable(eventType).observeOn(scheduler).subscribe(observer);
    }

    public Disposable register(Class eventType, Consumer observer, Scheduler scheduler, BackpressureStrategy strategy) {
        Flowable o = toFlowable(eventType);
        switch (strategy) {
            case DROP:
                o = o.onBackpressureDrop();
            case LATEST:
                o = o.onBackpressureLatest();
            case MISSING:
                o = o;
            case ERROR:
                o = RxJavaPlugins.onAssembly(new FlowableOnBackpressureError<>(o));
            default:
                o = o.onBackpressureBuffer();
        }
        if (scheduler != null) {
            o.observeOn(scheduler);
        }
        return o.subscribe(observer);
    }

    public Disposable register(Class eventType, Consumer observer, BackpressureStrategy strategy) {
        return register(eventType, observer, null, strategy);
    }

    public void unRegister(Disposable disposable) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    public void unRegister(CompositeDisposable compositeDisposable) {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }

    public void post(final Object event) {
        subjectBus.onNext(event);
        processorBus.onNext(event);
    }

    private Observable toObserverable(Class cls) {
        return subjectBus.ofType(cls);
    }

    private Flowable toFlowable(Class cls) {
        return processorBus.ofType(cls);
    }

    public boolean hasObservers() {
        return subjectBus.hasObservers();
    }

    public boolean hasSubscribers() {
        return processorBus.hasSubscribers();
    }
}