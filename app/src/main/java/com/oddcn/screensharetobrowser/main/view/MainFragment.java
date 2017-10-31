package com.oddcn.screensharetobrowser.main.view;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.oddcn.screensharetobrowser.R;
import com.oddcn.screensharetobrowser.Utils;
import com.oddcn.screensharetobrowser.databinding.FragmentMainBinding;
import com.oddcn.screensharetobrowser.main.viewModel.MainViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private MainViewModel vm;

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
        binding.tabMode.getTabAt(1).setIcon(R.drawable.ic_hotspot).setText("手机热点模式");

        vm = new MainViewModel(getContext());
        binding.setVm(vm);

        vm.refreshIp();
    }

    private void initEvent() {
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
            textView.setGravity(Gravity.CENTER);
            if (position == 0) {
                textView.setText("0");
            } else if (position == 1) {
                textView.setText("1");
            }
            container.addView(textView);
            return textView;
        }

    }
}
