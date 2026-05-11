package ua.naiksoftware.aritymod;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import org.javia.arity.Function;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.opengles.GL11;

/**
 * 3D graphics surface for two-variable functions.
 */
public class Graph3dSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer, Grapher,
        TouchHandler.TouchHandlerInterface {

    private static final float DISTANCE = 15f;
    private static final float MIN_ZOOM = 0.2f;
    private static final float MAX_ZOOM = 5f;

    private final TouchHandler touchHandler = new TouchHandler(this);
    private final FPS fps = new FPS();
    private final float[] matrix1 = new float[16];
    private final float[] matrix2 = new float[16];
    private final float[] matrix3 = new float[16];

    private Graph3d graph;
    private volatile Function function;
    private volatile boolean functionDirty;

    private volatile float zoomLevel = 1f;
    private float currentZoom = -1f;
    private volatile float angleX, angleY;

    private float lastTouchX, lastTouchY;
    private float pinchStartDist;
    private float pinchStartZoom;

    private float downX, downY;
    private boolean movedSinceDown;
    private final int touchSlop;

    private int width, height;

    public Graph3dSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        init();
    }

    public Graph3dSurfaceView(Context context) {
        super(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        init();
    }

    private void init() {
        setRenderer(this);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
        Matrix.setIdentityM(matrix1, 0);
        Matrix.rotateM(matrix1, 0, -75, 1, 0, 0);
    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public void setFunction(Function f) {
        this.function = f;
        functionDirty = true;
        zoomLevel = 1f;
        angleX = 0.5f;
        angleY = 0f;
        Matrix.setIdentityM(matrix1, 0);
        Matrix.rotateM(matrix1, 0, -75, 1, 0, 0);
        requestRender();
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glDisable(GL10.GL_DITHER);
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
        gl.glClearColor(0, 0, 0, 1);
        gl.glShadeModel(MainActivity.useHighQuality3d ? GL10.GL_SMOOTH : GL10.GL_FLAT);
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glLineWidth(1.5f);
        graph = new Graph3d((GL11) gl);
        functionDirty = true;
        currentZoom = -1f;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int w, int h) {
        this.width = w;
        this.height = h;
        gl.glViewport(0, 0, w, h);
        initFrustum(gl, DISTANCE * zoomLevel);
        currentZoom = zoomLevel;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GL11 gl = (GL11) gl10;

        float zoom = zoomLevel;
        if (currentZoom != zoom) {
            initFrustum(gl, DISTANCE * zoom);
            currentZoom = zoom;
        }

        if (functionDirty) {
            graph.update(gl, function, zoom);
            functionDirty = false;
        }

        if (fps.incFrame()) {
            MainActivity.log("3d f/s " + fps.getValue());
        }

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -DISTANCE * zoom);

        Matrix.setIdentityM(matrix2, 0);
        float ax = Math.abs(angleX);
        float ay = Math.abs(angleY);
        if (ay * 3 < ax) {
            Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
        } else if (ax * 3 < ay) {
            Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
        } else {
            if (ax > ay) {
                Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
                Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
            } else {
                Matrix.rotateM(matrix2, 0, angleY, 1, 0, 0);
                Matrix.rotateM(matrix2, 0, angleX, 0, 1, 0);
            }
        }
        Matrix.multiplyMM(matrix3, 0, matrix2, 0, matrix1, 0);
        gl.glMultMatrixf(matrix3, 0);
        System.arraycopy(matrix3, 0, matrix1, 0, 16);
        graph.draw(gl);

        if (shouldKeepRotating()) {
            angleX *= 0.92f;
            angleY *= 0.92f;
            requestRender();
        } else {
            angleX = 0;
            angleY = 0;
        }
    }

    private boolean shouldKeepRotating() {
        final float limit = 0.5f;
        return angleX < -limit || angleX > limit || angleY < -limit || angleY > limit;
    }

    private void initFrustum(GL10 gl, float distance) {
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        float near = distance * (1f / 3f);
        float far = distance * 3f;
        float dimen = near / 5f;
        int w = Math.max(1, width);
        int h = Math.max(1, height);
        float frustH = dimen * h / w;
        gl.glFrustumf(-dimen, dimen, -frustH, frustH, near, far);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_DOWN) {
            downX = event.getX();
            downY = event.getY();
            movedSinceDown = false;
        } else if (action == MotionEvent.ACTION_MOVE && !movedSinceDown) {
            float dx = event.getX() - downX;
            float dy = event.getY() - downY;
            if (dx * dx + dy * dy > touchSlop * touchSlop) {
                movedSinceDown = true;
            }
        } else if (action == MotionEvent.ACTION_UP && !movedSinceDown) {
            performClick();
        }
        return touchHandler.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public void onTouchDown(float x, float y) {
        lastTouchX = x;
        lastTouchY = y;
        angleX = 0;
        angleY = 0;
    }

    @Override
    public void onTouchMove(float x, float y) {
        float deltaX = x - lastTouchX;
        float deltaY = y - lastTouchY;
        if (deltaX * deltaX + deltaY * deltaY > 1f) {
            angleX = deltaX;
            angleY = deltaY;
            lastTouchX = x;
            lastTouchY = y;
            requestRender();
        }
    }

    @Override
    public void onTouchUp(float x, float y) {
        float vx = touchHandler.velocityTracker.getXVelocity() / 100f;
        float vy = touchHandler.velocityTracker.getYVelocity() / 100f;
        angleX = vx;
        angleY = vy;
        if (shouldKeepRotating()) {
            requestRender();
        } else {
            angleX = 0;
            angleY = 0;
        }
    }

    @Override
    public void onTouchZoomDown(float x1, float y1, float x2, float y2) {
        pinchStartDist = (float) Math.hypot(x2 - x1, y2 - y1);
        pinchStartZoom = zoomLevel;
    }

    @Override
    public void onTouchZoomMove(float x1, float y1, float x2, float y2) {
        if (pinchStartDist <= 0) return;
        float dist = (float) Math.hypot(x2 - x1, y2 - y1);
        if (dist <= 0) return;
        float ratio = pinchStartDist / dist;
        float newZoom = pinchStartZoom * ratio;
        newZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newZoom));
        if (newZoom != zoomLevel) {
            zoomLevel = newZoom;
            functionDirty = true;
            requestRender();
        }
    }

    @Override
    public String captureScreenshot() {
        if (width <= 0 || height <= 0) return null;
        final int w = width;
        final int h = height;
        final AtomicReference<Bitmap> bitmapRef = new AtomicReference<>();
        final CountDownLatch latch = new CountDownLatch(1);
        queueEvent(new Runnable() {
            @Override
            public void run() {
                bitmapRef.set(getRawPixels(w, h));
                latch.countDown();
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
        Bitmap bitmap = bitmapRef.get();
        if (bitmap == null) return null;
        Util.bitmapBGRtoRGB(bitmap, w, h);
        return Util.saveBitmap(bitmap, Grapher.SCREENSHOT_DIR, "calculator");
    }

    private static Bitmap getRawPixels(int w, int h) {
        int size = w * h;
        ByteBuffer buf = ByteBuffer.allocateDirect(size * 4);
        buf.order(ByteOrder.nativeOrder());
        GLES10.glReadPixels(0, 0, w, h, GLES10.GL_RGBA, GLES10.GL_UNSIGNED_BYTE, buf);
        int[] data = new int[size];
        buf.asIntBuffer().get(data);
        Bitmap bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
        bitmap.setPixels(data, size - w, -w, 0, 0, w, h);
        return bitmap;
    }
}
