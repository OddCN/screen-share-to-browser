package com.oddcn.screensharetobrowser.main.view;


import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.oddcn.screensharetobrowser.R;
import com.oddcn.screensharetobrowser.databinding.FragmentMainBinding;
import com.oddcn.screensharetobrowser.main.viewModel.MainViewModel;
import com.oddcn.screensharetobrowser.recorder.RecordService;
import com.oddcn.screensharetobrowser.recorder.RecordServiceListener;
import com.oddcn.screensharetobrowser.server.ServerService;
import com.oddcn.screensharetobrowser.server.ServerServiceListener;
import com.oddcn.screensharetobrowser.server.webServer.WebServer;
import com.oddcn.screensharetobrowser.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.BIND_AUTO_CREATE;

public class MainFragment extends Fragment {
    private static final String TAG = "MainFragment";

    private FragmentMainBinding binding;
    private MainViewModel vm;

    private ServerService serverService;

    public static final int RECORD_REQUEST_CODE = 101;
    private MediaProjectionManager projectionManager;
    private MediaProjection mediaProjection;
    private RecordService recordService;

    private ConnAdapter connAdapter;

    private BroadcastReceiver broadcastReceiverNetworkState;

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
        vm = new MainViewModel(getContext());
        binding.setVm(vm);

        vm.refreshIp();

        connAdapter = new ConnAdapter();
        binding.recyclerViewConn.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewConn.setAdapter(connAdapter);
    }

    private void initEvent() {
        initBroadcastReceiverNetworkStateChanged();
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
                    serverService.startServer();
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

            serverService.setListener(new ServerServiceListener() {
                @Override
                public void onServerStatusChanged(boolean isRunning) {
                    vm.isServerRunning.set(isRunning);
                    vm.serverConnCount.set(0);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            List<String> emptyConns = new ArrayList<>();
                            connAdapter.setData(emptyConns);
                            connAdapter.notifyDataSetChanged();
                        }
                    });
                }

                @Override
                public void onWebServerError(final int errorType) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (errorType == WebServer.ERROR_TYPE_PORT_IN_USE)
                                Toast.makeText(serverService, "因端口占用，已更换至可用端口", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onWsServerError(final int errorType) {
                }

                @Override
                public void onWsServerConnChanged(final List<String> connList) {
                    vm.serverConnCount.set(connList.size());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            connAdapter.setData(connList);
                            connAdapter.notifyDataSetChanged();
                        }
                    });
                }
            });
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

            recordService.setListener(new RecordServiceListener() {
                @Override
                public void onRecorderStatusChanged(boolean isRunning) {
                    vm.isRecorderRunning.set(isRunning);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    public void onDestroy() {
        serverService.removeListener();
        recordService.removeListener();
        getActivity().unbindService(serverConnection);
        getActivity().unbindService(recorderConnection);
        if (broadcastReceiverNetworkState != null) {
            getActivity().unregisterReceiver(broadcastReceiverNetworkState);
        }
        super.onDestroy();
    }

    private void initBroadcastReceiverNetworkStateChanged() {
        final IntentFilter filters = new IntentFilter();
        filters.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filters.addAction("android.net.wifi.STATE_CHANGE");
        filters.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
        broadcastReceiverNetworkState = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: " + intent.getAction());
                int state = intent.getIntExtra("wifi_state", -66);
                Log.i(TAG, "state= " + state);
                if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())
                        && state == 13) {
                    vm.mode.set(1);
                } else {
                    vm.mode.set(0);
                }
                vm.refreshIp();
            }
        };
        getActivity().registerReceiver(broadcastReceiverNetworkState, filters);
    }

}
