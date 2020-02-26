package com.megvii.beautify.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.ImageFormat;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicResize;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;

public class ScriptInsics {

    public static YUVScaler createYUVScaler(){
        return new YUVScaler();
    }

    public static YUV_2_RGB_Decoder createNV21_2RGB(){
        return new YUV_2_RGB_Decoder();
    }
    @TargetApi(Build.VERSION_CODES.KITKAT_WATCH)
    public static class YUVScaler{
        private Allocation in;
        private Allocation out;
        private ScriptIntrinsicResize sIns;
        RenderScript rs;
        private byte[] inData;
        private int inW;
        private int inH;
        private byte[] outData;
        private int outW;
        private int outH;
        public void setIn(byte[] inData, int inW, int inH){
            this.inData = inData;
            this.inW = inW;
            this.inH = inH;
        }
        public void setOut(byte[] outData,int outW, int outH){
            this.outData = outData;
            this.outW = outW;
            this.outH = outH;
        }

        public void process(Context context){
            if (sIns == null){
                rs = RenderScript.create(context);
                sIns = ScriptIntrinsicResize.create(rs);
                Type.Builder yuvTypeBuilder = new Type.Builder(rs, Element.U8(rs));
                yuvTypeBuilder.setX(inW);
                yuvTypeBuilder.setY(inH);
                yuvTypeBuilder.setZ(1);
                in = Allocation.createTyped(rs, yuvTypeBuilder.create()
                        , Allocation.USAGE_SCRIPT | Allocation.USAGE_IO_INPUT);
                yuvTypeBuilder.setX(outW);
                yuvTypeBuilder.setY(outH);
                yuvTypeBuilder.setZ(1);
                out = Allocation.createTyped(rs, yuvTypeBuilder.create(), Allocation.USAGE_SCRIPT);
            }
            in.copyFrom(inData);
            sIns.setInput(in);
            sIns.forEach_bicubic(out);
            out.copyTo(outData);
        }
    }

    public static class YUV_2_RGB_Decoder{
        Allocation in;
        Allocation out;
        ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
        RenderScript rs;
        public void GPUDecodeYUV420SPRGB(Context context, byte[] rgb
                , byte[] yuv, int W, int H){
            if(yuvToRgbIntrinsic == null) {
                rs = RenderScript.create(context);
                yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
                Type.Builder yuvType = new Type.Builder(rs, Element.U8(rs)).setX(yuv.length);
                in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);

                Type.Builder rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(W).setY(H);
                out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
            }
            in.copyFrom(yuv);
            yuvToRgbIntrinsic.setInput(in);
            yuvToRgbIntrinsic.forEach(out);
            out.copyTo(rgb);
        }
    }
}
