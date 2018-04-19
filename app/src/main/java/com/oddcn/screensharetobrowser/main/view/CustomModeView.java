package com.oddcn.screensharetobrowser.main.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.oddcn.screensharetobrowser.R;

/**
 * Created by OddCN on 2017/11/21.
 */

public class CustomModeView extends FrameLayout implements View.OnTouchListener {

    private boolean open;

    private FrameLayout frameLayout;
    private int frameColor;

    private TextView tvNow;

    private int imgVIconId;

    private String titleText;

    private String contentText;

    private View viewTransparent;

    public CustomModeView(@NonNull Context context) {
        super(context);
        initViewNull(context);
    }


    public CustomModeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTypedArray(context, attrs);
        initView(context);
    }

    public CustomModeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTypedArray(context, attrs);
        initView(context);
    }

    private void initTypedArray(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CustomModeView);

        open = ta.getBoolean(R.styleable.CustomModeView_open, false);
        frameColor = ta.getColor(R.styleable.CustomModeView_border_color, getResources().getColor(R.color.colorAccent));
        imgVIconId = ta.getResourceId(R.styleable.CustomModeView_img_icon, R.drawable.ic_wifi);
        titleText = ta.getString(R.styleable.CustomModeView_title_text);
        contentText = ta.getString(R.styleable.CustomModeView_content_text);

        ta.recycle();
    }

    public void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.custom_mode_view, this, true);

        frameLayout = (FrameLayout) findViewById(R.id.frame);
        tvNow = (TextView) findViewById(R.id.tv_now);
        ImageView imgV = (ImageView) findViewById(R.id.img_icon);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        TextView tvContent = (TextView) findViewById(R.id.tv_content);
        viewTransparent = findViewById(R.id.view_transparent);

        imgV.setImageResource(imgVIconId);
        tvTitle.setText(titleText);
        tvContent.setText(contentText);

        if (open) {
            open();
        } else {
            close();
        }

        setOnTouchListener(this);
    }

    public void setOpen(boolean open) {
        this.open = open;
        if (open) {
            open();
        } else {
            close();
        }
    }

    public void open() {
        frameLayout.setBackgroundColor(frameColor);
        tvNow.setVisibility(VISIBLE);
        viewTransparent.setVisibility(INVISIBLE);
    }

    public void close() {
        frameLayout.setBackgroundColor(Color.parseColor("#FFFFFF"));
        tvNow.setVisibility(INVISIBLE);
        viewTransparent.setVisibility(VISIBLE);
    }

    private void initViewNull(Context context) {
        LayoutInflater.from(context).inflate(R.layout.custom_mode_view, this, true);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                AnimatorSet upSet = new AnimatorSet();
                upSet.playTogether(
                        ObjectAnimator.ofFloat(view, "translationZ", 16),
                        ObjectAnimator.ofFloat(view, "scaleX", 1.05f),
                        ObjectAnimator.ofFloat(view, "scaleY", 1.05f)
                );
                upSet.setDuration(150);
                upSet.setInterpolator(new DecelerateInterpolator());
                upSet.start();
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                AnimatorSet downSet = new AnimatorSet();
                downSet.playTogether(
                        ObjectAnimator.ofFloat(view, "translationZ", 0),
                        ObjectAnimator.ofFloat(view, "scaleX", 1f),
                        ObjectAnimator.ofFloat(view, "scaleY", 1f)
                );
                downSet.setDuration(150);
                downSet.setInterpolator(new DecelerateInterpolator());
                downSet.start();
                break;
        }
        return false;
    }

}