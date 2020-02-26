package com.megvii.beautify.main.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.megvii.beautify.util.ChooseColorLayout;
import com.megvii.beautify.util.FiveChooseView;
import com.megvii.beautify.util.Util;

import butterknife.BindView;

/**
 * Created by liyanshun on 2017/7/7.
 */

public class BeautyMakeupFragment extends BaseFragment {
    @BindView(R.id.eyebrow)
    SeekBarRelativeLayout eyebrow;
    @BindView(R.id.eyebrow_template)
    ChooseColorLayout eyebrow_template;

    @BindView(R.id.lip)
    SeekBarRelativeLayout lip;
    @BindView(R.id.lip_color)
    ChooseColorLayout lip_color;

    @BindView(R.id.blush)
    SeekBarRelativeLayout blush;
    @BindView(R.id.blush_color)
    ChooseColorLayout blush_color;
    @BindView(R.id.blush_template)
    ChooseColorLayout blush_template;

    @BindView(R.id.eyeshadow)
    SeekBarRelativeLayout eyeshadow;
    @BindView(R.id.eyeshadow_color)
    ChooseColorLayout eyeshadow_color;
    @BindView(R.id.eyeshadow_template)
    ChooseColorLayout eyeshadow_template;

    @BindView(R.id.shading)
    SeekBarRelativeLayout shading;
    @BindView(R.id.shading_template)
    ChooseColorLayout shading_template;

    @BindView(R.id.contactlens)
    SeekBarRelativeLayout contactlens;
    @BindView(R.id.contactlens_template)
    ChooseColorLayout contactlens_template;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.beauty_makeup_fragment, null);
        return view;
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerEyeBrow = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_EYEBROW,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerContactLens = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_CONTACT_LENS,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerLip = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_LIP,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerBlush = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_BLUSH,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerEyeshadow = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_EYESHADOW,progress);
        }
    };

    private SeekBar.OnSeekBarChangeListener mOnSeekBarChangeListenerShading = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            int progress = seekBar.getProgress();
            SetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SHADING,progress);
        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i("xie", "xie shape create");
        title.setText(R.string.makeup);
        eyebrow.setmTitle(R.string.eyebrow);
        lip.setmTitle(R.string.lip);
        contactlens.setmTitle(R.string.contactlens);
        blush.setmTitle(R.string.blush);
        shading.setmTitle(R.string.shading);
        eyeshadow.setmTitle(R.string.eyeshadow);

        setVisiable();

        eyebrow.setOnSeekBarChangeListener(mOnSeekBarChangeListenerEyeBrow);
        contactlens.setOnSeekBarChangeListener(mOnSeekBarChangeListenerContactLens);
        blush.setOnSeekBarChangeListener(mOnSeekBarChangeListenerBlush);
        eyeshadow.setOnSeekBarChangeListener(mOnSeekBarChangeListenerEyeshadow);
        shading.setOnSeekBarChangeListener(mOnSeekBarChangeListenerShading);
        lip.setOnSeekBarChangeListener(mOnSeekBarChangeListenerLip);

        eyebrow.setProgress((int) (Util.CURRENT_MG_BEAUTIFY_EYEBROW * Util.BEAUTIFY_TRANS_COEFFICIENT));
        contactlens.setProgress((int) (Util.CURRENT_MG_BEAUTIFY_CONTACTLENS* Util.BEAUTIFY_TRANS_COEFFICIENT));
        blush.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_BLUSH * Util.BEAUTIFY_TRANS_COEFFICIENT));
        eyeshadow.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_EYESHADOW * Util.BEAUTIFY_TRANS_COEFFICIENT));
        shading.setProgress((int)(Util.CURRENT_MG_BEAUTIFY_SHADING * Util.BEAUTIFY_TRANS_COEFFICIENT));
        lip.setProgress((int) (Util.CURRENT_MG_BEAUTIFY_LIP * Util.BEAUTIFY_TRANS_COEFFICIENT));

        SetDefaultView();
        SetParamsListener();

    }

    private void SetDefaultView(){
        for(int i=0;i<Util.DEFAULT_OPTION_SUM;i++){
            eyebrow_template.setEyebrowTemplateView(i);

            blush_color.setButtonColorView(i,Util.DEFAULT_BLUSH_COLOR[i*3],Util.DEFAULT_BLUSH_COLOR[i*3+1],Util.DEFAULT_BLUSH_COLOR[i*3+2]);
            blush_template.setBlushTemplateView(i);

            eyeshadow_color.setButtonColorView(i,Util.DEFAULT_EYESHADOW_COLOR[i*3],Util.DEFAULT_EYESHADOW_COLOR[i*3+1],Util.DEFAULT_EYESHADOW_COLOR[i*3+2]);
            eyeshadow_template.setEyeShadowTemplateView(i);

            shading_template.setShadingTemplateView(i);

            contactlens_template.setContactLensTemplateView(i);
            lip_color.setButtonColorView(i,Util.DEAFULT_LIP_COLOR[i*3],Util.DEAFULT_LIP_COLOR[i*3+1],Util.DEAFULT_LIP_COLOR[i*3+2]);
        }

    }

    private void setDefaultValue(ChooseColorLayout colorLayout, int index) {
        colorLayout.setCheckIndex(index);
    }

    private void SetBeautyParam(int beautyType, int progress) {
        float chooseValue = (float) progress / Util.BEAUTIFY_TRANS_COEFFICIENT;
        switch (beautyType) {
            case BeaurifyJniSdk.MG_BEAUTIFY_EYEBROW:
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(beautyType, chooseValue,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_R,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_G,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_B,
                        Util.DEFAULT_EYEBROW_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX],null,
                        Util.DEFAULT_EYEBROW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_EYEBROW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX].mKeyPointsSize);
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_EYEBROW - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_EYEBROW = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;
            case BeaurifyJniSdk.MG_BEAUTIFY_CONTACT_LENS:
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(beautyType, chooseValue,0,0,0,Util.DEFAULT_CONTACT_LENS_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX],null,null,0);
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_CONTACTLENS - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_CONTACTLENS = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;
            case BeaurifyJniSdk.MG_BEAUTIFY_LIP:
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(beautyType, chooseValue,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_R,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_G,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_B,null,null,null,0);
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_LIP - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_LIP = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;
            case BeaurifyJniSdk.MG_BEAUTIFY_ADD_BLUSH:
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(beautyType, chooseValue,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B,
                        Util.DEFAULT_BLUSH_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX],null,
                        Util.DEFAULT_BLUSH_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_BLUSH_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX].mKeyPointsSize);
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_BLUSH - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_BLUSH = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;

            case BeaurifyJniSdk.MG_BEAUTIFY_EYESHADOW:
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(beautyType, chooseValue,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B,
                        Util.DEFAULT_EYESHADOW_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX],null,
                        Util.DEFAULT_EYESHADOW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_EYESHADOW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX].mKeyPointsSize);
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_EYESHADOW - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_EYESHADOW = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;

            case BeaurifyJniSdk.MG_BEAUTIFY_SHADING:
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(beautyType,chooseValue,0,0,0,
                        Util.DEFAULT_SHADING_TEMPLATE.get(Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX).first,
                        Util.DEFAULT_SHADING_TEMPLATE.get(Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX).second,
                        Util.DEFAULT_SHADING_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_SHADING_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX].mKeyPointsSize);
                if (Math.abs(Util.CURRENT_MG_BEAUTIFY_SHADING - chooseValue) > 0.1) {
                    Util.CURRENT_MG_BEAUTIFY_SHADING = chooseValue;
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void SetParamsListener(){
        eyebrow_template.setOnChooseListener(new ChooseColorLayout.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                //setDefaultValue(eyebrow_template,index);
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_EYEBROW, Util.CURRENT_MG_BEAUTIFY_EYEBROW,Util.DEFAULT_EYEBROW_COLOR[index*3],Util.DEFAULT_EYEBROW_COLOR[index*3+1],Util.DEFAULT_EYEBROW_COLOR[index*3+2],
                        Util.DEFAULT_EYEBROW_TEMPLATE[index],null,
                        Util.DEFAULT_EYEBROW_KEYPOINTS[index].mKeyPoints,
                        Util.DEFAULT_EYEBROW_KEYPOINTS[index].mKeyPointsSize);
                if(Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX != index) {
                    Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX = index;
                    if(null != callback ){
                        callback.onFragmantChanged(0,0);
                    }
                }
            }
        });

        blush_color.setOnChooseListener(new ChooseColorLayout.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                //setDefaultValue(eyebrow_color,index);
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_ADD_BLUSH, Util.CURRENT_MG_BEAUTIFY_BLUSH,Util.DEFAULT_BLUSH_COLOR[index*3],Util.DEFAULT_BLUSH_COLOR[index*3+1],Util.DEFAULT_BLUSH_COLOR[index*3+2],
                        Util.DEFAULT_BLUSH_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX],null,
                        Util.DEFAULT_BLUSH_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_BLUSH_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX].mKeyPointsSize);
                if(Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R != Util.DEFAULT_BLUSH_COLOR[index*3] ||
                        Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G != Util.DEFAULT_BLUSH_COLOR[index*3+1]  ||
                        Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B != Util.DEFAULT_BLUSH_COLOR[index*3 +2]) {
                    Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R = Util.DEFAULT_BLUSH_COLOR[index*3];
                    Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G = Util.DEFAULT_BLUSH_COLOR[index*3+1];
                    Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B = Util.DEFAULT_BLUSH_COLOR[index*3+2];
                    if(null != callback ){
                        callback.onFragmantChanged(0,0);
                    }
                }
            }
        });

        blush_template.setOnChooseListener(new ChooseColorLayout.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                //setDefaultValue(eyebrow_template,index);
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_ADD_BLUSH, Util.CURRENT_MG_BEAUTIFY_BLUSH,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B,
                        Util.DEFAULT_BLUSH_TEMPLATE[index],null,
                        Util.DEFAULT_BLUSH_KEYPOINTS[index].mKeyPoints,
                        Util.DEFAULT_BLUSH_KEYPOINTS[index].mKeyPointsSize);
                if(Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX != index) {
                    Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX = index;
                    if(null != callback ){
                        callback.onFragmantChanged(0,0);
                    }
                }
            }
        });

        eyeshadow_color.setOnChooseListener(new ChooseColorLayout.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                //setDefaultValue(eyebrow_color,index);
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_EYESHADOW, Util.CURRENT_MG_BEAUTIFY_EYESHADOW,Util.DEFAULT_EYESHADOW_COLOR[index*3],Util.DEFAULT_EYESHADOW_COLOR[index*3+1],Util.DEFAULT_EYESHADOW_COLOR[index*3+2],
                        Util.DEFAULT_EYESHADOW_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX],null,
                        Util.DEFAULT_EYESHADOW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_EYESHADOW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX].mKeyPointsSize);
                if(Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R != Util.DEFAULT_EYESHADOW_COLOR[index*3] ||
                        Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G != Util.DEFAULT_EYESHADOW_COLOR[index*3+1]  ||
                        Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B != Util.DEFAULT_EYESHADOW_COLOR[index*3 +2]) {
                    Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R = Util.DEFAULT_EYESHADOW_COLOR[index*3];
                    Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G = Util.DEFAULT_EYESHADOW_COLOR[index*3+1];
                    Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B = Util.DEFAULT_EYESHADOW_COLOR[index*3+2];
                    if(null != callback ){
                        callback.onFragmantChanged(0,0);
                    }
                }
            }
        });

        eyeshadow_template.setOnChooseListener(new ChooseColorLayout.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                //setDefaultValue(eyebrow_template,index);
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_EYESHADOW, Util.CURRENT_MG_BEAUTIFY_EYESHADOW,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B,
                        Util.DEFAULT_EYESHADOW_TEMPLATE[index],null,
                        Util.DEFAULT_EYESHADOW_KEYPOINTS[index].mKeyPoints,
                        Util.DEFAULT_EYESHADOW_KEYPOINTS[index].mKeyPointsSize);
                if(Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX != index) {
                    Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX = index;
                    if(null != callback ){
                        callback.onFragmantChanged(0,0);
                    }
                }
            }
        });

        shading_template.setOnChooseListener(new ChooseColorLayout.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                //setDefaultValue(eyebrow_template,index);
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_SHADING, Util.CURRENT_MG_BEAUTIFY_SHADING,0,0,0,
                        Util.DEFAULT_SHADING_TEMPLATE.get(index).first,
                        Util.DEFAULT_SHADING_TEMPLATE.get(index).second,
                        Util.DEFAULT_SHADING_KEYPOINTS[index].mKeyPoints,
                        Util.DEFAULT_SHADING_KEYPOINTS[index].mKeyPointsSize);
                if(Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX != index) {
                    Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX = index;
                    if(null != callback ){
                        callback.onFragmantChanged(0,0);
                    }
                }
            }
        });

        contactlens_template.setOnChooseListener(new ChooseColorLayout.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                setDefaultValue(contactlens_template,index);
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_CONTACT_LENS, Util.CURRENT_MG_BEAUTIFY_CONTACTLENS,0,0,0,
                        Util.DEFAULT_CONTACT_LENS_TEMPLATE[index],null,null,0);
                if(Util.CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX != index ) {
                            Util.CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX = index;
                            if(null != callback ){
                                callback.onFragmantChanged(0,0);
                            }
                }
            }
        });
        lip_color.setOnChooseListener(new ChooseColorLayout.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                setDefaultValue(lip_color, index);
                BeaurifyJniSdk.preViewInstance().nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_LIP, Util.CURRENT_MG_BEAUTIFY_LIP, Util.DEAFULT_LIP_COLOR[index * 3], Util.DEAFULT_LIP_COLOR[index * 3 + 1], Util.DEAFULT_LIP_COLOR[index * 3 + 2],
                        null,null,null,0);
                if (Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_R != Util.DEAFULT_LIP_COLOR[index * 3] ||
                        Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_G != Util.DEAFULT_LIP_COLOR[index * 3 + 1] ||
                        Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_B != Util.DEAFULT_LIP_COLOR[index * 3 + 2]) {
                    Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_R = Util.DEAFULT_LIP_COLOR[index * 3];
                    Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_G = Util.DEAFULT_LIP_COLOR[index * 3 + 1];
                    Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_B = Util.DEAFULT_LIP_COLOR[index * 3 + 2];
                    if (null != callback) {
                        callback.onFragmantChanged(0, 0);
                    }
                }
            }
        });

    }

    private void setVisiable(){
        if(Util.isPreView){
            shading.setVisibility(View.GONE);
            shading_template.setVisibility(View.GONE);
        }

        if(Util.needGone(",eyebrow")){
            eyebrow.setVisibility(View.GONE);
            eyebrow_template.setVisibility(View.GONE);
        }
        if(Util.needGone(",lip")){
            lip.setVisibility(View.GONE);
            lip_color.setVisibility(View.GONE);
        }
        if(Util.needGone(",add_blush")){
            blush.setVisibility(View.GONE);
            blush_template.setVisibility(View.GONE);
            blush_color.setVisibility(View.GONE);
        }
        if(Util.needGone(",shading")){
            shading.setVisibility(View.GONE);
            shading_template.setVisibility(View.GONE);
        }
        if(Util.needGone(",eyeshadow")){
            eyeshadow.setVisibility(View.GONE);
            eyeshadow_template.setVisibility(View.GONE);
            eyeshadow_color.setVisibility(View.GONE);
        }
        if(Util.needGone(",contact_lens")){
            contactlens.setVisibility(View.GONE);
            contactlens_template.setVisibility(View.GONE);
        }
    }

}
