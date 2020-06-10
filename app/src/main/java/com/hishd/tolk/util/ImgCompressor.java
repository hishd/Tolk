package com.hishd.tolk.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

public class ImgCompressor {
    public static byte[] comPressImageToBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream byteArrayOPStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, byteArrayOPStream);
            return byteArrayOPStream.toByteArray();
        }
        return null;
    }

}
