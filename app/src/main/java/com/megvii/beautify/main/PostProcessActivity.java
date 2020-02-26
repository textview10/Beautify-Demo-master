package com.megvii.beautify.main;

import android.Manifest;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.megvii.beautify.R;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.main.fragment.BaseFragment;
import com.megvii.beautify.main.fragment.BeautyFragmentPost;
import com.megvii.beautify.main.fragment.BeautyMakeupFragment;
import com.megvii.beautify.main.fragment.BeautyShapeFragment;
import com.megvii.beautify.main.fragment.FragmentCallBack;
import com.megvii.beautify.main.fragment.ListFragment;
import com.megvii.beautify.model.Model;
import com.megvii.beautify.model.StaticsEvent;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.KeyPoints;
import com.megvii.beautify.util.FileUtil;
import com.megvii.beautify.util.FiveChooseView;
import com.megvii.beautify.util.ImageConUtil;
import com.megvii.beautify.util.ImageUtils;
import com.megvii.beautify.util.MLog;
import com.megvii.beautify.util.NoDoubleClickUtil;
import com.megvii.beautify.util.Util;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by donglinghai on 2017/12/16.
 */

public class PostProcessActivity extends Activity implements View.OnTouchListener, FragmentCallBack {
    public final static String TAG = "PostProcessActivity";

    @BindView(R.id.process_image_id_new)
    ImageView mProcessImage;

    @BindView(R.id.main_radio_group_new)
    RadioGroup radioGroup;

    @BindView(R.id.fragment_content)
    ViewGroup fragmentContent;

    @BindView(R.id.statics_info)
    TextView tvStatics;

    @BindView(R.id.none)
    RadioButton radioButtonNone;

//    @BindView(R.id.facepp_layout_debugSwitch)
//    Button btnDebugSwitch;

    Button ll_download;
    Button ll_gallery;



    private BaseFragment mCurrentFragment;


    private Bitmap mInBmp;
    private byte[] inNV21;
    private int nv21_width;
    private int nv21_height;
    private Bitmap mInBmpshow;
    private Bitmap mOutBmp;
    private Bitmap moutBmpshow;

    public static final int GALLERY_CODE = 101;
    public static final int REQ_GALLERY_CODE = 102;

    ProgressDialog dialog;
    private Context mContext;

    private BeaurifyJniSdk beaurifyJniSdk;

    //@BindView(R.id.choose_value)
    //FiveChooseView choose_value;
    float mCurrentChoose = Util.CURRENT_MG_BEAUTIFY_DENOISE;
    int mImageType;
    //the pay load of min face, modify this parameter to larger values will cause the
    //detect face faster.
    public final static int FACE_DETECT_BALANCE_PARAM = 25;

    public int mImageOrientation = 0;

    private Handler handler=new Handler(){

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);

        Util.isPreView = false;   //设置为后处理
        toggleHideyBar(this);

        mContext = getApplicationContext();



        //初始化美瞳模板
        Util.initTemplates(mContext);

        final Uri uri = getIntent().getParcelableExtra("imgurl");
        mImageType =  getIntent().getIntExtra("image_type", 0);
        Log.i(TAG, "onCreate: type is  " + mImageType);

        dialog = new ProgressDialog(this);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                String path = ImageConUtil.getRealPathFromURI(PostProcessActivity.this, uri);
                mInBmp = ImageConUtil.getImagewithoutRotation(path);
                mImageOrientation = ImageConUtil.getImageOrienata(path);
                //mInBmp = getYUVBitMap(inNV21, mInBmp.getWidth(), mInBmp.getHeight());
                //clampBitmap();
                Bitmap InBmpWithRotate = ImageConUtil.getImagewithRotation(path);
                if(mImageType == LaunchActivity.NV21_CODE){
                    inNV21 = getNV21(InBmpWithRotate);
                    mInBmpshow = getYUVBitMap(inNV21, nv21_width, nv21_height);
                    clampShowBitmap();
                } else {
                    mInBmpshow = InBmpWithRotate;
                    clampShowBitmap();
                }

                beaurifyJniSdk.nativeReleaseResources();
                beaurifyJniSdk.nativeSetLogLevel(beaurifyJniSdk.MG_LOG_LEVEL_DEBUG);
                beaurifyJniSdk.nativeCreateBeautyHandle(mContext, mInBmp.getWidth(),
                        mInBmp.getHeight(), mImageOrientation, Util.MG_FPP_DENSEDETECTIONMODE_FULL_SIZE,
                        ConUtil.getFileContent(mContext, R.raw.mgbeautify_1_2_4_model)
                        , ConUtil.getFileContent(mContext, R.raw.detect_model),
                        ConUtil.getFileContent(mContext, R.raw.dense_model));
                beaurifyJniSdk.nativeDoneGLContext();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        int width = getWindowManager().getDefaultDisplay().getWidth();
//                        int bitmapWidth=mInBmpshow.getWidth();
//                        int bitmapHeight=mInBmpshow.getHeight();
//                        int imageHeight=width*bitmapHeight/bitmapWidth;
//                        mProcessImage.setLayoutParams(new RelativeLayout.LayoutParams(width,imageHeight));
                        mProcessImage.setImageBitmap(mInBmpshow);
                    }
                });

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                    }
                });
            }
        }).start();

        setContentView(R.layout.activity_post_process);
        ButterKnife.bind(this);
//        radioGroup = (RadioGroup)findViewById(R.id.main_radio_group_new);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                MLog.i("radioGroup:" + checkedId);
                if (NoDoubleClickUtil.isFastDoubleClick()){
                    return;
                }
                showFragment(checkedId);
                radioButtonNone.setChecked(true);

            }
        });

        radioGroup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                MLog.i("radioGroup:" + event.getAction());
                return false;
            }
        });


        //根据能力，是否隐藏功能
        if(Util.needGoneBeautify()){
            View beautifyButton = findViewById(R.id.beauty);
            beautifyButton.setVisibility(View.GONE);
        }
        if(Util.needGoneBeautifyShape()){
            View beautifyShapeButton = findViewById(R.id.beautity);
            beautifyShapeButton.setVisibility(View.GONE);
        }
        if(Util.needGoneMakeup()){
            View MakeupButton = findViewById(R.id.makeup);
            MakeupButton.setVisibility(View.GONE);
        }

        ImageConUtil.toggleHideyBar(this);

        mProcessImage = (ImageView)findViewById(R.id.process_image_id_new);


        mProcessImage.setOnTouchListener(this);

        beaurifyJniSdk = BeaurifyJniSdk.imageInstance();
        ConUtil.toggleHideyBar(this);
        //choose_value.setCheckIndex(mCurrentChoose);
        /*m
        choose_value.setOnChooseListener(new FiveChooseView.OnChooseListener() {
            @Override
            public void onChoose(int index) {
                mCurrentChoose = index;
            }
        });
        */


    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStaticsEvent(StaticsEvent event) {
        tvStatics.setText(event.info);
    }

    private void showFragment(int checkedId) {
        if (checkedId == R.id.none) {
            return;
        }
        radioGroup.setVisibility(View.INVISIBLE);
        fragmentContent.setVisibility(View.VISIBLE);
        toggleHideyBar(this);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Bundle data = new Bundle();
        switch (checkedId) {
            case R.id.beauty:
                mCurrentFragment = new BeautyFragmentPost();
                mCurrentFragment.setCallBack(this);
                break;
            case R.id.beautity:
                mCurrentFragment = new BeautyShapeFragment();
                mCurrentFragment.setCallBack(this);
                break;
            case R.id.makeup:
                mCurrentFragment = new BeautyMakeupFragment();
                mCurrentFragment.setCallBack(this);
                break;
            case R.id.filter:
                data.putInt(ListFragment.ARGUMENTS, Util.TYPE_FILTER);
                break;

        }

        if (!data.isEmpty()) {
            mCurrentFragment = new ListFragment();
            mCurrentFragment.setArguments(data);
            mCurrentFragment.setCallBack(this);
        }
        fragmentTransaction.add(R.id.fragment_content, mCurrentFragment);
        fragmentTransaction.commit();

    }

    @Override
    public void onFragmantChanged(int type, int value)
    {
        Log.d(TAG,"---->>>> on onFragmantChanged callback :type="+type+",value ="+value);
        processImageUnchecked(type);
    }

    @Override
    public void onListFragmentChanged()
    {
        Log.d(TAG,"---->>>> on onListFragmentChanged callback");
        processImageFilter();
    }

    @Override
    public void onBackPressed() {
        if (radioGroup.getVisibility() == View.INVISIBLE) {
            radioGroup.setVisibility(View.VISIBLE);
            toggleHideyBar(this);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.remove(mCurrentFragment);
            fragmentTransaction.commit();
            fragmentContent.setVisibility(View.INVISIBLE);
        }
         else {
            super.onBackPressed();
        }
    }

    private void clampShowBitmap() {
        int w = mInBmpshow.getWidth();
        int h = mInBmpshow.getHeight();

        //部分平台的gpu只支持4096*4096分辨率，对图片进行缩放 ，这边demo现实统一全部都进行缩放
        if( w  > 4096 || h > 4096 )
        {
            long startTime = System.nanoTime();

            float scalew =  ((float) 4096) / w;
            float scaleh =  ((float) 4096) / h;
            float scale = scalew < scaleh ? scalew : scaleh;

            Matrix matrix = new Matrix();
            matrix.postScale(scale,scale);

            mInBmpshow = Bitmap.createBitmap(mInBmpshow, 0, 0, w, h,matrix,true);

            long consumingTime = System.nanoTime() - startTime;
            Log.d(TAG, "zoom time the consumingTime is " + consumingTime); // 387731385 纳秒 ~ 300毫秒，有点慢
        }
    }



    private void clampBitmap() {
        int w = mInBmp.getWidth();
        int h = mInBmp.getHeight();
        int w8 = w;
        int h8 = h;
        if (w % 8 != 0 || h % 8 != 0) {
            w8 = w - w % 8;
            h8 = h - h % 8;
            mInBmp = mInBmp.createBitmap(mInBmp, 0, 0, w8, h8);
        }

        //部分平台的gpu只支持4096*4096分辨率，对图片进行缩放 ，这边demo现实统一全部都进行缩放
        if( w8 > 3120 || h8 > 4096 )
        {

            long startTime = System.nanoTime();

            float scalew =  ((float) 3120) / w8;
            float scaleh =  ((float) 4096) / h8;

            Matrix matrix = new Matrix();
            matrix.postScale(scalew,scaleh);

            mInBmpshow = Bitmap.createBitmap(mInBmp, 0, 0, w8, h8,matrix,true);

            long consumingTime = System.nanoTime() - startTime;
            Log.e("zoom time", "the consumingTime is " + consumingTime); // 387731385 纳秒 ~ 300毫秒，有点慢
        }
        else
            mInBmpshow = mInBmp;
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaurifyJniSdk.nativeReleaseResources();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        //ConUtil.toggleHideyBar(this);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    public static void toggleHideyBar(Activity activity) {
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;


        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

//        if (Build.VERSION.SDK_INT >= 16) {
//            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
//        }
//
        if (Build.VERSION.SDK_INT >= 19) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_GALLERY_CODE && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            String path = ImageConUtil.getRealPathFromURI(this, uri);
            mImageOrientation = ImageConUtil.getImageOrienata(path);
            mInBmp = ImageConUtil.getImagewithoutRotation(path);
            //mInBmp = getYUVBitMap(inNV21, mInBmp.getWidth(), mInBmp.getHeight());
            //clampBitmap();
            Bitmap InBmpWithOrientation = ImageConUtil.getImagewithRotation(path);
            if(mImageType == LaunchActivity.NV21_CODE){
                inNV21 = getNV21(InBmpWithOrientation);
                mInBmpshow = getYUVBitMap(inNV21, InBmpWithOrientation.getWidth(), InBmpWithOrientation.getHeight());
                clampShowBitmap();
            } else {
                mInBmpshow = InBmpWithOrientation;
                clampShowBitmap();
            }

            beaurifyJniSdk.nativeReleaseResources();
            beaurifyJniSdk.nativeSetLogLevel(beaurifyJniSdk.MG_LOG_LEVEL_DEBUG);
            beaurifyJniSdk.nativeCreateBeautyHandle(mContext, mInBmp.getWidth(),
                    mInBmp.getHeight(), mImageOrientation, Util.MG_FPP_DENSEDETECTIONMODE_FULL_SIZE,
                    ConUtil.getFileContent(mContext, R.raw.mgbeautify_1_2_4_model)
                    , ConUtil.getFileContent(mContext, R.raw.detect_model),
                    ConUtil.getFileContent(mContext, R.raw.dense_model));
            beaurifyJniSdk.nativeDoneGLContext();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mProcessImage.setImageBitmap(mInBmpshow);
                }
            });

            mOutBmp = null;
        }
    }


    public void processImageUnchecked(final int type) {
        if(mImageType == LaunchActivity.NV21_CODE){
            processImagenv21(type);
        }else {
            processImage();
        }
    }

    public void processImageFilter()
    {
        if (Util.isFilterChanged) {
            Util.isFilterChanged = false;
            processImage();
        }
    }

    private void requestGalleryPerm() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, GALLERY_CODE);
        } else {
            openGalleryActivity();
        }

    }


    private void openGalleryActivity() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_GALLERY_CODE);
    }


    @OnClick(R.id.ll_gallery)
    public void onSelectPicture() {
        requestGalleryPerm();
        Util.resetDefaultBeautifyParam();


        //滤镜处理
        Model.filterPosition = 0;
        Util.filterPath = "";

        if (radioGroup.getVisibility() == View.INVISIBLE) {
            radioGroup.setVisibility(View.VISIBLE);
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.remove(mCurrentFragment);
            fragmentTransaction.commit();
            fragmentContent.setVisibility(View.INVISIBLE);
        }
    }

    private void testFacePP(){
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                byte[] bmpByte = ConUtil.getPixelsRGBA(mInBmp);
                BeaurifyJniSdk.testFacePP(
                        ConUtil.getFileContent(mContext, R.raw.megviifacepp_model),
                        bmpByte, mInBmp.getWidth(), mInBmp.getHeight());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mProcessImage.setImageBitmap(mInBmp);
                        dialog.dismiss();
                    }
                });
            }
        }).start();
    }
    byte[] mImgData;
    private byte[] facePPProcessInternal(){
        //DebugYuvDumpUtil.dumpYUVImage(mImgData, null, mImgWidth, mImgHeight, "Face++Beauty_before");
        byte[] tempres = new byte[inNV21.length];
        mImgData = inNV21;
        int mImgWidth = nv21_width;
        int mImgHeight = nv21_height;

        BeaurifyJniSdk beautySdk = BeaurifyJniSdk.imageInstance();
        Log.v(TAG, "enter FPP beauty");
        beautySdk.nativeCreateBeautyHandle(mContext, mImgWidth,
                mImgHeight, 270, Util.MG_FPP_DENSEDETECTIONMODE_FULL_SIZE,
                ConUtil.getFileContent(mContext, R.raw.mgbeautify_1_2_4_model)
                , ConUtil.getFileContent(mContext, R.raw.detect_model),
                ConUtil.getFileContent(mContext, R.raw.dense_model)
                );

        beautySdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_DENOISE, (float)10);
       // beautySdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS, (float)6);
        Log.v(TAG, "setDenioseLevel & setBrightnessLevel= " + (float)6);
        Log.i(TAG, "Francine, mImgWidth = " + mImgWidth + " mImgHeight = " + mImgHeight);
        beautySdk.nativeProcessImageNV21(mImgData, tempres, mImgWidth, mImgHeight);
        mImgData = tempres;
        tempres = null;
        //DebugYuvDumpUtil.dumpYUVImage(mImgData, null, mImgWidth, mImgHeight, "Face++Beauty_After");
        Log.v(TAG, "leave FPP beauty");
        beautySdk.nativeReleaseResources();
        return mImgData;
    }

    public void processImagenv21Test(final int type) {
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {

                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                facePPProcessInternal();

                byte[] outNV21 = mImgData;

                //mInBmp = getYUVBitMap(inNV21, width, height);
                mOutBmp = getYUVBitMap(outNV21, nv21_width, nv21_height);

                showOutBimtap();

                //beaurifyJniSdk.nativeReleaseResources();
            }
        }).start();
    }


    public void processImagenv21(final int type) {
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {


                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                int width = nv21_width;
                int height = nv21_height;

                // beaurifyJniSdk.prepareGLContext();
                //mOutBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                //byte[] inNV21 = getNV21(width, height, mInBmp);
                beaurifyJniSdk.nativeCreateBeautyHandle(mContext, width,
                        height, 0, Util.MG_FPP_DENSEDETECTIONMODE_FULL_SIZE,
                        ConUtil.getFileContent(mContext, R.raw.mgbeautify_1_2_4_model)
                        , ConUtil.getFileContent(mContext, R.raw.detect_model),
                        ConUtil.getFileContent(mContext, R.raw.dense_model));

                beaurifyJniSdk.nativeSetBeautyParam(type, mCurrentChoose);

                byte[] outNV21 = new byte[inNV21.length];

                beaurifyJniSdk.nativeProcessImageNV21(inNV21, outNV21, width, height);

                //mInBmp = getYUVBitMap(inNV21, width, height);
                mOutBmp = getYUVBitMap(outNV21, width, height);

                showOutBimtap();

                beaurifyJniSdk.nativeReleaseResources();
            }
        }).start();
    }

    /**
     * @param type
     *BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS = 3;
     *BeaurifyJniSdk.MG_BEAUTIFY_DENOISE = 4;
     *BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK = 5;
     */
    public void processImage() {
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
               // beaurifyJniSdk.prepareGLContext();
                if(mOutBmp == null){
                    mOutBmp = mInBmp.copy(mInBmp.getConfig(), mInBmp.isMutable());
                }


                //byte[] bmpByte = ConUtil.getPixelsRGBA(mInBmp);
                //beaurifyJniSdk.nativeDetectFace(bmpByte, mInBmp.getWidth(), mInBmp.getHeight(), Util.MG_IMAGEMODE_RGBA);
                beaurifyJniSdk.nativeShareGLContext();
                if (TextUtils.isEmpty(Util.filterPath)) {
                    beaurifyJniSdk.nativeRemoveFilter();
                } else {
                    beaurifyJniSdk.nativeSetFilter(Util.filterPath);
                }

                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_DENOISE, Util.CURRENT_MG_BEAUTIFY_DENOISE);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS, Util.CURRENT_MG_BEAUTIFY_BRIGHTNESS);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTEN_EYE,Util.CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_TOOTH,Util.CURRENT_MG_BEAUTIFY_TOOTH);
                //祛斑
                if((Util.CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES-0.0f)>0.01){
                    //拷贝随机森林树至cache目录
                    String outPathName = Util.mRandomFrestModelPath+"trained_rt_model.dat";
                    FileUtil.copyDataFromRaw2Path(mContext,"trained_rt_model.dat",outPathName);
                    beaurifyJniSdk.nativeSetBeautyRemoveSpeckles(Util.CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES,ConUtil.getFileContent(mContext, R.raw.muvar),Util.mRandomFrestModelPath);
                }
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBAGS, Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBAGS);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_HIGH_NOSEBRIDGE, Util.CURRENT_MG_BEAUTIFY_HIGH_NOSE_BRIDGE);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_UPCHEEK,Util.CURRENT_MG_BEAUTIFY_UP_CHEEK);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SKIN_BALANCE, Util.CURRENT_MG_BEAUTIFY_SKIN_BALANCE);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK, Util.CURRENT_MG_BEAUTIFY_ADD_PINK);


                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SHRINK_FACE, Util.CURRENT_MG_BEAUTIFY_SHRINK_FACE);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ENLARGE_EYE, Util.CURRENT_MG_BEAUTIFY_ENLARGE_EYE);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_THIN_FACE, Util.CURRENT_MG_BEAUTIFY_THIN_FACE);
                beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBROW, Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW);
                beaurifyJniSdk.nativeSetBeautyParam2(beaurifyJniSdk.MG_BEAUTIFY_EYEBROW,Util.CURRENT_MG_BEAUTIFY_EYEBROW,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_R,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_G,Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_B,
                        Util.DEFAULT_EYEBROW_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX],null,
                        Util.DEFAULT_EYEBROW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_EYEBROW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX].mKeyPointsSize);

                beaurifyJniSdk.nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_ADD_BLUSH,Util.CURRENT_MG_BEAUTIFY_BLUSH,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G,Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B,
                        Util.DEFAULT_BLUSH_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX],null,
                        Util.DEFAULT_BLUSH_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_BLUSH_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX].mKeyPointsSize);

                beaurifyJniSdk.nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_EYESHADOW,Util.CURRENT_MG_BEAUTIFY_EYESHADOW,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G,Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B,
                        Util.DEFAULT_EYESHADOW_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX],null,
                        Util.DEFAULT_EYESHADOW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_EYESHADOW_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX].mKeyPointsSize);

                Pair<Bitmap[],Bitmap[]> shadingTemplate = Util.DEFAULT_SHADING_TEMPLATE.get(Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX);
                beaurifyJniSdk.nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_SHADING, Util.CURRENT_MG_BEAUTIFY_SHADING,0,0,0,
                        shadingTemplate.first,shadingTemplate.second,
                        Util.DEFAULT_SHADING_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX].mKeyPoints,
                        Util.DEFAULT_SHADING_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX].mKeyPointsSize);

                beaurifyJniSdk.nativeSetBeautyParam2(beaurifyJniSdk.MG_BEAUTIFY_CONTACT_LENS,Util.CURRENT_MG_BEAUTIFY_CONTACTLENS,0,0,0,Util.DEFAULT_CONTACT_LENS_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX],null,null,0);
                beaurifyJniSdk.nativeSetBeautyParam2(beaurifyJniSdk.MG_BEAUTIFY_LIP,Util.CURRENT_MG_BEAUTIFY_LIP,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_R,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_G,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_B,null,null,null,0);

                long startTime = System.currentTimeMillis();
                beaurifyJniSdk.nativeProcessImage(mInBmp, mOutBmp);
                long endTime = System.currentTimeMillis();
                Log.i("ProcessImage", "ProcessImage: "+(endTime - startTime)+" ms");
                //byte[] mImBmpNV21TempIn = getNV21(mInBmp);
                //byte[] mImBmpNV21TempOut = new byte[mImBmpNV21TempIn.length];
                //beaurifyJniSdk.nativeProcessImageNV21(mImBmpNV21TempIn,mImBmpNV21TempOut,nv21_width,nv21_height);
                //mOutBmp = getYUVBitMap(mImBmpNV21TempOut,nv21_width,nv21_height);
                showOutBimtap();
                beaurifyJniSdk.nativeDoneGLContext();

            }
        }).start();
    }



    private void showOutBimtap(){
        int w = mOutBmp.getWidth();
        int h = mOutBmp.getHeight();
        Bitmap moutBmpshowTemp;
        int max[] = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, max,0);
        //部分平台的gpu只支持4096*4096分辨率，对图片进行缩放 ，这边demo现实统一全部都进行缩放
        if( w  > max[0] || h > max[0] )
        {

            long startTime = System.nanoTime();

            float scalew =  ((float) max[0]) / w;
            float scaleh =  ((float) max[0]) / h;
            float scale = scalew < scaleh ? scalew : scaleh;

            Matrix matrix = new Matrix();
            matrix.postScale(scale,scale);

            moutBmpshowTemp = Bitmap.createBitmap(mOutBmp, 0, 0, w, h,matrix,true);

            long consumingTime = System.nanoTime() - startTime;
            Log.e("zoom time", "the consumingTime is " + consumingTime); // 387731385 纳秒 ~ 300毫秒，有点慢
        }else{
            moutBmpshowTemp = mOutBmp;
        }

        if(mImageOrientation != 0){
            moutBmpshow = ImageConUtil.getImagewithRotation(moutBmpshowTemp,mImageOrientation);
        }
        else
        {
            moutBmpshow = moutBmpshowTemp;
        }

        //beaurifyJniSdk.releaseGLContext();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessImage.setImageBitmap(moutBmpshow);
                dialog.dismiss();
            }
        });
    }

    @OnClick(R.id.ll_download)
    public void onSavePicture() {
        saveBitmap();
        mSaveFlag = true;

    }

    private boolean mSaveFlag = false;

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (requestCode == GALLERY_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT);
            } else if (mSaveFlag) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        permissionSaveBitmap();
                    }
                }).start();

            }
        }
    }

    public void saveBitmap() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, PostProcessActivity.GALLERY_CODE);
        } else {
            permissionSaveBitmap();
        }
    }

    public void permissionSaveBitmap() {
        dialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap saveImage = moutBmpshow;
                if(saveImage == null){
                    saveImage = mInBmp;
                }

                mSaveFlag = false;
                String bitmapFileName = System.currentTimeMillis() + ".jpg";
                ImageUtils.saveImageToGallery(PostProcessActivity.this, saveImage, bitmapFileName);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(PostProcessActivity.this, R.string.text_saved, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });



//                ConUtil.saveBitmap(mOutBmp); //保存时还是原尺寸图片
            }
        }).start();

    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mProcessImage.setImageBitmap(mInBmpshow);
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mOutBmp != null) {
                mProcessImage.setImageBitmap(moutBmpshow);//显示用缩放后的
            }
            return true;
        }
        return false;
    }


    boolean isTest = false;
    public byte[] getNV21(Bitmap scaled) {

        if(!isTest) {

            int inputWidth = scaled.getWidth();
            int inputHeight = scaled.getHeight();
            nv21_width = inputWidth;
            nv21_height = inputHeight;

            int[] argb = new int[inputWidth * inputHeight];

            scaled.getPixels(argb, 0, inputWidth, 0, 0, inputWidth, inputHeight);

            byte[] yuv = new byte[inputWidth * inputHeight * 3 / 2];
            encodeYUV420SP(yuv, argb, inputWidth, inputHeight);

            //scaled.recycle();

            return yuv;
        }



        nv21_width = 3264;
        nv21_height = 2448;
        byte[] nv12_nv21 =  ConUtil.getFileContent(mContext, R.raw.beauty_green);
        /**
        int start = nv21_width*nv21_height;
        for(int i = start; i < nv12_nv21.length; i += 2){
            byte temp = nv12_nv21[i];
            nv12_nv21[i] = nv12_nv21[i + 1];
            nv12_nv21[i + 1] = temp;
        }**/
        return nv12_nv21;
    }


    public static void encodeYUV420SP(byte[] yuv420sp, int[] argb, int width, int height) {

        final int frameSize = width * height;

        int yIndex = 0;
        int uvIndex = frameSize;

        int a, R, G, B, Y, U, V;
        int index = 0;
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {

                a = (argb[index] & 0xff000000) >> 24; // a is not used obviously
                R = (argb[index] & 0xff0000) >> 16;
                G = (argb[index] & 0xff00) >> 8;
                B = (argb[index] & 0xff) >> 0;

                // well known RGB to YUV algorithm
                Y = ((66 * R + 129 * G + 25 * B + 128) >> 8) + 16;
                U = ((-38 * R - 74 * G + 112 * B + 128) >> 8) + 128;
                V = ((112 * R - 94 * G - 18 * B + 128) >> 8) + 128;

                // NV21 has a plane of Y and interleaved planes of VU each sampled by a factor of 2
                //    meaning for every 4 Y pixels there are 1 V and 1 U.  Note the sampling is
                // every other
                //    pixel AND every other scanline.
                yuv420sp[yIndex++] = (byte) ((Y < 0) ? 0 : ((Y > 255) ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0) {
                    yuv420sp[uvIndex++] = (byte) ((V < 0) ? 0 : ((V > 255) ? 255 : V));
                    yuv420sp[uvIndex++] = (byte) ((U < 0) ? 0 : ((U > 255) ? 255 : U));
                }

                index++;
            }
        }
    }

    public static Bitmap getYUVBitMap(byte[] data, int width, int height) {
        YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, byteArrayOutputStream);
        byte[] jpegData = byteArrayOutputStream.toByteArray();
        // 获取照相后的bitmap
        Bitmap tmpBitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.length);
        tmpBitmap = tmpBitmap.copy(Bitmap.Config.ARGB_8888, true);
        return tmpBitmap;
    }

}
