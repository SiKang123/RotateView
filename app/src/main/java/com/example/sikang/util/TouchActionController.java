package com.example.sikang.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SiKang on 2016/3/3.
 */
public class TouchActionController {
    private List<ViewTouchActionListener> mActionListener = new ArrayList<ViewTouchActionListener>(12);
    public static TouchActionController mTouchActionController = null;
    private boolean isMaxMove, isMinMove;

    private TouchActionController() {
        isMaxMove = false;
        isMinMove = false;
    }

    public static TouchActionController getInstance() {
        if (mTouchActionController == null) {
            synchronized (TouchActionController.class) {
                if (mTouchActionController == null) {
                    mTouchActionController = new TouchActionController();
                }
            }
        }
        return mTouchActionController;
    }

    public void registerTouchActionListener(ViewTouchActionListener listener) {
        if (!mActionListener.contains(listener)) {
            mActionListener.add(listener);
        }
    }

    public void unRegisterActionListener(ViewTouchActionListener listener) {
        if (mActionListener.contains(listener)) {
            mActionListener.remove(listener);
        }
    }

    public void notifyListener(ViewTouchActionListener sender, final int ACTION_ID, final Object... args) {
        for (final ViewTouchActionListener listener : mActionListener) {
            if (sender != listener) {
                listener.onTouchAction(ACTION_ID, args);
            }
        }
    }

    public boolean isMaxMove() {
        return isMaxMove;
    }

    public boolean isMinMove() {
        return isMinMove;
    }

    public void setIsMaxMove(boolean isMaxMove) {
        this.isMaxMove = isMaxMove;
    }

    public void setIsMinMove(boolean isMinMove) {
        this.isMinMove = isMinMove;
    }
}
