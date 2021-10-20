package com.example.frontend.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

import com.google.android.material.slider.RangeSlider;

public class DragGridView extends GridView {
    private long dragResponseMS=1000; //长按响应时间
    private boolean isDrag=false; //是否可以拖动，默认不可以
    private int mDownX;
    private int mDownY;
    private int moveX;
    private int moveY;

    private int mDragPosition; //正在拖动的position
    private View mStartDragItemView=null; //开始拖动的position对应的view
    private ImageView mDragImageView; //用于拖动的镜像
    private Vibrator mVibrator; //震动器
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private Bitmap mDragBitmap; //拖动的item所对应的Bitmap

    private int mPoint2ItemTop; //拖动点到item的上边缘距离
    private int mPoint2ItemLeft;//拖动点到item的左边缘距离
    private int mOffset2Top; //DragGrid距离屏幕顶部的偏移量
    private int mOffset2Left; //DragGrid距离屏幕左边的偏移量

    private int mStatusHeight;  //状态栏高度
    private int mDownScrollBorder;  //DragGrid自动滚动的边界值
    private int mUpScrollBorder;

    private static final int speed = 20; //自动滚动的速度
    private OnChanageListener onChanageListener;

    public interface OnChanageListener{  //自定义的回调接口
        public void onChange(int from,int to);
    }

    public DragGridView(Context context) {
        this(context, null);
    }

    public DragGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mStatusHeight = getStatusHeight(context); //获取状态栏的高度
    }

    private Handler mHandler = new Handler();

    private Runnable mLongClickRunnable = new Runnable() { //长按事件
        @Override
        public void run() {
            isDrag=true;
            mVibrator.vibrate(50); //震动
            mStartDragItemView.setVisibility(INVISIBLE); //隐藏
            createDragImage(mDragBitmap,mDownX,mDownY);
        }
    };

    public void setOnChangeListener(OnChanageListener onChanageListener){
        this.onChanageListener=onChanageListener;
    }

    public void setDragResponseMS(long dragResponseMS){
        this.dragResponseMS=dragResponseMS;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mDownX=(int)ev.getX();
                mDownY=(int)ev.getY();
                //根据X,Y坐标获取点击Item的position
                mDragPosition=pointToPosition(mDownX,mDownY);
                if(mDragPosition== AdapterView.INVALID_POSITION){
                    return super.dispatchTouchEvent(ev);
                }
                //长按延迟执行
                mHandler.postDelayed(mLongClickRunnable,dragResponseMS);
                mStartDragItemView=getChildAt(mDragPosition-getFirstVisiblePosition());
                //计算距离
                mPoint2ItemTop=mDownY-mStartDragItemView.getTop();
                mPoint2ItemLeft=mDownX-mStartDragItemView.getLeft();
                mOffset2Top=(int)(ev.getRawY()-mDownY);
                mOffset2Left=(int)(ev.getRawX()-mDownX);
                //设置偏移量
                mDownScrollBorder=getHeight()/4;
                mUpScrollBorder=getHeight()*3/4;

                //绘图缓存
                mStartDragItemView.setDrawingCacheEnabled(true);
                mDragBitmap=Bitmap.createBitmap(mStartDragItemView.getDrawingCache());
                mStartDragItemView.destroyDrawingCache();
                break;
            case MotionEvent.ACTION_MOVE:
                int moveX=(int)ev.getX();
                int moveY=(int)ev.getY();
                if(!isTouchInItem(mStartDragItemView,moveX,moveY)){
                    mHandler.removeCallbacks(mLongClickRunnable);
                }
                break;
            case MotionEvent.ACTION_UP:
                mHandler.removeCallbacks(mLongClickRunnable);
                mHandler.removeCallbacks(mScrollRunnable);
                break;
            default:
                break;
        }
        return  super.dispatchTouchEvent(ev);
    }

    private boolean isTouchInItem(View dragView, int x,int y){//判断点击是否在GridView的item上面
        if(dragView==null){
            return false;
        }
        int leftOffset=dragView.getLeft();
        int topOffset=dragView.getTop();
        if(x<leftOffset||x>leftOffset+dragView.getWidth()){
            return false;
        }
        return y >= topOffset && y <= topOffset + dragView.getHeight();
    }

//    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev){
        if(isDrag && mDragImageView!=null){
            switch (ev.getAction()){
                case MotionEvent.ACTION_MOVE:
                    moveX=(int)ev.getX();
                    moveY=(int)ev.getY();
                    onDragItem(moveX,moveY);
                    break;
                case MotionEvent.ACTION_UP:
                    onStopDrag();
                    isDrag=false;
                    break;
            }
            return true;
        }
        return super.onTouchEvent(ev);
    }

//    @SuppressLint("RtlHardcoded")
    private void createDragImage(Bitmap bitmap, int downX, int downY){//创建拖动的镜像
        mWindowLayoutParams=new WindowManager.LayoutParams();
        mWindowLayoutParams.format= PixelFormat.TRANSLUCENT;
        mWindowLayoutParams.gravity= Gravity.TOP|Gravity.LEFT;
        mWindowLayoutParams.x = downX - mPoint2ItemLeft + mOffset2Left;
        mWindowLayoutParams.y = downY - mPoint2ItemTop + mOffset2Top - mStatusHeight;
        mWindowLayoutParams.alpha = 0.55f; //透明度
        mWindowLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE ;

        mDragImageView = new ImageView(getContext());
        mDragImageView.setImageBitmap(bitmap);
        mWindowManager.addView(mDragImageView, mWindowLayoutParams);
    }

    private void removeDragImage(){ //移动拖动镜像
        if(mDragImageView!=null){
            mWindowManager.removeView(mDragImageView);
            mDragImageView=null;
        }
    }

    private void onDragItem(int moveX,int moveY){ //拖动Item
        mWindowLayoutParams.x=moveX-mPoint2ItemLeft+mOffset2Left;
        mWindowLayoutParams.y=moveY-mPoint2ItemTop+mOffset2Top;
        mWindowManager.updateViewLayout(mDragImageView, mWindowLayoutParams); //更新镜像位置
        onSwapItem(moveX,moveY);
        mHandler.post(mScrollRunnable);
    }

    private Runnable mScrollRunnable=new Runnable() {
        @Override
        public void run() {
            int scrollY;
            if(moveY>mUpScrollBorder){
                scrollY=speed;
                mHandler.postDelayed(mScrollRunnable,25);
            }else if(moveY<mDownScrollBorder){
                scrollY=-speed;
                mHandler.postDelayed(mScrollRunnable,25);
            }else{
                scrollY=0;
                mHandler.removeCallbacks(mScrollRunnable);
            }
            onSwapItem(moveX,moveY);
            smoothScrollBy(scrollY,10);
        }
    };

    private void onSwapItem(int moveX,int moveY){ //交换Item并控制显示与隐藏
        int tempPosition=pointToPosition(moveX,moveY);
        if(tempPosition!=mDragPosition && tempPosition!=AdapterView.INVALID_POSITION){
            if(onChanageListener!=null){
                onChanageListener.onChange(mDragPosition,tempPosition);
            }
            getChildAt(tempPosition-getFirstVisiblePosition()).setVisibility(INVISIBLE);
            getChildAt(mDragPosition-getFirstVisiblePosition()).setVisibility(VISIBLE);
            mDragPosition=tempPosition;
        }
    }

    private void onStopDrag(){ //停止拖动时将隐藏的Item显示并将镜像移除
        View view=getChildAt(mDragPosition-getFirstVisiblePosition());
        if(view!=null){
            view.setVisibility(View.VISIBLE);
        }
        //(this.getAdapter()).getItemHide(-1);
        removeDragImage();
    }

    private static int getStatusHeight(Context context){
        int statusHeight = 0;
        Rect localRect = new Rect();
        ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(localRect);
        statusHeight = localRect.top;
        if (0 == statusHeight){
            Class<?> localClass;
            try {
                localClass = Class.forName("com.android.internal.R$dimen");
                Object localObject = localClass.newInstance();
                int i5 = Integer.parseInt(localClass.getField("status_bar_height").get(localObject).toString());
                statusHeight = context.getResources().getDimensionPixelSize(i5);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusHeight;
    }


    

}
