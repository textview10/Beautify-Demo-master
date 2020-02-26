package com.megvii.beautify.util;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.megvii.beautify.R;

/**
 * Created by liyanshun on 2017/7/7.
 */

public class FiveChooseView extends RelativeLayout {


    public interface OnChooseListener {
        void onChoose(int index);
    }

    TextView mTitle;
    RadioGroup mRadios;
    OnChooseListener mListener;

    public FiveChooseView(Context context) {
        super(context);
        init();
    }

    public FiveChooseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public FiveChooseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.five_choose, this);
        mTitle = (TextView) findViewById(R.id.five_choose_title);
        mRadios = (RadioGroup) findViewById(R.id.five_choose_radio_group);
        mRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (mListener == null) {
                    return;
                }
                switch (checkedId) {
                    case R.id.choose_one:
                        mListener.onChoose(1);
                        break;
                    case R.id.choose_two:
                        mListener.onChoose(2);
                        break;
                    case R.id.choose_three:
                        mListener.onChoose(3);
                        break;
                    case R.id.choose_four:
                        mListener.onChoose(4);
                        break;
                    case R.id.choose_five:
                        mListener.onChoose(5);
                        break;
                    case R.id.choose_none:
                    default:
                        mListener.onChoose(0);
                }
            }
        });
    }

    public void setmTitle(@StringRes int res) {
        mTitle.setText(res);
    }

    public void setOnChooseListener(OnChooseListener mListener) {
        this.mListener = mListener;
    }

    public void setCheckIndex(int checkIndex) {
        RadioButton targetButton;
        switch (checkIndex) {

            case 1:
                targetButton = (RadioButton) mRadios.findViewById(R.id.choose_one);
                break;
            case 2:
                targetButton = (RadioButton) mRadios.findViewById(R.id.choose_two);
                break;
            case 3:
                targetButton = (RadioButton) mRadios.findViewById(R.id.choose_three);
                break;
            case 4:
                targetButton = (RadioButton) mRadios.findViewById(R.id.choose_four);
                break;
            case 5:
                targetButton = (RadioButton) mRadios.findViewById(R.id.choose_five);
                break;
            case 0:
            default:
                targetButton = (RadioButton) mRadios.findViewById(R.id.choose_none);
        }

        targetButton.setChecked(true);
    }
}
