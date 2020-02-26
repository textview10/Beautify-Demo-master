package com.megvii.beautify.ui;

import android.content.Context;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.megvii.beautify.R;

/**
 * @Project HumanBeauty3D
 * @Package com.megvii.human.ui.widget
 * @Author zhuangzaiku
 * @Date 2018/7/19
 * Copyright (c) 2018 Megvii All rights reserved.
 */
public class SeekBarRelativeLayout extends RelativeLayout {

    private SeekBar mSeekBar;
    private TextView mTitleView;
    private TextView mTextPercentView;
    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListener;
    private int mTextViewPaddingLeft = 0;

    public SeekBarRelativeLayout(Context context) {
        super(context);
        initSeekBar();
    }

    public SeekBarRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSeekBar();
    }

    public SeekBarRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSeekBar();
    }

    public void initSeekBar() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.layout_seekbar, this);
        mTextPercentView = (TextView) findViewById(R.id.tv_percent);
        mTitleView = (TextView) findViewById(R.id.seekbar_title);
        mTextPercentView.setVisibility(View.INVISIBLE);

        mSeekBar = (SeekBar) findViewById(R.id.ss_strength);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                }

                setMarginLeftForTextView(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }

                mTextPercentView.setVisibility(VISIBLE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mOnSeekBarChangeListener != null) {
                    mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
                mTextPercentView.setVisibility(INVISIBLE);
            }
        });

    }

    public void setOnSeekBarChangeListener(SeekBar.OnSeekBarChangeListener onSeekBarChangeListener) {
        this.mOnSeekBarChangeListener = onSeekBarChangeListener;
    }

    public void setmTitle(@StringRes int res) {
        mTitleView.setText(res);
    }

    private void setText(String str) {
        mTextPercentView.setText(str);
    }

    public void setMarginLeftForTextView(int progress) {
        if (mSeekBar != null && mTextPercentView != null) {
            LayoutParams layoutParams = (LayoutParams) mTextPercentView.getLayoutParams();
            int width = mSeekBar.getWidth() - mSeekBar.getPaddingLeft() - mSeekBar.getPaddingRight();
            layoutParams.leftMargin = (int) (((float) progress / mSeekBar.getMax()) * width);

            layoutParams.leftMargin += mSeekBar.getPaddingRight() - mTextPercentView.getWidth() / 2 + mTextViewPaddingLeft;

            setText(Integer.toString(progress));
            mTextPercentView.setLayoutParams(layoutParams);
        }
    }

    public void setMarginLeftForTextView2(int progress) {
        if (mSeekBar != null && mTextPercentView != null) {
            LayoutParams layoutParams = (LayoutParams) mTextPercentView.getLayoutParams();
            int width = mSeekBar.getWidth() - mSeekBar.getPaddingLeft() - mSeekBar.getPaddingRight();
            layoutParams.leftMargin = (int) (((float) progress * 2 / mSeekBar.getMax()) * width);

            layoutParams.leftMargin += mSeekBar.getPaddingRight() - mTextPercentView.getWidth() / 2 + mTextViewPaddingLeft;

            setText(Integer.toString(progress));
            mTextPercentView.setLayoutParams(layoutParams);
        }
    }

    public void setProgress(int process) {
        if (mSeekBar != null) {
            mSeekBar.setProgress(process);
        }
    }

    public void setEnabled(boolean enabled) {
        if (mSeekBar != null) {
            mSeekBar.setEnabled(enabled);
        }
    }

}
