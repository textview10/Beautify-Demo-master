package com.megvii.beautify.component;

import android.os.Looper;

import com.megvii.beautify.app.Constant;
import com.megvii.beautify.jni.BeaurifyJniSdk;
import com.megvii.beautify.model.Model;
import com.megvii.beautify.model.BeautyDownEvent;
import com.megvii.beautify.model.http.DownStickerApis;
import com.megvii.beautify.model.http.RetrofitHelper;
import com.megvii.beautify.util.FileUtil;
import com.megvii.beautify.util.Util;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by xiejiantao on 2017/7/18.
 * 1.adapter的item的更新是通过eventbus根据tag刷新界面，有点耦合，观察者模式或者eventbus会不会好点
 * 2。model的数据保存在preference，贴纸的状态存放在数据库，前面的是静态数据，后面是网络动态的，后面会根据前面的做适配
 * 3.忽略前两条
 */

public class DownLoaderManager {


    public static final int STATE_NONE = 0;//未开始
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_DOWNLOADED = 2;

    private static DownLoaderManager manager;

    private ThreadPoolExecutor executor;


    private DownLoaderManager() {
//        Log.i("xie", "xie cpu size" + (Runtime.getRuntime().availableProcessors() * 2 + 1));
        executor = new ThreadPoolExecutor(
                Runtime.getRuntime().availableProcessors() * 2 + 1, //当某个核心任务执行完毕，会依次从缓冲队列中取出等待任务
                Runtime.getRuntime().availableProcessors() * 4 + 2, //5,先corePoolSize,然后new LinkedBlockingQueue<Runnable>(),然后maximumPoolSize,但是它的数量是包含了corePoolSize的
                1, //表示的是maximumPoolSize当中等待任务的存活时间
                TimeUnit.HOURS,
                new LinkedBlockingQueue<Runnable>(), //缓冲队列，用于存放等待任务，Linked的先进先出
                Executors.defaultThreadFactory(), //创建线程的工厂
                new ThreadPoolExecutor.AbortPolicy() //用来对超出maximumPoolSize的任务的处理策略
        );
    }

    public static DownLoaderManager getInstance() {
        if (manager == null) {
            synchronized (DownLoaderManager.class) {
                if (manager == null) {
                    manager = new DownLoaderManager();
                }
            }
        }
        return manager;
    }

    public void execute(Runnable runnable) {
        if (runnable == null) return;
        executor.execute(runnable);
    }

    public void remove(Runnable runnable) {
        if (runnable == null) return;
        executor.remove(runnable);
    }

    public void executeDownRunnable(Model model) {
        execute(downloadRunnable(model));
    }

    public boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public Runnable downloadRunnable(final Model model) {
        return new Runnable() {
            @Override
            public void run() {

//                Log.d("xie", "xie start main" + isMainThread());
                BeautyDownEvent event = new BeautyDownEvent();
                event.status = STATE_DOWNLOADING;
                event.titleChinese = model.titleChinese;
                event.titleEnglish = model.titleEnglish;
                model.status = STATE_DOWNLOADING;
                EventBus.getDefault().post(event);

                DownStickerApis apis = RetrofitHelper.getAPIService(DownStickerApis.class);
                String zipName;
                if (model.type== Util.TYPE_FILTER){
                    zipName=model.filter;
                }else{
                    zipName=model.zipName;
                }
                Call<ResponseBody> call = apis.downloadFileWithDynamicUrlAsync(Constant.BASE_URL + zipName);
                try {
                    Response<ResponseBody> response = call.execute();
//                    Log.d("xie", "xie res main" + isMainThread());
                    if (response.isSuccessful()) {
//                        Log.d("xie", "xie server contacted and has file");

                        boolean writtenToDisk = writeResponseBodyToDisk(response.body(),model);

                        if (writtenToDisk) {
                            event = new BeautyDownEvent();
                            event.status = STATE_DOWNLOADED;
                            model.status = STATE_DOWNLOADED;
                            event.titleChinese = model.titleChinese;
                            event.titleEnglish = model.titleEnglish;
                            if (model.type==Util.TYPE_STICKER){
                                BeaurifyJniSdk.preViewInstance().nativePreparePackage(Constant.sStickerDownloadPath + model.zipName);
                            }
                            EventBus.getDefault().post(event);

                        } else {
                            event = new BeautyDownEvent();
                            event.status = STATE_NONE;
                            model.status = STATE_NONE;
                            event.titleChinese = model.titleChinese;
                            event.titleEnglish = model.titleEnglish;
                            EventBus.getDefault().post(event);
                        }

//                        Log.d("xie", "xie file download was a success? " + writtenToDisk);
                    } else {
//                        Log.d("xie", "xie server contact failed");
                        event = new BeautyDownEvent();
                        event.status = STATE_NONE;
                        model.status = STATE_NONE;
                        event.titleChinese = model.titleChinese;
                        event.titleEnglish = model.titleEnglish;
                        EventBus.getDefault().post(event);
                    }


                } catch (IOException e) {
//                    Log.d("xie", "xie server io failed");
                    event = new BeautyDownEvent();
                    event.status = STATE_NONE;
                    model.status = STATE_NONE;
                    event.titleChinese = model.titleChinese;
                    event.titleEnglish = model.titleEnglish;
                    EventBus.getDefault().post(event);
                    e.printStackTrace();
                }

            }

            /**
             * 流的写入
             * @param body
             * @return
             */
            public static  final String TEMP_PATH="temp";
            private boolean writeResponseBodyToDisk(ResponseBody body,Model model) {
                try {
                    // todo change the file location/name according to your needs
                    String zipPath;
                    String tempPath;

                    if (model.type==Util.TYPE_STICKER){
                        zipPath=Constant.sStickerDownloadPath + model.zipName;
                    }else if (model.type==Util.TYPE_FILTER){
                        zipPath=Constant.sFilterDownloadPath + model.filter;
                    }else{
                        return false;
                    }
                    tempPath=zipPath+TEMP_PATH;

                    FileUtil.deleteFile(zipPath);
                    FileUtil.deleteFile(tempPath);

                    File tempZipFile = new File(tempPath);

                    InputStream inputStream = null;
                    OutputStream outputStream = null;

                    try {
                        byte[] fileReader = new byte[4096];

                        long fileSize = body.contentLength();
                        long fileSizeDownloaded = 0;

                        inputStream = body.byteStream();
                        outputStream = new FileOutputStream(tempZipFile);

                        while (true) {
                            int read = inputStream.read(fileReader);

                            if (read == -1) {
                                break;
                            }

                            outputStream.write(fileReader, 0, read);

                            fileSizeDownloaded += read;

//                            Log.d("xie", "xie file download: " + fileSizeDownloaded + " of " + fileSize);
                        }

                        outputStream.flush();
                        FileUtil.renameFile(tempPath,zipPath);
                        return true;
                    } catch (IOException e) {
                        return false;
                    } finally {
                        if (inputStream != null) {
                            inputStream.close();
                        }

                        if (outputStream != null) {
                            outputStream.close();
                        }
                    }
                } catch (IOException e) {
                    return false;
                }
            }


        };
    }


}
