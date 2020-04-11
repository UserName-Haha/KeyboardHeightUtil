package com.haha.keyboardheightutil;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.haha.keyboardheightutil.tool.Utils;

import java.util.ArrayList;

/**
 * @author zhe.chen
 * @date 2020-04-11 10:11
 * Des:
 */
public class KeyboardHeightProvider extends PopupWindow {

    private final static String TAG = "sample_KeyboardHeightProvider";
    private ArrayList<KeyboardHeightObserver> mKeyboardHeightObserverList = new ArrayList<>();
    private int keyboardLandscapeHeight;
    private int keyboardPortraitHeight;
    private View popupView;
    private View parentView;
    private Activity activity;
    private Handler handler = new Handler();


    public KeyboardHeightProvider(Activity activity) {
        super(activity);
        this.activity = activity;

        LayoutInflater inflator = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        this.popupView = inflator.inflate(R.layout.haha_popupwindow, null, false);
        setContentView(popupView);

        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);

        parentView = activity.findViewById(android.R.id.content);

        setWidth(0);//  这样既能测量高度，又不会导致界面不能点击
//        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);

        popupView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (popupView != null) {
                    handleOnGlobalLayout();
                }
            }
        });
        handler.postDelayed(mStartRunnable, 500);
    }

    private void start() {
        if (!isShowing() && parentView.getWindowToken() != null) {
            setBackgroundDrawable(new ColorDrawable(0));
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0);
        }
    }

    public void addKeyboardHeightObserver(KeyboardHeightObserver observer) {
        if (mKeyboardHeightObserverList != null && !mKeyboardHeightObserverList.contains(observer)) {
            mKeyboardHeightObserverList.add(observer);
        }
    }

    public void removeKeyboardHeightObserver(KeyboardHeightObserver observer) {
        if (mKeyboardHeightObserverList != null && mKeyboardHeightObserverList.contains(observer)) {
            mKeyboardHeightObserverList.remove(observer);
        }
        dismiss();
        handler.removeCallbacks(mStartRunnable);
    }

    private void handleOnGlobalLayout() {
        Point screenSize = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(screenSize);

        Rect rect = new Rect();
        popupView.getWindowVisibleDisplayFrame(rect);
        rect = calculationStatusBar(screenSize, rect);

        int orientation = getScreenOrientation();
        int keyboardHeight = screenSize.y - rect.bottom;
        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation);
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.keyboardPortraitHeight = keyboardHeight;
            notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation);
        } else {
            this.keyboardLandscapeHeight = keyboardHeight;
            notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation);
        }
    }

    /**
     * 通过 getWindowVisibleDisplayFrame 方法获取可见视图的尺寸在部分机型上不准确，
     * 如设备是刘海屏（待确定）需要将刘海包含进去
     *
     * @param rect
     * @return
     */
    private Rect calculationStatusBar(Point screenSize, Rect rect) {
        if (screenSize.y - rect.bottom == 0) return rect;//如果当前屏幕的高与可见视图的高一致，则代表软键盘已关闭，故无需在计算
        if (Utils.hasNotchScreen(activity)) {
            int notchHeight = Utils.getNotchHeight(activity);
            if (notchHeight > 0) {
                rect.bottom = rect.bottom - notchHeight;
            }
        }
        return rect;
    }

    private int getScreenOrientation() {
        return activity.getResources().getConfiguration().orientation;
    }

    private void notifyKeyboardHeightChanged(int height, int orientation) {
        if (mKeyboardHeightObserverList != null) {
            for (KeyboardHeightObserver observer : mKeyboardHeightObserverList) {
                //如果获取到的高度大于1/5，即认为软键盘弹出
                observer.onKeyboardHeightChanged(height > Utils.getScreenHeight(activity) / 5, height, orientation);
            }
        }
    }

    Runnable mStartRunnable = new Runnable() {
        @Override
        public void run() {
            start();
        }
    };

    public interface KeyboardHeightObserver {
        void onKeyboardHeightChanged(boolean isPopUp, int height, int orientation);
    }


}
