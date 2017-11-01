package com.oddcn.screensharetobrowser.utils;

import android.app.Activity;
import android.widget.Toast;

import com.oddcn.screensharetobrowser.R;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * Created by oddzh on 2017/11/1.
 */

public class PermissionUtil {
    public interface PermissionsListener {
        void onGranted();
    }

    public static void requestPermission(final Activity activity, final PermissionsListener listener, final String... permissions) {
        RxPermissions rxPermissions = new RxPermissions(activity);
        rxPermissions.request(permissions)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            listener.onGranted();
                        } else {
                            Toast.makeText(activity, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                    .show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
