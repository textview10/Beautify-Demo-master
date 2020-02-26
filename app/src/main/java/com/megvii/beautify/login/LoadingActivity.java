package com.megvii.beautify.login;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;


import com.megvii.beautify.R;

import com.megvii.beautify.main.MainActivity;
import com.megvii.beautify.util.DialogUtil;
import com.megvii.beautify.util.MLog;
import com.megvii.beautify.util.SysUtil;

import static android.os.Build.VERSION_CODES.M;


public class LoadingActivity extends Activity implements ILoginView {
    public static final int EXTERNAL_STORAGE_REQ_CAMERA_CODE = 10;
    private DialogUtil mDialogUtil;
    private LoginPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_layout);
        mDialogUtil = new DialogUtil(this);

        mPresenter = new LoginPresenter(this);
        mPresenter.init();

        Log.e("xie", "onCreate: "+Thread.currentThread().getId());


    }


    @Override
    public void initComplete() {
        requestCameraPerm();
    }

    private void requestCameraPerm() {
       // ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, LoadingActivity.EXTERNAL_STORAGE_REQ_CAMERA_CODE);
//        MLog.i("xie permission req"+ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA));
        if (android.os.Build.VERSION.SDK_INT >= M) {//无需判断也是可以的
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                //进行权限请求
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, LoadingActivity.EXTERNAL_STORAGE_REQ_CAMERA_CODE);
            } else if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED&&!SysUtil.checkCameraHasPerm()){
                mDialogUtil.showDialog("获取相机权限失败");
//                showSettingDialog("相机");
            }else{
                enterNextPage();
            }
        } else {
            enterNextPage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
//        MLog.i("xie permission result"+grantResults[0]);
        if (requestCode == EXTERNAL_STORAGE_REQ_CAMERA_CODE) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted
                mDialogUtil.showDialog("获取相机权限失败");
            } else {
                if (SysUtil.checkCameraHasPerm()){
                    enterNextPage();
                }else{
                    mDialogUtil.showDialog("获取相机权限失败");
                }

            }
        }
    }

//    public void showSettingDialog(String msg){
//        AlertDialog.Builder builder=new AlertDialog.Builder(this);
//        builder.setMessage("无"+msg+"权限，去设置里打开");
//        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                SysUtil.getAppDetailSettingIntent(LoadingActivity.this);
//                dialog.dismiss();
//            }
//        });
//        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });
//        builder.show();
//    }



    private void enterNextPage() {
        startActivity(new Intent(LoadingActivity.this, MainActivity.class));
        this.finish();
    }

}