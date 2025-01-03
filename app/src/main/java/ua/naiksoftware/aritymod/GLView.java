// Copyright (C) 2009 Mihai Preda

package ua.naiksoftware.aritymod;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

abstract class GLView extends SurfaceView implements SurfaceHolder.Callback {
    private boolean hasSurface;
    private boolean paused;
    private EGL10 egl;
    private EGLDisplay display;
    private EGLConfig config;
    private EGLSurface surface;
    private EGLContext eglContext;
    private GL11 gl;
    protected int width, height;
    private boolean mIsLooping;
    private UIHandler handler = new UIHandler(new WeakReference<>(this));

    abstract void onDrawFrame(GL10 gl);
    abstract void onSurfaceCreated(GL10 gl, int width, int height);

    public String captureScreenshot() {
        Bitmap bitmap = getRawPixels(gl, width, height);
        Util.bitmapBGRtoRGB(bitmap, width, height);
        return Util.saveBitmap(bitmap, Grapher.SCREENSHOT_DIR, "calculator");
    }

    private static Bitmap getRawPixels(GL10 gl, int width, int height) {
        int size = width * height;
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        gl.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, buf);
        int data[] = new int[size];
        buf.asIntBuffer().get(data);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        bitmap.setPixels(data, size-width, -width, 0, 0, width, height);
        return bitmap;
    }

    private static final class UIHandler extends Handler {
        WeakReference<GLView> glView;
        private UIHandler(WeakReference<GLView> reference) {
            this.glView = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            glView.get().glDraw();
        }
    }

    public GLView(Context context) {
        super(context);
        init();
    }

    public GLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }

    public void onResume() {
        MainActivity.log("onResume " + this);
        paused = false;
        if (hasSurface) {
            initGL();
        }
    }

    public void onPause() {
        MainActivity.log("onPause " + this);
        deinitGL();
    }

    private void initGL() {
        egl = (EGL10) EGLContext.getEGL();
        display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
        int[] ver = new int[2];
        egl.eglInitialize(display, ver);

        int[] configSpec = {EGL10.EGL_NONE};
        EGLConfig[] configOut = new EGLConfig[1];
        int[] nConfig = new int[1];
        egl.eglChooseConfig(display, configSpec, configOut, 1, nConfig);
        config = configOut[0];
        eglContext = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, null);
        surface = egl.eglCreateWindowSurface(display, config, getHolder(), null);
        egl.eglMakeCurrent(display, surface, surface, eglContext);
        gl = (GL11) eglContext.getGL();
        onSurfaceCreated(gl, width, height);
        requestDraw();
    }

    private void deinitGL() {
        paused = true;
        if (display != null) {
            egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
            egl.eglDestroySurface(display, surface);
            egl.eglDestroyContext(display, eglContext);
            egl.eglTerminate(display);

            egl = null;
            config = null;
            eglContext = null;
            surface = null;
            display = null;
            gl = null;
        }
    }

    protected void glDraw() {
        if (hasSurface && !paused) {
            onDrawFrame(gl);
            if (!egl.eglSwapBuffers(display, surface)) {
                MainActivity.log("swapBuffers error " + egl.eglGetError());
            }
            if (egl.eglGetError() == EGL11.EGL_CONTEXT_LOST) {
                MainActivity.log("egl context lost " + this);
                paused = true;
            }
            if (mIsLooping) {
                requestDraw();
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        MainActivity.log("surfaceCreated " + this);
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        MainActivity.log("surfaceChanged " + format + ' ' + this);
        this.width  = width;
        this.height = height;
        boolean doInit = !hasSurface && !paused;
        hasSurface = true;
        if (doInit) {
            initGL();
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        MainActivity.log("surfaceDestroyed " + this);
        hasSurface = false;
        deinitGL();
    }

    public void startLooping() {
        if (!mIsLooping) {
            MainActivity.log("start looping");
            mIsLooping = true;
            glDraw();
        }
    }

    public void stopLooping() {
        if (mIsLooping) {
            MainActivity.log("stop looping");
            mIsLooping = false;
        }
    }

    public boolean isLooping() {
        return mIsLooping;
    }

    public void requestDraw() {
        handler.sendEmptyMessage(1);
    }
}
