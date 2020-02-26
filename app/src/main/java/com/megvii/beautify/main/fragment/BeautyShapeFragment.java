package com.megvii.beautify.main.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
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

public class BeautyShapeFragment extends BaseFragment {
    @BindView(R.id.eye)
    SeekBarRelativeLayout eyeView;
    @BindView(R.id.face)
    SeekBarRelativeLayout faceView;
    @BindView(R.id.shortface)
    SeekBarRelativeLayout thinFaceView;
    @BindView(R.id.remove_eyebrow)
    SeekBarRelativeLayout removeEyebrow;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beauty_shape_fragment, null);
        return view;
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerFace = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SHRINK_FACE,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerEye = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ENLARGE_EYE,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerThinFace = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_THIN_FACE,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerRemoveEyebrow = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBROW,progress);
        }
    };


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("xie", "xie shape create");
        title.setText(R.string.beautity);
        eyeView.setmTitle(R.string.eye);
        faceView.setmTitle(R.string.face);
        thinFaceView.setmTitle(R.string.thinface);
        removeEyebrow.setmTitle(R.string.removeeyebrow);

        eyeView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerEye);
        faceView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerFace);
        thinFaceView.setOnSeekBarChangeListener(mOnSeekBarChangeListenerThinFace);
        removeEyebrow.setOnSeekBarChangeListener(mOnSeekBarChangeListenerRemoveEyebrow);

        eyeView.setProgress((int) (Util.CURRENT_MG_BEAUTIFY_ENLARGE_EYE * Util.BEAUTIFY_TRANS_COEFFICIENT));
        faceView.setProgress((int) (Util.CURRENT_MG_BEAUTIFY_SHRINK_FACE * Util.BEAUTIFY_TRANS_COEFFICIENT));
        thinFaceView.setProgress((int) (Util.CURRENT_MG_BEAUTIFY_THIN_FACE * Util.BEAUTIFY_TRANS_COEFFICIENT));
        removeEyebrow.setProgress((int) (Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW * Util.BEAUTIFY_TRANS_COEFFICIENT));
    }

    private void setDefaultValue(FiveChooseView skinView, int index) {
        skinView.setCheckIndex(index);
    }

    private void SetBeautyParam(int beautyType, int progress) {
        float chooseValue = (float) progress / Util.BEAUTIFY_TRANS_COEFFICIENT;
        BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam(beautyType, chooseValue);
        switch (beautyType) {
            case BeaurifyJniSdk.MG_BEAUTIFY_SHRINK_FACE:
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_SHRINK_FACE - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_SHRINK_FACE = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;
            case BeaurifyJniSdk.MG_BEAUTIFY_ENLARGE_EYE:
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_ENLARGE_EYE - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_ENLARGE_EYE = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;
            case BeaurifyJniSdk.MG_BEAUTIFY_THIN_FACE:
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_THIN_FACE - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_THIN_FACE = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;
            case BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBROW:
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setVisable(){
        if(Util.needGone(",enlarge_eye")){
            eyeView.setVisibility(View.GONE);
        }
        if(Util.needGone(",thin_face")){
            thinFaceView.setVisibility(View.GONE);
        }
        if(Util.needGone(",shrink_face")){
            faceView.setVisibility(View.GONE);
        }
        if(Util.needGone(",remove_eyebrow")){
            removeEyebrow.setVisibility(View.GONE);
        }
    }
}
