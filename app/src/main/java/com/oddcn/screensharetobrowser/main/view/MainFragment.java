package com.oddcn.screensharetobrowser.main.view;


import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.oddcn.screensharetobrowser.R;
import com.oddcn.screensharetobrowser.RxBus;
import com.oddcn.screensharetobrowser.databinding.FragmentMainBinding;
import com.oddcn.screensharetobrowser.main.model.entity.ServerStatusChangedEvent;
import com.oddcn.screensharetobrowser.main.viewModel.MainViewModel;
import com.oddcn.screensharetobrowser.recorder.RecordService;
import com.oddcn.screensharetobrowser.server.MyServer;
import com.oddcn.screensharetobrowser.utils.PermissionUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private MainViewModel vm;

    public static final int RECORD_REQUEST_CODE = 101;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_main, container, false);
        binding = FragmentMainBinding.bind(contentView);

        initView();
        initEvent();
        return contentView;
    }

    private void initView() {
        binding.viewPagerModeDesc.setAdapter(new ModeDescAdapter());
        binding.tabMode.setupWithViewPager(binding.viewPagerModeDesc);
        binding.tabMode.getTabAt(0).setIcon(R.drawable.ic_wifi).setText("WiFi模式");
        binding.tabMode.getTabAt(1).setIcon(R.drawable.ic_hotspot).setText("热点模式");

        vm = new MainViewModel(getContext());
        binding.setVm(vm);

        vm.refreshIp();
    }

    private void initEvent() {
        RxBus.getDefault()
                .toObservable(ServerStatusChangedEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<ServerStatusChangedEvent>() {
                    @Override
                    public void accept(ServerStatusChangedEvent serverStatusChangedEvent) throws Exception {
                        vm.isServerRunning.set(serverStatusChangedEvent.isServerRunning);
                        Toast.makeText(getContext(), serverStatusChangedEvent.msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .subscribe();

        projectionManager = (MediaProjectionManager) getActivity().getSystemService(getContext().MEDIA_PROJECTION_SERVICE);

        Intent intent = new Intent(getActivity(), RecordService.class);
        getActivity().bindService(intent, connection, getContext().BIND_AUTO_CREATE);

        binding.btnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vm.isServerRunning.get()) {
                    MyServer.get().stopWithException();
                    recordService.stopRecord();
                    vm.isRecorderRunning.set(false);
                } else {
                    MyServer.init("0.0.0.0", vm.port.get());
                    MyServer.get().runAsync();
                }
            }
        });
        binding.btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtil.requestPermission(
                        getActivity(),
                        new PermissionUtil.PermissionsListener() {
                            @Override
                            public void onGranted() {
                                if (recordService.isRunning()) {
                                    recordService.stopRecord();
                                    vm.isRecorderRunning.set(false);
                                } else {
                                    Intent captureIntent = projectionManager.createScreenCaptureIntent();
                                    startActivityForResult(captureIntent, RECORD_REQUEST_CODE);
                                }
                            }
                        },
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                );
            }
        });
        binding.viewPagerModeDesc.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECORD_REQUEST_CODE && resultCode == RESULT_OK) {
            mediaProjection = projectionManager.getMediaProjection(resultCode, data);
            recordService.setMediaProject(mediaProjection);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    recordService.startRecord();
                    vm.isRecorderRunning.set(true);
                }
            }).start();
        }
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    public void onDestroy() {
        MyServer.get().stopWithException();
        getActivity().unbindService(connection);
        super.onDestroy();
    }

    private class ModeDescAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            TextView textView = new TextView(container.getContext());
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(layoutParams);
            textView.setGravity(Gravity.CENTER_VERTICAL);
            if (position == 0) {
                textView.setText(getText(R.string.wifi_mode_desc));
            } else if (position == 1) {
                textView.setText(getText(R.string.hotspot_mode_desc));
            }
            container.addView(textView);
            return textView;
        }
    }
}
