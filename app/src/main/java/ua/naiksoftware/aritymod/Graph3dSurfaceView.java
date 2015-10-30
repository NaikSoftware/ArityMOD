package ua.naiksoftware.aritymod;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.View;

import org.javia.arity.Function;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 3D graphics
 */
public class Graph3dSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, Grapher {

    private int width, height;
    private GL10 gl;
    private Function function;

    public Graph3dSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Graph3dSurfaceView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        this.gl = gl;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.width = width;
        this.height = height;
        gl.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        gl.glClearColor(0, 0, 0, 1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
    }

    @Override
    public void setFunction(Function f) {
        this.function = f;
    }

    @Override
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
        bitmap.setPixels(data, size - width, -width, 0, 0, width, height);
        return bitmap;
    }

    @Override
    public View getView() {
        return this;
    }
}
