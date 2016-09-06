package it.gilvegliach.autocrop;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AutoCropImageView extends ImageView {
  private final static ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
  private final Rect bounds = new Rect();

  private final Paint paint = new Paint();
  {
    paint.setColor(Color.BLACK);
    paint.setStrokeWidth(2.f);
    paint.setStyle(Paint.Style.STROKE);
  }

  public AutoCropImageView(Context context) {
    super(context);
  }

  public AutoCropImageView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public AutoCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public long setImageBitmapWithAutocrop(Bitmap bm) {
    super.setImageBitmap(bm);
    long start = System.currentTimeMillis();
    int w = bm.getWidth();
    int h = bm.getHeight();
    int[] pixels = new int[w * h];
    bm.getPixels(pixels, 0, w, 0, 0, w, h);
    int l = findLeft(pixels, w, h);
    int t = findTop(pixels, w, h);
    int r = findRight(pixels, w, h);
    int b = findBottom(pixels, w, h);
    bounds.set(l, t, r, b);
    long delta = System.currentTimeMillis() - start;
    Log.d("autocrop", "bounds: " + bounds + ", ms: " + delta);
    return delta;
  }

  private int findLeft(int[] pixels, int w, int h) {
    for (int j = 0; j < w; j++) {
      for (int i = 0; i < h; i++) {
        int px = pixels[i * w + j];
        if (!isBackground(px)) {
          return j;
        }
      }
    }
    return 0;
  }

  private int findTop(int[] pixels, int w, int h) {
    for (int i = 0; i < h; i++) {
      for (int j = 0; j < w; j++) {
        int px = pixels[i * w + j];
        if (!isBackground(px)) {
          return i;
        }
      }
    }
    return 0;
  }

  private int findRight(int[] pixels, int w, int h) {
    for (int j = w - 1; j >= 0; j--) {
      for (int i = 0; i < h; i++) {
        int px = pixels[i * w + j];
        if (!isBackground(px)) {
          return j;
        }
      }
    }
    return w - 1;
  }

  private int findBottom(int[] pixels, int w, int h) {
    for (int i = h - 1; i >= 0; i--) {
      for (int j = 0; j < w; j++) {
        int px = pixels[i * w + j];
        if (!isBackground(px)) {
          return i;
        }
      }
    }
    return h - 1;
  }

  public long setImageBitmapWithAutocropPar(Bitmap bm) {
    super.setImageBitmap(bm);
    long start = System.currentTimeMillis();
    final int w = bm.getWidth();
    final int h = bm.getHeight();
    final int[] pixels = new int[w * h];
    bm.getPixels(pixels, 0, w, 0, 0, w, h);

    Future<Integer> l = EXECUTOR.submit(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return findLeft(pixels, w, h);
      }
    });
    Future<Integer> t = EXECUTOR.submit(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return findTop(pixels, w, h);
      }
    });
    Future<Integer> r = EXECUTOR.submit(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return findRight(pixels, w, h);
      }
    });
    Future<Integer> b = EXECUTOR.submit(new Callable<Integer>() {
      @Override
      public Integer call() throws Exception {
        return findBottom(pixels, w, h);
      }
    });

    try {
      bounds.set(l.get(), t.get(), r.get(), b.get());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    long delta = System.currentTimeMillis() - start;
    Log.d("autocrop", "parallel, bounds: " + bounds + ", ms: " + delta);
    return delta;
  }

  private boolean isBackground(int px) {
    int diff = 255 - Color.alpha(px);
    diff += 255 - Color.red(px);
    diff += 255 - Color.green(px);
    diff += 255 - Color.blue(px);
    return diff < 10L;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    if (!bounds.isEmpty()) {
      canvas.drawRect(bounds, paint);
    }
  }
}
