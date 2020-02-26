package com.megvii.beautify.util;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.megvii.beautify.R;

/**
 * Created by liyanshun on 2017/7/7.
 */

public class ChooseColorLayout extends RelativeLayout {


    public interface OnChooseListener {
        void onChoose(int index);
    }

    RadioGroup mRadios;
    OnChooseListener mListener;

    public ChooseColorLayout(Context context) {
        super(context);
        init();
    }

    public ChooseColorLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public ChooseColorLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.color_choose, this);
        mRadios = (RadioGroup) findViewById(R.id.color_choose_radio_group);
        mRadios.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (mListener == null) {
                    return;
                }
                switch (checkedId) {
                    case R.id.color_0:
                        mListener.onChoose(0);
                        break;
                    case R.id.color_1:
                        mListener.onChoose(1);
                        break;
                    case R.id.color_2:
                        mListener.onChoose(2);
                        break;
                    case R.id.color_3:
                        mListener.onChoose(3);
                        break;
                    case R.id.color_4:
                        mListener.onChoose(4);
                        break;
                    case R.id.color_5:
                        mListener.onChoose(5);
                        break;
                    case R.id.color_6:
                        mListener.onChoose(6);
                        break;
                    case R.id.color_7:
                        mListener.onChoose(7);
                        break;
                    case R.id.color_8:
                        mListener.onChoose(8);
                        break;
                    case R.id.color_9:
                        mListener.onChoose(9);
                        break;
                    default:
                        mListener.onChoose(0);
                }
            }
        });
    }


    public void setOnChooseListener(OnChooseListener mListener) {
        this.mListener = mListener;
    }

    public void setCheckIndex(int checkIndex) {
        RadioButton targetButton;
        switch (checkIndex) {

            case 1:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_1);
                break;
            case 2:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_2);
                break;
            case 3:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_3);
                break;
            case 4:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_4);
                break;
            case 5:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_5);
                break;
            case 6:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_6);
                break;
            case 7:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_7);
                break;
            case 8:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_8);
                break;
            case 9:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_9);
                break;
            case 0:
            default:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_0);
        }

        targetButton.setChecked(true);
    }

    public void setButtonColorView(int index, int r, int g, int b){
        RadioButton targetButton;
        switch (index) {

            case 1:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_1);
                break;
            case 2:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_2);
                break;
            case 3:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_3);
                break;
            case 4:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_4);
                break;
            case 5:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_5);
                break;
            case 6:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_6);
                break;
            case 7:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_7);
                break;
            case 8:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_8);
                break;
            case 9:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_9);
                break;
            case 0:
            default:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_0);
        }
        targetButton.setBackgroundColor(Color.rgb(r,g,b));
    }

    public void setContactLensTemplateView(int index){
        RadioButton targetButton;
        switch (index) {

            case 1:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_1);
                targetButton.setBackgroundResource(R.drawable._190_mt);
                break;
            case 2:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_2);
                targetButton.setBackgroundResource(R.drawable._631_mt);
                break;
            case 3:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_3);
                targetButton.setBackgroundResource(R.drawable._801_mt);
                break;
            case 4:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_4);
                targetButton.setBackgroundResource(R.drawable._803_mt);
                break;
            case 5:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_5);
                targetButton.setBackgroundResource(R.drawable._8011_mt);
                break;
            case 6:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_6);
                targetButton.setBackgroundResource(R.drawable._7_mt);
                break;
            case 7:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_7);
                targetButton.setBackgroundResource(R.drawable._8_mt);
                break;
            case 8:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_8);
                targetButton.setBackgroundResource(R.drawable._9_mt);
                break;
            case 9:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_9);
                targetButton.setBackgroundResource(R.drawable._05_mt);
                break;
            case 0:
            default:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_0);
                targetButton.setBackgroundResource(R.drawable._10_mt);
        }
    }

    public void setEyebrowTemplateView(int index){
        RadioButton targetButton;
        switch (index) {

            case 1:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_1);
                targetButton.setBackgroundResource(R.drawable.star_eyebrow);
                break;
            case 2:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_2);
                targetButton.setBackgroundResource(R.drawable.salix_eyebrow);
                break;
            case 3:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_3);
                targetButton.setBackgroundResource(R.drawable.euro_eyebrow);
                break;
            case 4:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_4);
                targetButton.setBackgroundResource(R.drawable.silk_eyebrow);
                break;
            case 5:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_5);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 6:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_6);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 7:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_7);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 8:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_8);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 9:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_9);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 0:
            default:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_0);
                targetButton.setBackgroundResource(R.drawable.stand_eyebrow);
        }

    }

    public void setBlushTemplateView(int index){
        RadioButton targetButton;
        switch (index) {

            case 1:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_1);
                targetButton.setBackgroundResource(R.drawable.heart);
                break;
            case 2:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_2);
                targetButton.setBackgroundResource(R.drawable.sunburn);
                break;
            case 3:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_3);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 4:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_4);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 5:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_5);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 6:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_6);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 7:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_7);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 8:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_8);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 9:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_9);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 0:
            default:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_0);
                targetButton.setBackgroundResource(R.drawable.ellipse);
        }

    }

    public void setEyeShadowTemplateView(int index){
        RadioButton targetButton;
        switch (index) {

            case 1:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_1);
                targetButton.setVisibility(View.INVISIBLE);

                break;
            case 2:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_2);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 3:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_3);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 4:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_4);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 5:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_5);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 6:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_6);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 7:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_7);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 8:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_8);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 9:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_9);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 0:
            default:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_0);
                targetButton.setBackgroundResource(R.drawable.pat1);
        }

    }

    public void setShadingTemplateView(int index){
        RadioButton targetButton;
        switch (index) {

            case 1:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_1);
                targetButton.setBackgroundResource(R.drawable.little_v);
                break;
            case 2:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_2);
                targetButton.setBackgroundResource(R.drawable.supperstar);
                break;
            case 3:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_3);
                targetButton.setBackgroundResource(R.drawable.water_light);
                break;
            case 4:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_4);
                targetButton.setBackgroundResource(R.drawable.supper);
                break;
            case 5:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_5);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 6:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_6);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 7:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_7);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 8:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_8);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 9:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_9);
                targetButton.setVisibility(View.INVISIBLE);
                break;
            case 0:
            default:
                targetButton = (RadioButton) mRadios.findViewById(R.id.color_0);
                targetButton.setBackgroundResource(R.drawable.high_nosebridge);
        }

    }
}
