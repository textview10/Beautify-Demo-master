package com.megvii.beautify.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.opengl.GLES20;
import android.os.Bundle;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.megvii.beautify.BuildConfig;
import com.megvii.beautify.R;
import com.megvii.beautify.login.ILoginView;
import com.megvii.beautify.login.LoadingActivity;
import com.megvii.beautify.login.LoginPresenter;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.main.fragment.BeautyFragmentPreView;
import com.megvii.beautify.main.fragment.BeautyShapeFragment;
import com.megvii.beautify.model.Model;
import com.megvii.beautify.util.ConUtil;
import com.megvii.beautify.util.FileUtil;
import com.megvii.beautify.util.ImageConUtil;
import com.megvii.beautify.util.ImageUtils;
import com.megvii.beautify.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class LaunchActivity extends Activity {
    public static final int CAMERA_CODE = 100;
    public static final int GALLERY_CODE = 101;
    public static final int NV21_CODE = 102;

    private static DocumentBuilderFactory dbFactory = null;
    private static DocumentBuilder db = null;
    private static Document document = null;

    private LoginPresenter mPresenter;

    public String cameraPath;
    private Uri uriResult;
    private String[] mPermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest
            .permission.CAMERA};
    private List<String> mNoPermission = new ArrayList<>();

    public int mImageOrientation = 0;

    @BindView(R.id.btn_gallery)
    //Button btn_gallery;
    View btn_gallery;

    @BindView(R.id.btn_camera)
    View btn_camera;

    @BindView(R.id.btn_batch_proc)
    View btn_batch_proc;

    private BeaurifyJniSdk beaurifyJniSdk;

    private TextView mVersionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        ButterKnife.bind(this);
        mVersionView = (TextView) findViewById(R.id.version);
        mVersionView.setText(BeaurifyJniSdk.preViewInstance().nativeGetBeautyVersion());
        mPresenter = new LoginPresenter();
        mPresenter.init();
        if(BuildConfig.BUILD_TYPE=="release"){
            btn_batch_proc.setVisibility(View.INVISIBLE);
        }

    }

    @OnClick(R.id.btn_camera)
    public void goCamera(View v) {
        Util.isTestHAL = false;
        requestCameraPerm(v);
        resetFilterParam();
        Util.resetDefaultBeautifyParam();
    }

    @OnClick(R.id.btn_gallery)
    public void goGallery(View v) {
        requestGalleryPerm(GALLERY_CODE);
        resetFilterParam();
        Util.resetDefaultBeautifyParam();
    }

    @OnClick(R.id.btn_batch_proc)
    public void batchprocess(View v) {

        beaurifyJniSdk = BeaurifyJniSdk.imageInstance();
        beaurifyJniSdk.nativeReleaseResources();
        beaurifyJniSdk.nativeSetLogLevel(beaurifyJniSdk.MG_LOG_LEVEL_DEBUG);
        beaurifyJniSdk.nativeCreateBeautyHandle(this, 0,
                0, 0, Util.MG_FPP_DENSEDETECTIONMODE_FULL_SIZE,
                ConUtil.getFileContent(this, R.raw.mgbeautify_1_2_4_model)
                , ConUtil.getFileContent(this, R.raw.detect_model),
                ConUtil.getFileContent(this,R.raw.dense_model)
        );
        beaurifyJniSdk.nativeDoneGLContext();

        Util.isPreView = false;

        //初始化美瞳，染眉模板
        Util.initTemplates(getApplicationContext());
        //读取配置文件，设置参数
        readParamFileAndSet("file:///sdcard/megvii_batch_proc/BeautifyParams.xml");

        onFolderBatchProcess("/sdcard/megvii_batch_proc");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaurifyJniSdk.nativeReleaseResources();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == CAMERA_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted
                Toast.makeText(this, R.string.err_camera, Toast.LENGTH_SHORT);
            } else {
                startPreview();
            }
        } else if (requestCode == GALLERY_CODE
                || requestCode == NV21_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted
                Toast.makeText(this, R.string.err_gallery, Toast.LENGTH_SHORT);
            } else {
                openGalleryActivity(requestCode);
            }
        }
    }

    public void requestGalleryPerm(int  code) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, code);
        } else {
            openGalleryActivity(code);
        }
    }


    public void requestCameraPerm(View view) {
        mNoPermission.clear();
        for (int i = 0; i < mPermission.length; i++) {
            if (ContextCompat.checkSelfPermission(this, mPermission[i]) != PackageManager
                    .PERMISSION_GRANTED) {
                mNoPermission.add(mPermission[i]);
            }
        }
        if (mNoPermission.isEmpty()) {
            startPreview();
        } else {
            String[] permission = mNoPermission.toArray(new String[mNoPermission.size()]);
            ActivityCompat.requestPermissions(this, permission, CAMERA_CODE);
        }
    }



    private void openGalleryActivity(int code) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, code);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.i("wangshuai", "onActivityResult" + resultCode + data + cameraPath);
        switch (requestCode) {
            case GALLERY_CODE:
            case NV21_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    uriResult = data.getData();
                    startImage(requestCode);
                }
                break;
            default:
                break;
        }
    }

    private void startPreview(){
        Intent intent = new Intent();
        intent.setClass(this, LoadingActivity.class);
        startActivity(intent);
    }
    public void startImage(int code) {
//        final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
//        dialog.show();
        Intent intent = new Intent(this, PostProcessActivity.class);
        intent.putExtra("imgurl", uriResult);
        intent.putExtra("image_type", code);
        startActivity(intent);

    }

    private void resetFilterParam(){
        //滤镜处理
        Model.filterPosition = 0;
        Util.filterPath = "";
    }


    private Bitmap  mInBmp, mOutBmp,moutBmpshow;
    public void onFolderBatchProcess(final String folder){
        new Thread(new Runnable() {
            @Override
            public void run() {
                File parent = new File(folder);
                final String[] children = parent.list();
                if(children != null) {
                    for (int index = 0; index < children.length; index++) {
                        Log.i(this.getClass().getSimpleName(), "batch: " + folder + File.separator + children[index]);
                        if (mInBmp != null && !mInBmp.isRecycled()) {
                            mInBmp.recycle();
                            mInBmp = null;
                        }
                        String path = folder + File.separator + children[index];
                        mInBmp = ImageConUtil.getImagewithoutRotation(path);

                        if(mInBmp==null){
                            continue;
                        }
                        mImageOrientation = ImageConUtil.getImageOrienata(path);
                        if (mOutBmp != null && !mOutBmp.isRecycled()) {
                            mOutBmp.recycle();
                            mOutBmp = null;
                        }
                        mOutBmp = mInBmp.copy(mInBmp.getConfig(), mInBmp.isMutable());
                        showToast("Start processing: " + children[index]);
                        batchProcessImage(mInBmp, mOutBmp);
                        saveBitmap(children[index], moutBmpshow);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("Batch done!");
                    }
                });

            }
        }).start();
    }

    private void showToast(final String content) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LaunchActivity.this, content, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveBitmap(String fileName, Bitmap resultBmp) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission
                    .WRITE_EXTERNAL_STORAGE}, PostProcessActivity.GALLERY_CODE);
        } else {
            saveResultImage(fileName, resultBmp);
        }
    }


   private void saveResultImage(String  fileName, Bitmap resultBmp){
       StringBuffer strPrefix = new StringBuffer((fileName.substring(0 , fileName.length() - 4)));
       strPrefix.append("_");
       strPrefix.append("after");
       strPrefix.append(".jpg");
       ImageUtils.saveImageToGallery(LaunchActivity.this, resultBmp, strPrefix.toString());
   }

    public void batchProcessImage(Bitmap srcImage,Bitmap dstImage) {

        long startTime = System.currentTimeMillis();
        beaurifyJniSdk.nativeShareGLContext();
        beaurifyJniSdk.nativeReset(srcImage.getWidth(),srcImage.getHeight(),mImageOrientation);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_DENOISE, Util.CURRENT_MG_BEAUTIFY_DENOISE);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTNESS, Util.CURRENT_MG_BEAUTIFY_BRIGHTNESS);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_BRIGHTEN_EYE,Util.CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_TOOTH,Util.CURRENT_MG_BEAUTIFY_TOOTH);
        //祛斑
        if((Util.CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES-0.0f)>0.01){
            //拷贝随机森林树至cache目录
            String outPathName = Util.mRandomFrestModelPath+"trained_rt_model.dat";
            FileUtil.copyDataFromRaw2Path(this,"trained_rt_model.dat",outPathName);
            beaurifyJniSdk.nativeSetBeautyRemoveSpeckles(Util.CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES,ConUtil.getFileContent(this, R.raw.muvar),Util.mRandomFrestModelPath);
        }
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBAGS, Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBAGS);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_HIGH_NOSEBRIDGE, Util.CURRENT_MG_BEAUTIFY_HIGH_NOSE_BRIDGE);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_UPCHEEK,Util.CURRENT_MG_BEAUTIFY_UP_CHEEK);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SKIN_BALANCE, Util.CURRENT_MG_BEAUTIFY_SKIN_BALANCE);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ADD_PINK, Util.CURRENT_MG_BEAUTIFY_ADD_PINK);

        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_SHRINK_FACE, Util.CURRENT_MG_BEAUTIFY_SHRINK_FACE);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_ENLARGE_EYE, Util.CURRENT_MG_BEAUTIFY_ENLARGE_EYE);
        beaurifyJniSdk.nativeSetBeautyParam(BeaurifyJniSdk.MG_BEAUTIFY_THIN_FACE, Util.CURRENT_MG_BEAUTIFY_THIN_FACE);
        beaurifyJniSdk.nativeSetBeautyParam(beaurifyJniSdk.MG_BEAUTIFY_REMOVE_EYEBROW,Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW);

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

        beaurifyJniSdk.nativeSetBeautyParam2(BeaurifyJniSdk.MG_BEAUTIFY_SHADING, Util.CURRENT_MG_BEAUTIFY_SHADING,0,0,0,
                Util.DEFAULT_SHADING_TEMPLATE.get(Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX).first,
                Util.DEFAULT_SHADING_TEMPLATE.get(Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX).second,
                Util.DEFAULT_SHADING_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX].mKeyPoints,
                Util.DEFAULT_SHADING_KEYPOINTS[Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX].mKeyPointsSize);

        beaurifyJniSdk.nativeSetBeautyParam2(beaurifyJniSdk.MG_BEAUTIFY_CONTACT_LENS,Util.CURRENT_MG_BEAUTIFY_CONTACTLENS,0,0,0,Util.DEFAULT_CONTACT_LENS_TEMPLATE[Util.CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX],null,null,0);
        beaurifyJniSdk.nativeSetBeautyParam2(beaurifyJniSdk.MG_BEAUTIFY_LIP,Util.CURRENT_MG_BEAUTIFY_LIP,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_R,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_G,Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_B,null,null,null,0);


        beaurifyJniSdk.nativeProcessImage(srcImage, dstImage);
        adjustOutBimtap();
        beaurifyJniSdk.nativeDoneGLContext();

        long endTime = System.currentTimeMillis();
        Log.i("batchProcessImage", "batchProcessImage: "+(endTime - startTime)+" ms");
    }

    static{
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            db = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    private void readParamFileAndSet(final String ParamFile){
        float [] params = new float[10]; //8个参数容器
        try{
            //URI uri = URI.create(ParamFile);

            //将给定 URI 的内容解析为一个 XML 文档,并返回Document对象
            document = db.parse(ParamFile);
            //按文档顺序返回包含在文档中且具有给定标记名称的所有 Element 的 NodeList
            NodeList paramList = document.getElementsByTagName("param");
            //遍历param
            for(int i=0;i<paramList.getLength();i++){
                //获取第i个param结点
                org.w3c.dom.Node node = paramList.item(i);
                //获取第i个book的所有属性
                NamedNodeMap namedNodeMap = node.getAttributes();
                //获取已知名为id的属性值
                String id = namedNodeMap.getNamedItem("id").getTextContent();


                //获取param结点的子节点,包含了Test类型的换行
                NodeList cList = node.getChildNodes();//System.out.println(cList.getLength());9
                String confidense = "";
                String color = "";
                String type = "";

                //将一个confidense里面的属性加入数组
                for(int j=0;j<cList.getLength();j++){
                    org.w3c.dom.Node cNode = cList.item(j);
                    String nodeName = cNode.getNodeName();
                    switch(nodeName){
                        case "coefficient":
                            confidense = cNode.getTextContent();
                            break;
                        case "color":
                            color = cNode.getTextContent();
                            break;
                        case "type":
                            type = cNode.getTextContent();
                            break;
                    }
                }
                switch(id){
                    case "denoise":
                        Util.CURRENT_MG_BEAUTIFY_DENOISE = (new Float(confidense));
                        break;
                    case "brightness":
                        Util.CURRENT_MG_BEAUTIFY_BRIGHTNESS = (new Float(confidense));
                        break;
                    case "brightness_eye":
                        Util.CURRENT_MG_BEAUTIFY_BRIGHTEN_EYE = (new Float(confidense));
                        break;
                    case "teeth":
                        Util.CURRENT_MG_BEAUTIFY_TOOTH = (new Float(confidense));
                        break;
                    case "pink":
                        Util.CURRENT_MG_BEAUTIFY_ADD_PINK = (new Float(confidense));
                        break;
                    case "remove_speckles":
                        Util.CURRENT_MG_BEAUTIFY_REMOVE_SPECKLES = (new Float(confidense));
                        break;
                    case "remove_eyebags":
                        Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBAGS = (new Float(confidense));
                        break;
                    case "high_nosebridge":
                        Util.CURRENT_MG_BEAUTIFY_HIGH_NOSE_BRIDGE = (new Float(confidense));
                        break;
                    case "up_cheek":
                        Util.CURRENT_MG_BEAUTIFY_UP_CHEEK = (new Float(confidense));
                        break;
                    case "skin_balance":
                        Util.CURRENT_MG_BEAUTIFY_SKIN_BALANCE = (new Float(confidense));
                        break;
                    case "small_face":
                        Util.CURRENT_MG_BEAUTIFY_SHRINK_FACE = (new Float(confidense));
                        break;
                    case "enlarge_eye":
                        Util.CURRENT_MG_BEAUTIFY_ENLARGE_EYE = (new Float(confidense));
                        break;
                    case "thin_face":
                        Util.CURRENT_MG_BEAUTIFY_THIN_FACE = (new Float(confidense));
                        break;
                    case "remove_eyebrow":
                        Util.CURRENT_MG_BEAUTIFY_REMOVE_EYEBROW = (new Float(confidense));
                        break;
                    case "eyebrow": {
                        Util.CURRENT_MG_BEAUTIFY_EYEBROW = (new Float(confidense));
                        int colorIndex = (new Integer(color));
                        Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_R = Util.DEFAULT_EYEBROW_COLOR[colorIndex * 3];
                        Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_G = Util.DEFAULT_EYEBROW_COLOR[colorIndex * 3 + 1];
                        Util.CURRENT_MG_BEAUTIFY_EYEBROW_COLOR_B = Util.DEFAULT_EYEBROW_COLOR[colorIndex * 3 + 2];
                        Util.CURRENT_MG_BEAUTIFY_EYEBROW_TEMPLATE_INDEX = (new Integer(type));
                        break;
                    }
                    case "blush": {
                        Util.CURRENT_MG_BEAUTIFY_BLUSH = (new Float(confidense));
                        int colorIndex = (new Integer(color));
                        Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_R = Util.DEFAULT_BLUSH_COLOR[colorIndex * 3];
                        Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_G = Util.DEFAULT_BLUSH_COLOR[colorIndex * 3 + 1];
                        Util.CURRENT_MG_BEAUTIFY_BLUSH_COLOR_B = Util.DEFAULT_BLUSH_COLOR[colorIndex * 3 + 2];
                        Util.CURRENT_MG_BEAUTIFY_BLUSH_TEMPLATE_INDEX = (new Integer(type));
                        break;
                    }
                    case "eyeshadow": {
                        Util.CURRENT_MG_BEAUTIFY_EYESHADOW = (new Float(confidense));
                        int colorIndex = (new Integer(color));
                        Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_R = Util.DEFAULT_EYESHADOW_COLOR[colorIndex * 3];
                        Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_G = Util.DEFAULT_EYESHADOW_COLOR[colorIndex * 3 + 1];
                        Util.CURRENT_MG_BEAUTIFY_EYESHADOW_COLOR_B = Util.DEFAULT_EYESHADOW_COLOR[colorIndex * 3 + 2];
                        Util.CURRENT_MG_BEAUTIFY_EYESHADOW_TEMPLATE_INDEX = (new Integer(type));
                        break;
                    }
                    case "shading":{
                        Util.CURRENT_MG_BEAUTIFY_SHADING = (new Float(confidense));
                        Util.CURRENT_MG_BEAUTIFY_SHADING_TEMPLATE_INDEX = (new Integer(type));
                        break;
                    }
                    case "lip": {
                        Util.CURRENT_MG_BEAUTIFY_LIP = (new Float(confidense));
                        int colorIndex = (new Integer(color));
                        Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_R = Util.DEAFULT_LIP_COLOR[colorIndex*3];
                        Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_G = Util.DEAFULT_LIP_COLOR[colorIndex*3 + 1];
                        Util.CURRENT_MG_BEAUTIFY_LIP_COLOR_B = Util.DEAFULT_LIP_COLOR[colorIndex*3 + 2];
                        break;
                    }
                    case "contact_lens": {
                        Util.CURRENT_MG_BEAUTIFY_CONTACTLENS = (new Float(confidense));
                        Util.CURRENT_MG_BEAUTIFY_CONTACT_LENS_TEMPLATE_INDEX = (new Integer(type));
                        break;
                    }
                }

            }

        }catch (Exception e){
            Log.i("readParamFileAndSet","parse BeautifyParams.xml error,use default date");
        }

    }

    private void adjustOutBimtap() {
        int w = mOutBmp.getWidth();
        int h = mOutBmp.getHeight();
        Bitmap moutBmpshowTemp;
        int max[] = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, max, 0);
        //部分平台的gpu只支持4096*4096分辨率，对图片进行缩放 ，这边demo现实统一全部都进行缩放
        if (w > max[0] || h > max[0]) {

            long startTime = System.nanoTime();

            float scalew = ((float) max[0]) / w;
            float scaleh = ((float) max[0]) / h;
            float scale = scalew < scaleh ? scalew : scaleh;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            moutBmpshowTemp = Bitmap.createBitmap(mOutBmp, 0, 0, w, h, matrix, true);

            long consumingTime = System.nanoTime() - startTime;
            Log.e("zoom time", "the consumingTime is " + consumingTime); // 387731385 纳秒 ~ 300毫秒，有点慢
        } else {
            moutBmpshowTemp = mOutBmp;
        }

        if (mImageOrientation != 0) {
            moutBmpshow = ImageConUtil.getImagewithRotation(moutBmpshowTemp, mImageOrientation);
        } else {
            moutBmpshow = moutBmpshowTemp;
        }
    }
}
