// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod.core.util;

import android.graphics.Bitmap;
import android.os.Environment;

import java.nio.ShortBuffer;
import java.io.*;

import timber.log.Timber;

public final class Util {

    public static String saveBitmap(Bitmap bitmap, String dir, String baseName) {
        try {
            File sdcard = Environment.getExternalStorageDirectory();
            File pictureDir = new File(sdcard, dir);
            pictureDir.mkdirs();
            File f = null;
            for (int i = 1; i < 200; ++i) {
                String name = baseName + i + ".png";
                f = new File(pictureDir, name);
                if (!f.exists()) {
                    break;
                }
            }
            if (!f.exists()) {
                String name = f.getAbsolutePath();
                FileOutputStream fos = new FileOutputStream(name);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.flush();
                fos.close();
                return name;
            }
        } catch (Exception e) {
            Timber.e(e, "Exception saving screenshot");
        }
        return null;
    }

    public static void bitmapBGRtoRGB(Bitmap bitmap, int width, int height) {
        int size = width * height;
        short[] data = new short[size];
        ShortBuffer buf = ShortBuffer.wrap(data);
        bitmap.copyPixelsToBuffer(buf);
        for (int i = 0; i < size; ++i) {
            //BGR-565 to RGB-565
            short v = data[i];
            data[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
        }
        buf.rewind();
        bitmap.copyPixelsFromBuffer(buf);
    }
}
