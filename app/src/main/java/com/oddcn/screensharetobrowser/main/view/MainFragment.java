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
import android.support.v7.widget.LinearLayoutManager;
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
import com.oddcn.screensharetobrowser.main.model.entity.RecorderStatusChangedEvent;
import com.oddcn.screensharetobrowser.main.model.entity.WsServerStatusChangedEvent;
import com.oddcn.screensharetobrowser.main.viewModel.MainViewModel;
import com.oddcn.screensharetobrowser.recorder.RecordService;
import com.oddcn.screensharetobrowser.server.ServerService;
import com.oddcn.screensharetobrowser.utils.PermissionUtil;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private MainViewModel vm;

    private ServerService serverService;

    public static final int RECORD_REQUEST_CODE = 101;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;

    private ConnAdapter connAdapter;

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

        connAdapter = new ConnAdapter();
        binding.recyclerViewConn.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewConn.setAdapter(connAdapter);
    }

    private void initEvent() {
        RxBus.getDefault()
                .toObservable(WsServerStatusChangedEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<WsServerStatusChangedEvent>() {
                    @Override
                    public void accept(WsServerStatusChangedEvent wsServerStatusChangedEvent) throws Exception {
                        vm.isServerRunning.set(wsServerStatusChangedEvent.isServerRunning);
                        vm.serverConnCount.set(wsServerStatusChangedEvent.connList.size());
                        connAdapter.setData(wsServerStatusChangedEvent.connList);
                        connAdapter.notifyDataSetChanged();
                        if (wsServerStatusChangedEvent.msg.isEmpty()) {
                            return;
                        }
                        Toast.makeText(getContext(), wsServerStatusChangedEvent.msg, Toast.LENGTH_SHORT).show();
                    }
                })
                .subscribe();
        RxBus.getDefault()
                .toObservable(RecorderStatusChangedEvent.class)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<RecorderStatusChangedEvent>() {
                    @Override
                    public void accept(RecorderStatusChangedEvent recorderStatusChangedEvent) throws Exception {
                        vm.isRecorderRunning.set(recorderStatusChangedEvent.isRunning);
                    }
                })
                .subscribe();

        projectionManager = (MediaProjectionManager) getActivity().getSystemService(getContext().MEDIA_PROJECTION_SERVICE);

        Intent serverIntent = new Intent(getActivity(), ServerService.class);
        getActivity().bindService(serverIntent, serverConnection, BIND_AUTO_CREATE);

        Intent recordIntent = new Intent(getActivity(), RecordService.class);
        getActivity().bindService(recordIntent, recorderConnection, BIND_AUTO_CREATE);

        binding.btnServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (serverService.isRunning()) {
                    serverService.stopServer();
                    recordService.stopRecord();
                } else {
                    serverService.startServer("0.0.0.0", vm.port.get());
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

    private ServiceConnection serverConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            ServerService.ServerServiceBinder binder = (ServerService.ServerServiceBinder) service;
            serverService = binder.getServerService();
            vm.isServerRunning.set(serverService.isRunning());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    private ServiceConnection recorderConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            recordService = binder.getRecordService();
            recordService.setConfig(metrics.widthPixels, metrics.heightPixels, metrics.densityDpi);
            vm.isRecorderRunning.set(recordService.isRunning());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    public void onDestroy() {
        getActivity().unbindService(serverConnection);
        getActivity().unbindService(recorderConnection);
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
