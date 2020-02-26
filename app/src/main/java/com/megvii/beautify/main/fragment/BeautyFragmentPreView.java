package com.megvii.beautify.main.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.megvii.beautify.R;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.ui.SeekBarRelativeLayout;
import com.megvii.beautify.util.FiveChooseView;
import com.megvii.beautify.util.Util;

import butterknife.BindView;

/**
 * Created by liyanshun on 2017/7/7.
 */

public class BeautyFragmentPreView extends BaseFragment {
    @BindView(R.id.skin)
    SeekBarRelativeLayout skinView;
    @BindView(R.id.white)
    SeekBarRelativeLayout whiteView;
    @BindView(R.id.brighteneye)
    SeekBarRelativeLayout brighteneyeView;
    @BindView(R.id.tooth)
    SeekBarRelativeLayout toothView;
    @BindView(R.id.remove_eyebags)
    SeekBarRelativeLayout removeEyebagsView;
    @BindView(R.id.red)
    SeekBarRelativeLayout redView;

    /*
    private FragmentCallBack callback = null;

    public void setCallBack(FragmentCallBack cb)
    {
        callback = cb;
    }
    */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beauty_fragment_preview, null);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int i =1;
                return false;
            }
        });
        return view;
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerSkin = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_DENOISE,progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_DENOISE,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerWhite = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
           // SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS,progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerBrightenEye = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
           // SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTEN_EYE,progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTEN_EYE,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerTooth = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_TOOTH,progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_TOOTH,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerRemoveEyebags = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_TOOTH,progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBAGS,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerRed = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK,progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerRemoveSpeckles = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            //SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK,progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_SPECKLES,progress);
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title.setText(R.string.beauty);
        skinView.setmTitle(R.string.denoise);
        whiteView.setmTitle(R.string.white);
        brighteneyeView.setmTitle(R.string.brighteneye);
        toothView.setmTitle(R.string.tooth);
        removeEyebagsView.setmTitle(R.string.removeeyebags);
        redView.setmTitle(R.string.red);

        setVisable();

        skinView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerSkin);
        whiteView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerWhite);
        brighteneyeView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerBrightenEye);
        toothView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerTooth);
        removeEyebagsView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerRemoveEyebags);
        redView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerRed);


        skinView.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_DENOISE*Util.BEAUTIFY_TRANS_COEFFICIENT));
        whiteView.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_BRIGHTNESS*Util.BEAUTIFY_TRANS_COEFFICIENT));
        brighteneyeView.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE*Util.BEAUTIFY_TRANS_COEFFICIENT));
        toothView.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_TOOTH*Util.BEAUTIFY_TRANS_COEFFICIENT));
        removeEyebagsView.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBAGS*Util.BEAUTIFY_TRANS_COEFFICIENT));
        redView.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_ADD_PINK*Util.BEAUTIFY_TRANS_COEFFICIENT));

    }

    private void setDefaultValue(FiveChooseView skinView, int index) {
        skinView.setCheckIndex(index);
    }


    private void SetBeautyParam(int beautyType, int progress){
        float chooseValue = (float)progress/Util.BEAUTIFY_TRANS_COEFFICIENT;
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(beautyType, chooseValue);
        switch (beautyType){
            case BeaurifyJniSdk.MG_BEAUTIFY_DENOISE:
            if(Math.abs(Util.CURRENT_MG_BEAUTIFY_DENOISE - chooseValue) >0.1){
                Util.CURRENT_MG_BEAUTIFY_DENOISE = chooseValue;
                if(null != callback ){
                    callback.onFragmantChanged(0,0);
                }
            }
            break;
            case BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS:
            if(Math.abs(Util.CURRENT_MG_BEAUTIFY_BRIGHTNESS - chooseValue) >0.1){
                Util.CURRENT_MG_BEAUTIFY_BRIGHTNESS = chooseValue;
                if(null != callback ){
                    callback.onFragmantChanged(0,0);
                }
            }
            break;
            case BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTEN_EYE:
            if(Math.abs(Util.CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE - chooseValue) >0.1){
                Util.CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE = chooseValue;
                if(null != callback ){
                    callback.onFragmantChanged(0,0);
                }
            }
            break;
            case BeaurifyJniSdk.MG_BEAUTIFY_TOOTH:
            if(Math.abs(Util.CURRENT_MG_BEAUTIFY_TOOTH - chooseValue) >0.1){
                Util.CURRENT_MG_BEAUTIFY_TOOTH = chooseValue;
                if(null != callback ){
                    callback.onFragmantChanged(0,0);
                }
            }
            break;
            case BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBAGS:
            if(Math.abs(Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBAGS - chooseValue) >0.1){
                Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBAGS = chooseValue;
                if(null != callback ){
                    callback.onFragmantChanged(0,0);
                }
            }
            break;
            case BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK:
            if(Math.abs(Util.CURRENT_MG_BEAUTIFY_ADD_PINK - chooseValue) >0.1){
                Util.CURRENT_MG_BEAUTIFY_ADD_PINK = chooseValue;
                if(null != callback ){
                    callback.onFragmantChanged(0,0);
                }
            }
            break;
            case BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_SPECKLES:
            if(Math.abs(Util.CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES - chooseValue) >0.1){
                Util.CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES = chooseValue;
                if(null != callback ){
                    callback.onFragmantChanged(0,0);
                }
            }
            default:
                break;

        }

    }

    private void setVisable(){
        if(Util.needGone(",denoise")){
            skinView.setVisibility(View.GONE);
        }
        if(Util.needGone(",brightness")){
            whiteView.setVisibility(View.GONE);
        }
        if(Util.needGone(",brithten_eye")){
            brighteneyeView.setVisibility(View.GONE);
        }
        if(Util.needGone(",tooth")){
            toothView.setVisibility(View.GONE);
        }
        if(Util.needGone(",remove_eyebags")){
            removeEyebagsView.setVisibility(View.GONE);
        }
        if(Util.needGone(",pink")){
            redView.setVisibility(View.GONE);
        }
    }
}
