package com.econ.openfile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class ElectrodeView extends ViewGroup{

    private Bitmap mBitmap;
    private Bitmap newBitmap;
    private Paint mPaint;

    public ElectrodeView(Context context) {
        this(context,null);
    }

    public ElectrodeView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public ElectrodeView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }



    private void initView(){
        mBitmap = BitmapFactory.decodeResource(getResources(),R.drawable.brain02);
        mPaint = new Paint();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int viewWidth = MeasureSpec.getSize(widthMeasureSpec);
        int viewHeight = MeasureSpec.getSize(heightMeasureSpec);

        int mBitmapWidth = mBitmap.getWidth();
        int mBitmapHeight = mBitmap.getHeight();

         float newBitmapHeight = 0,newBitmapWidth = 0;
         if (mBitmapHeight > viewHeight){
             newBitmapHeight = viewHeight;
             newBitmapWidth = mBitmapWidth * (newBitmapHeight/mBitmapHeight);

             //计算缩放比例
             float scaleWidth =((float) newBitmapWidth)/ mBitmapWidth;
             float scaleHeight = ((float) newBitmapHeight) / mBitmapHeight;

             //取得想要缩放的matrix参数
             Matrix matrix = new Matrix();
             matrix.postScale(scaleWidth,scaleHeight);

             newBitmap = Bitmap.createBitmap(mBitmap,0,0,mBitmapWidth,mBitmapHeight,matrix,true);
             setMeasuredDimension(newBitmap.getWidth(),newBitmap.getHeight());
         }else if (mBitmapWidth > viewWidth){
             newBitmapWidth = viewWidth;
             newBitmapHeight = mBitmapHeight * (newBitmapWidth / mBitmapWidth);

             //计算缩放比例
             float scaleWidth =((float) newBitmapWidth)/ mBitmapWidth;
             float scaleHeight = ((float) newBitmapHeight) / mBitmapHeight;

             //取得想要缩放的matrix参数
             Matrix matrix = new Matrix();
             matrix.postScale(scaleWidth,scaleHeight);

             newBitmap = Bitmap.createBitmap(mBitmap,0,0,mBitmapWidth,mBitmapHeight,matrix,true);
             setMeasuredDimension(newBitmap.getWidth(),newBitmap.getHeight());
         } else {
              setMeasuredDimension(mBitmapWidth,mBitmapHeight);
         }

    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            child.layout(0,0,child.getMeasuredWidth(),child.getMeasuredHeight());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (newBitmap != null){
            canvas.drawBitmap(newBitmap,0f,0f,null);

        }else {
            canvas.drawBitmap(mBitmap,0,0,mPaint);
        }

        // 遍历绘制子视图
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child != null) {
                // 为子视图创建新的画布并绘制
                Bitmap childBitmap = Bitmap.createBitmap(child.getWidth(), child.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas childCanvas = new Canvas(childBitmap);
                child.draw(childCanvas);
                // 在当前画布上绘制子视图内容
                canvas.drawBitmap(childBitmap, child.getLeft(), child.getTop(), null);
            }
        }
    }
}
