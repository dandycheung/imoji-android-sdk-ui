package com.imojiapp.imojigraphics;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by thor on 29/08/15.
 */
public class IGEditorView extends GLSurfaceView implements GLSurfaceView.Renderer {
    private final Handler mHandler = new Handler();
    private Bitmap inputBitmap;
    private int igContext = 0;
    private int igInputImage = 0;
    private int igEditor = 0;
    private int surfaceWidth = 0, surfaceHeight = 0;
    private boolean zoomInOnAspectFit;
    private float density = 1.f;
    private Queue<Runnable> mGLQueue = new ConcurrentLinkedQueue<>();
    private Queue<Runnable> mEditorIndependentGLQueue = new ConcurrentLinkedQueue<>();
    private StateListener stateListener = null;
    private int state = IG.EDITOR_DRAW, substate = IG.EDITOR_IDLE;

    public IGEditorView(Context context) {
        this(context, null);
    }

    public IGEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        density = metrics.density;

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.IGEditorView);
        if (array.hasValue(R.styleable.IGEditorView_ig__drawable)) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) array.getDrawable(R.styleable.IGEditorView_ig__drawable);
            inputBitmap = bitmapDrawable.getBitmap();
        }

        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 8);
        getHolder().setFormat(PixelFormat.TRANSLUCENT);
        setRenderer(this);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        surfaceWidth = 0;
        surfaceHeight = 0;
        resetEditor();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        surfaceWidth = width;
        surfaceHeight = height;

        aspectFit();
    }

    @Override
    public void onDrawFrame(GL10 gl) {

        gl.glViewport(0, 0, surfaceWidth, surfaceHeight);

        while (!mEditorIndependentGLQueue.isEmpty()) {
            mEditorIndependentGLQueue.poll().run();
        }

        if (igEditor != 0) {

            while (!mGLQueue.isEmpty()) {
                mGLQueue.poll().run();
            }

            IG.EditorDisplay(igEditor);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(igEditor == 0) {
            return false;
        }

        int action = event.getActionMasked();
        for (int index = 0; index < event.getPointerCount(); index++) {
            final int id = event.getPointerId(index);
            final float x = event.getX(index) / density;
            final float y = event.getY(index) / density;

            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    if (event.getPointerCount() >= 2) {
                        break;
                    }
                case MotionEvent.ACTION_POINTER_DOWN:
                    mGLQueue.add(new Runnable() {
                        @Override
                        public void run() {
                            IG.EditorTouchEvent(igEditor, IG.TOUCH_BEGAN, id, x, y);
                        }
                    });
                    break;

                case MotionEvent.ACTION_MOVE:
                    mGLQueue.add(new Runnable() {
                        @Override
                        public void run() {
                            IG.EditorTouchEvent(igEditor, IG.TOUCH_MOVED, id, x, y);
                        }
                    });
                    break;

                case MotionEvent.ACTION_UP:
                    if (event.getPointerCount() >= 2) {
                        break;
                    }
                case MotionEvent.ACTION_POINTER_UP:
                    mGLQueue.add(new Runnable() {
                        @Override
                        public void run() {
                            IG.EditorTouchEvent(igEditor, IG.TOUCH_ENDED, id, x, y);
                        }
                    });
                    break;

                case MotionEvent.ACTION_CANCEL:
                    mGLQueue.add(new Runnable() {
                        @Override
                        public void run() {
                            IG.EditorTouchEvent(igEditor, IG.TOUCH_CANCELED, 0, x, y);
                        }
                    });
                    break;

                default:
                    Log.d("IGEditorView", "UNKNOWN EVENT: " + event.toString());
                    return false;
            }
        }

        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                updateState();
            }
        });

        return true;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.getHolder().setFixedSize((int) Math.round(w / density), (int) Math.round(h / density));
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void destroyEditor() {
        if (igEditor != 0) {
            IG.EditorDestroy(igEditor);
            igEditor = 0;
        }

        if (igInputImage != 0) {
            IG.ImageDestroy(igInputImage);
            igInputImage = 0;
        }

        if (igContext != 0) {
            IG.ContextDestroy(igContext);
            igContext = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        destroyEditor();
    }

    private void resetEditor() {
        destroyEditor();

        if(inputBitmap != null) {
            igContext = IG.ContextCreateHosted();
            igInputImage = IG.ImageFromNative(igContext, inputBitmap, 1);
            igEditor = IG.EditorCreate(igInputImage);

            aspectFit();
        }
    }

    public void aspectFit() {
        if(igEditor != 0 && igInputImage != 0 && surfaceWidth > 0 && surfaceHeight > 0) {
            float imageWidth = IG.ImageGetWidth(igInputImage);
            float imageHeight = IG.ImageGetHeight(igInputImage);

            float viewportAspect = surfaceWidth / surfaceHeight;
            float imageAspect = imageWidth / imageHeight;

            float zoom = imageAspect > viewportAspect ?
                    surfaceWidth / imageWidth :
                    surfaceHeight / imageHeight;

            IG.EditorZoomTo(igEditor, zoomInOnAspectFit ? zoom : Math.min(1, zoom));
        }
    }

    private void updateState() {
        final int newState = IG.EditorGetState(igEditor);
        final int newSubstate = IG.EditorGetSubstate(igEditor);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if(stateListener != null && (newState != state || newSubstate != substate)) {
                    state = newState;
                    substate = newSubstate;

                    stateListener.onStateChanged(newState, newSubstate);
                }
            }
        });
    }

    public void undo() {
        if (igEditor != 0) {
            mGLQueue.add(new Runnable() {
                @Override
                public void run() {
                    IG.EditorUndo(igEditor);
                }
            });

            // TODO: Refresh display here!
        }
    }

    // Sets the input image; must have 1 pixel of padding around the sides. The input image is
    // _not_ copied, so please retain it until image is changed, or view's parent object is deinit'd
    public void setInputBitmap(final Bitmap inputBitmap) {
        this.inputBitmap = inputBitmap;

        mEditorIndependentGLQueue.add(new Runnable() {
            @Override
            public void run() {
                resetEditor();
            }
        });

        // TODO: Refresh display here!
    }

    public boolean isImojiReady() {
        return igEditor == 0 ? false : IG.EditorImojiIsReady(igEditor);
    }

    public boolean canUndo() {
        return igEditor == 0 ? false : IG.EditorCanUndo(igEditor);
    }

    public void scrollTo(final float x, final float y) {
        if (igEditor == 0) {
            return;
        }

        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                IG.EditorScrollTo(igEditor, x, y);
            }
        });

        // TODO: Refresh display here!
    }

    public void zoomTo(final float zoom) {
        if (igEditor == 0) {
            return;
        }

        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                IG.EditorZoomTo(igEditor, zoom);
            }
        });

        // TODO: Refresh display here!
    }

    // Return the edge paths, for use with BorderSetEdgePaths() or WebP embedded chunk; destroy after use
    public int getEdgePaths() {
        return igEditor == 0 ? 0 : IG.EditorGetEdgePaths(igEditor);
    }

    /**
     * Get output image, cropped to bounds of edge paths and notify caller; destroy after use
     */
    public void getOutputBitmap(final BitmapListener callback) {
        if (igEditor == 0) {
            callback.onBitmapOutputReady(null);
        } else {
            mGLQueue.add(new Runnable() {
                @Override
                public void run() {
                    int igOutputImage = IG.EditorGetOutputImage(igEditor);
                    final Bitmap outputBitmap = IG.ImageToNative(igOutputImage);
                    IG.ImageDestroy(igOutputImage);

                    //post on main thread
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onBitmapOutputReady(outputBitmap);
                        }
                    });
                }
            });
        }
    }

    // Return an output image, cropped and trimmed to edge paths; destroy after use
    public void getTrimmedOutputBitmap(final BitmapListener callback) {
        if (igEditor == 0) {
            callback.onBitmapOutputReady(null);
        } else {
            mGLQueue.add(new Runnable() {
                @Override
                public void run() {
                    int igTrimmedOutputImage = IG.EditorGetTrimmedOutputImage(igEditor);
                    final Bitmap trimmedOutputBitmap = IG.ImageToNative(igTrimmedOutputImage);
                    IG.ImageDestroy(igTrimmedOutputImage);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onBitmapOutputReady(trimmedOutputBitmap);
                        }
                    });
                }
            });
        }
    }

    public void setGLBackgroundColor(final int color) {
        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                IG.EditorSetBackgroundColor(igEditor, Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
            }
        });
    }

    public void setStrokeColor(final int color) {
        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                IG.EditorSetStrokeColor(igEditor, Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
            }
        });

    }

    public void setDotColor(final int color) {
        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                IG.EditorSetDotColor(igEditor, Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color));
            }
        });

    }

    public void setStrokeWidth(final float width) {
        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                IG.EditorSetStrokeWidth(igEditor, width);
            }
        });
    }

    public void setImageAlpha(final int alpha) {

        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                IG.EditorSetImageAlpha(igEditor, alpha);
            }
        });
    }

    public byte[] serialize() {
        return IG.EditorSerialize(igEditor);
    }

    public void deserialize(final byte[] data) {
        mGLQueue.add(new Runnable() {
            @Override
            public void run() {
                IG.EditorDeserialize(igEditor, data);
            }
        });
    }

    public boolean isZoomInOnAspectFit() {
        return zoomInOnAspectFit;
    }

    public void setZoomInOnAspectFit(boolean zoomInOnAspectFit) {
        this.zoomInOnAspectFit = zoomInOnAspectFit;
    }

    public int getState() {
        return state;
    }

    public int getSubstate() {
        return substate;
    }

    public void setStateListener(StateListener stateListener) {
        this.stateListener = stateListener;
    }

    public interface StateListener {
        void onStateChanged(int igEditorState, int igEditorSubstate);
    }

    // Callback interface for when user requests the output bitmap
    public interface BitmapListener {
        void onBitmapOutputReady(Bitmap bitmap);
    }
}