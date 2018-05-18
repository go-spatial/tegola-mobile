package go_spatial.com.github.tegola.mobile.android.ux;

//credit to: https://stackoverflow.com/questions/18768360/draggable-drawer-with-a-handle-instead-of-action-bar-on-top-of-other-apps?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa

import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.ViewDragHelper;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class DrawerHandle implements DrawerLayout.DrawerListener {
    public static final String TAG = "DrawerHandle";

    private ViewGroup mRootView;
    private DrawerLayout mDrawerLayout;
    private View mHandle;
    private View mDrawer;
    private View m_img_drawer_closed;
    private View m_img_drawer_opened;

    private float mVerticalOffset;
    private int mGravity;
    private WindowManager mWM;
    private Display mDisplay;
    private Point mScreenDimensions = new Point();

    private OnClickListener mHandleClickListener = new OnClickListener(){
        @Override
        public void onClick(View v) {
            toggle();
        }
    };

    private OnTouchListener mHandleTouchListener = new OnTouchListener() {
        private static final int MAX_CLICK_DURATION = 200;
        private long startClickTime;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    startClickTime = System.currentTimeMillis();
                    break;
                }
                case MotionEvent.ACTION_UP: {
                    if(System.currentTimeMillis() - startClickTime < MAX_CLICK_DURATION) {
                        v.performClick();
                        return true;
                    }
                }
            }
            MotionEvent copy = MotionEvent.obtain(event);
            copy.setEdgeFlags(ViewDragHelper.EDGE_ALL);
            copy.setLocation(event.getRawX() + (mGravity == Gravity.LEFT || mGravity == GravityCompat.START ? -mHandle.getWidth()/2 : mHandle.getWidth()/2), event.getRawY());
            copy.setLocation(event.getRawX(), event.getRawY());
            mDrawerLayout.onTouchEvent(copy);
            copy.recycle();
            return true;
        }
    };

    private int getDrawerViewGravity(View drawerView) {
        final int gravity = ((DrawerLayout.LayoutParams)drawerView.getLayoutParams()).gravity;
        return GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(drawerView));
    }

    private float getTranslation(float slideOffset) {
        return (mGravity == GravityCompat.START || mGravity == Gravity.LEFT)
                    ? slideOffset * (mDrawer.getWidth())
                    : -slideOffset * (mDrawer.getWidth());
    }

    private void updateScreenDimensions() {
        if (Build.VERSION.SDK_INT >= 13) {
            mDisplay.getSize(mScreenDimensions);
        } else {
            mScreenDimensions.x = mDisplay.getWidth();
            mScreenDimensions.y = mDisplay.getHeight();
        }
    }

    private Integer m_handlelayout = null;
    private DrawerHandle(DrawerLayout drawerLayout, View drawer, int handleLayout, float handleVerticalOffset) {
        mDrawer = drawer;
        Log.d(TAG, "ctor: set drawer (view) ref to: " + mDrawer.getContext().getResources().getResourceName(mDrawer.getId()));
        mGravity = getDrawerViewGravity(mDrawer);
        Log.d(TAG, "ctor: got DrawerViewGravity");
        mDrawerLayout = drawerLayout;
        Log.d(TAG, "ctor: got ref to DrawerLayout view: " + mDrawerLayout.getContext().getResources().getResourceName(mDrawerLayout.getId()));
        mRootView = (ViewGroup)mDrawerLayout.getRootView();
        Log.d(TAG, "ctor: got root view of drawerlayout: " + mDrawerLayout.getContext().getResources().getResourceName(mDrawerLayout.getId()));
        LayoutInflater inflater = (LayoutInflater)mDrawerLayout.getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE);
        mHandle = inflater.inflate(handleLayout, mRootView, false);
        m_img_drawer_closed = mHandle.findViewById(R.id.img_drawer_closed);
        m_img_drawer_opened = mHandle.findViewById(R.id.img_drawer_opened);
        m_handlelayout = handleLayout;
        Log.d(TAG, "ctor: inflated DrawerHandle layout: " + mHandle.getContext().getResources().getResourceName(handleLayout));
        mWM = (WindowManager)mDrawerLayout.getContext().getSystemService(Context.WINDOW_SERVICE);
        mDisplay = mWM.getDefaultDisplay();
        Log.d(TAG, "ctor: got ref to DefaultDisplay");

        mHandle.setOnClickListener(mHandleClickListener);
        Log.d(TAG, "ctor: set OnClickListener");
        mHandle.setOnTouchListener(mHandleTouchListener);
        Log.d(TAG, "ctor: set OnTouchListener");
        Log.d(TAG, "ctor: mRootView.width: " + mRootView.getWidth() + "; mRootView.height: " + mRootView.getHeight());
        Log.d(TAG, "ctor: mHandle.getLayoutParams().width: " + mHandle.getLayoutParams().width + "; mHandle.getLayoutParams().height: " + mHandle.getLayoutParams().height);
        mRootView.addView(mHandle, new FrameLayout.LayoutParams(mHandle.getLayoutParams().width, mHandle.getLayoutParams().height, mGravity));
        Log.d(TAG, "ctor: added DrawerHandle view " + mHandle.getContext().getResources().getResourceName(handleLayout) + " to root view of drawerlayout" + mDrawerLayout.getContext().getResources().getResourceName(mDrawerLayout.getId()));
        setVerticalOffset(handleVerticalOffset);
        mDrawerLayout.addDrawerListener(this);
        Log.d(TAG, "ctor: added *this* as DrawerListener to DrawerLayout: " + mDrawerLayout.getContext().getResources().getResourceName(mDrawerLayout.getId()));
    }

    public static DrawerHandle attach(View drawer, int handleLayout, float verticalOffset) {
        if (!(drawer.getParent() instanceof DrawerLayout))
            throw new IllegalArgumentException("Argument drawer must be direct child of a DrawerLayout");
        return new DrawerHandle((DrawerLayout)drawer.getParent(), drawer, handleLayout, verticalOffset);
    }

    public static DrawerHandle attach(View drawer, int handleLayout) {
        return attach(drawer, handleLayout, 0);
    }

    public void detach() {
        mDrawerLayout.removeDrawerListener(this);
        Log.d(TAG, "detach: removed *this* as DrawerListener from DrawerLayout: " + mDrawerLayout.getContext().getResources().getResourceName(mDrawerLayout.getId()));
        mRootView.removeView(mHandle);
        Log.d(TAG, "detach: removed DrawerHandle view " + mHandle.getContext().getResources().getResourceName(m_handlelayout) + " from root view of drawerlayout " + mDrawerLayout.getContext().getResources().getResourceName(mDrawerLayout.getId()));
        mRootView.postInvalidate();
        m_handlelayout = null;
    }

    @Override
    public void onDrawerClosed(View arg0) {
        m_img_drawer_closed.setVisibility(View.VISIBLE);
        m_img_drawer_opened.setVisibility(View.GONE);
    }

    @Override
    public void onDrawerOpened(View arg0) {
        m_img_drawer_closed.setVisibility(View.GONE);
        m_img_drawer_opened.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDrawerSlide(View arg0, float slideOffset) {
        float translationX = getTranslation(slideOffset);
        mHandle.setTranslationX(translationX);
    }

    @Override
    public void onDrawerStateChanged(int arg0) {
    }

    public View getView(){
        return mHandle;
    }

    public View getDrawer() {
        return mDrawer;
    }

    public void setVerticalOffset(float offset) {
        updateScreenDimensions();
        mVerticalOffset = offset;
        mHandle.setY(mVerticalOffset * mScreenDimensions.y);
    }

    public void openDrawer() {
        mDrawerLayout.openDrawer(mGravity);
    }

    public void closerDrawer() {
        mDrawerLayout.closeDrawer(mGravity);
    }

    public void toggle() {
        if (!mDrawerLayout.isDrawerOpen(mGravity))
            mDrawerLayout.openDrawer(mGravity);
        else
            mDrawerLayout.closeDrawer(mGravity);
    }
}
