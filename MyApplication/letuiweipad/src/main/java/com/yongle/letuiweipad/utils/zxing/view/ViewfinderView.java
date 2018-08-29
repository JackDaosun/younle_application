package com.yongle.letuiweipad.utils.zxing.view;/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import com.google.zxing.ResultPoint;
import com.yongle.letuiweipad.R;
import com.yongle.letuiweipad.utils.zxing.camera.CameraManager;

import java.util.Collection;
import java.util.HashSet;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder
 * rectangle and partial transparency outside it, as well as the laser scanner
 * animation and result points.
 *
 */
public  class ViewfinderView extends View {
  private static final String TAG = "log";

  private static final long ANIMATION_DELAY = 10L;
  private static final int OPAQUE = 0xFF;
    private final Context context;


    private int ScreenRate;


  private static final int CORNER_WIDTH = 2;

  private static final int MIDDLE_LINE_WIDTH =2;


  private static final int MIDDLE_LINE_PADDING = 2;
  //private static final String TextSize="ȡ����";

  public int SPEEN_DISTANCE = 2;


  private static float density;

  private static final int TEXT_SIZE = 16;

  private static final int TEXT_PADDING_TOP = 30;

  private Paint paint;

  private int slideTop;

  private int slideBottom;

  private Bitmap resultBitmap;
  private final int maskColor;
  private final int resultColor;

  private final int resultPointColor;
  private Collection<ResultPoint> possibleResultPoints;
  private Collection<ResultPoint> lastPossibleResultPoints;

  boolean isFirst;
    public ViewfinderView(Context context, AttributeSet attrs) {
    super(context, attrs);
      this.context=context;

    density = context.getResources().getDisplayMetrics().density;

    ScreenRate = (int)(20 * density);

    paint = new Paint();
    Resources resources = getResources();
    maskColor = resources.getColor(R.color.left_gray_bg);
    resultColor = resources.getColor(R.color.left_gray_bg);

    resultPointColor = resources.getColor(R.color.left_gray_bg);
    possibleResultPoints = new HashSet<ResultPoint>(5);
  }


    @Override
  public void onDraw(Canvas canvas) {

    Rect frame = CameraManager.get().getFramingRect();
    if (frame == null) {
      return;
    }
    if(!isFirst){
      isFirst = true;
      slideTop = frame.top;
      slideBottom = frame.bottom;
    }
    int width = canvas.getWidth();
    int height = canvas.getHeight();
    paint.setColor(resultBitmap != null ? resultColor : maskColor);
    canvas.drawRect(0, 0, width, frame.top, paint);//上面
    canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);//左侧
    canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1,//右侧
            paint);
    canvas.drawRect(0, frame.bottom + 1, width, height, paint);//下侧
    if (resultBitmap != null) {
      // Draw the opaque result bitmap over the scanning rectangle
      paint.setAlpha(OPAQUE);
      canvas.drawBitmap(resultBitmap, frame.left, frame.top, paint);
    } else {
      paint.setColor(Color.rgb(0,154,247));
      canvas.drawRect(frame.left, frame.top, frame.left + ScreenRate,
              frame.top + CORNER_WIDTH, paint);
      canvas.drawRect(frame.left, frame.top, frame.left + CORNER_WIDTH, frame.top
              + ScreenRate, paint);
      canvas.drawRect(frame.right - ScreenRate, frame.top, frame.right+1,
              frame.top + CORNER_WIDTH, paint);
      canvas.drawRect(frame.right - CORNER_WIDTH, frame.top, frame.right+1, frame.top
              + ScreenRate, paint);
      canvas.drawRect(frame.left, frame.bottom - CORNER_WIDTH, frame.left
              + ScreenRate, frame.bottom+1, paint);
      canvas.drawRect(frame.left, frame.bottom - ScreenRate,
              frame.left + CORNER_WIDTH, frame.bottom+1, paint);
      canvas.drawRect(frame.right - ScreenRate, frame.bottom - CORNER_WIDTH,
              frame.right+1, frame.bottom+1, paint);
      canvas.drawRect(frame.right - CORNER_WIDTH, frame.bottom - ScreenRate,
              frame.right+1, frame.bottom+1, paint);

      //绘制中间的线,每次刷新界面，中间的线往下移动SPEEN_DISTANCE
      slideTop += SPEEN_DISTANCE;
      if(slideTop >= frame.bottom-30){
        slideTop = frame.top+10;
      }
      Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.scan_line);
      canvas.drawBitmap(bitmap, (width-bitmap.getWidth())/2, slideTop - MIDDLE_LINE_WIDTH/2+30,paint);

        //canvas.drawRect(frame.left + MIDDLE_LINE_PADDING, slideTop - MIDDLE_LINE_WIDTH/2, frame.right - MIDDLE_LINE_PADDING,slideTop + MIDDLE_LINE_WIDTH/2, paint);
        //frame.left + MIDDLE_LINE_PADDING + 8
    /*   String TextSize="ȡ����";

        //paint.setColor(CORNER_WIDTH);
      canvas.drawText(TextSize,frame.left,frame.exactCenterY() , paint);*/


     /* paint.setColor(Color.WHITE);
      paint.setTextSize(TEXT_SIZE * density);
      paint.setAlpha(0x40);
      paint.setTypeface(Typeface.create("System", Typeface.BOLD));
      canvas.drawText(getResources().getString(R.string.scan_text), frame.left, (float) (frame.bottom + (float) TEXT_PADDING_TOP * density), paint);
*/
      Collection<ResultPoint> currentPossible = possibleResultPoints;
      Collection<ResultPoint> currentLast = lastPossibleResultPoints;
      if (currentPossible.isEmpty()) {
        lastPossibleResultPoints = null;
      } else {
        possibleResultPoints = new HashSet<ResultPoint>(5);
        lastPossibleResultPoints = currentPossible;
        paint.setAlpha(OPAQUE);
        paint.setColor(resultPointColor);
        for (ResultPoint point : currentPossible) {
          canvas.drawCircle(frame.left + point.getX(), frame.top
                  + point.getY(), 6.0f, paint);
        }
      }
      if (currentLast != null) {
        paint.setAlpha(OPAQUE / 2);
        paint.setColor(resultPointColor);
        for (ResultPoint point : currentLast) {
          canvas.drawCircle(frame.left + point.getX(), frame.top
                  + point.getY(), 3.0f, paint);
        }
      }
      postInvalidateDelayed(ANIMATION_DELAY, frame.left, frame.top,
              frame.right, frame.bottom);
    }
  }

  public void drawViewfinder() {
    resultBitmap = null;
    invalidate();
  }

  /**
   * Draw a bitmap with the result points highlighted instead of the live
   * scanning display.
   *
   * @param barcode
   *            An image of the decoded barcode.
   */
  public void drawResultBitmap(Bitmap barcode) {
    resultBitmap = barcode;
    invalidate();
  }

  public void addPossibleResultPoint(ResultPoint point) {
    possibleResultPoints.add(point);
  }

}