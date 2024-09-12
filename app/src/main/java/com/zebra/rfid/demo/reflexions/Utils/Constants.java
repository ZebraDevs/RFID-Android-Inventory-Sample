package com.zebra.rfid.demo.reflexions.Utils;

import android.util.Log;

/**
 * Class to maintain the strings used for notifications and intent actions
 */
public class Constants {
    
    //For Debugging
    public static final boolean DEBUG = false;
    public static final int TYPE_DEBUG = 60;
    public static final int TYPE_ERROR = 61;
    
    /**
     * Method to be used throughout the app for logging debug messages
     *
     * @param type    - One of TYPE_ERROR or TYPE_DEBUG
     * @param TAG     - Simple String indicating the origin of the message
     * @param message - Message to be logged
     */
    public static void logAsMessage(int type, String TAG, String message) {
        if (DEBUG) {
            if (type == TYPE_DEBUG)
                Log.d(TAG, (message == null || message.isEmpty()) ? "Message is Empty!!" : message);
            else if (type == TYPE_ERROR)
                Log.e(TAG, (message == null || message.isEmpty()) ? "Message is Empty!!" : message);
        }
    }

}
