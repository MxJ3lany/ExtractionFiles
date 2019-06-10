/**
 * Copyright (C) 2013- Iordan Iordanov
 *
 * This is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,
 * USA.
 */


package com.undatech.opaque.input;

import android.view.MotionEvent;
import android.os.Vibrator;

import com.undatech.opaque.R;
import com.undatech.opaque.RemoteClientLibConstants;
import com.undatech.opaque.RemoteCanvas;
import com.undatech.opaque.RemoteCanvasActivity;

public class InputHandlerDirectDragPan extends InputHandlerGeneric {
    static final String TAG = "InputHandlerDirectDragPan";
    public static final String ID = "DirectDragPan";
    
    public InputHandlerDirectDragPan(RemoteCanvasActivity activity, RemoteCanvas canvas,
                                     RemotePointer pointer, Vibrator myVibrator) {
        super(activity, canvas, pointer, myVibrator);
    }

    /*
     * (non-Javadoc)
     * @see com.undatech.opaque.input.InputHandler#getDescription()
     */
    @Override
    public String getDescription() {
        return canvas.getResources().getString(R.string.input_method_direct_drag_pan_description);
    }

    /*
     * (non-Javadoc)
     * @see com.undatech.opaque.input.InputHandler#getId()
     */
    @Override
    public String getId() {
        return ID;
    }
    
    /*
     * (non-Javadoc)
     * @see com.undatech.opaque.input.InputHandlerGeneric#onLongPress(android.view.MotionEvent)
     */
    @Override
    public void onLongPress(MotionEvent e) {

        // If we've performed a right/middle-click and the gesture is not over yet, do not start drag mode.
        if (secondPointerWasDown || thirdPointerWasDown)
            return;
        
        myVibrator.vibrate(RemoteClientLibConstants.SHORT_VIBRATION);

        canvas.displayShortToastMessage("Panning");
        endDragModesAndScrolling();
        panMode = true;
    }
    

    /*
     * (non-Javadoc)
     * @see android.view.GestureDetector.SimpleOnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        RemotePointer p = canvas.getPointer();

        // If we are scaling, allow panning around by moving two fingers around the screen
        if (inScaling) {
            float scale = canvas.getZoomFactor();
            activity.showToolbar();
            canvas.relativePan((int)(distanceX*scale), (int)(distanceY*scale));
        } else {        
            // onScroll called while scaling/swiping gesture is in effect. We ignore the event and pretend it was
            // consumed. This prevents the mouse pointer from flailing around while we are scaling.
            // Also, if one releases one finger slightly earlier than the other when scaling, it causes Android 
            // to stick a spiteful onScroll with a MASSIVE delta here. 
            // This would cause the mouse pointer to jump to another place suddenly.
            // Hence, we ignore onScroll after scaling until we lift all pointers up.
            boolean twoFingers = false;
            if (e1 != null)
                twoFingers = (e1.getPointerCount() > 1);
            if (e2 != null)
                twoFingers = twoFingers || (e2.getPointerCount() > 1);
    
            if (twoFingers||inSwiping)
                return true;
    
            activity.showToolbar();
            
            if (!dragMode) {
                dragMode = true;
                p.leftButtonDown(getX(e1), getY(e1), e1.getMetaState());
            } else {
                p.moveMouseButtonDown(getX(e2), getY(e2), e2.getMetaState());
            }
        }
        canvas.movePanToMakePointerVisible();
        return true;
    }
}

