package com.zebra.rfid.demo.reflexions.Utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.zebra.rfid.demo.reflexions.BuildConfig;
import com.zebra.rfid.demo.reflexions.R;

public class DataWedge
{
    public static final String INTENT_NAME              =   BuildConfig.APPLICATION_ID + ".BARCODE";
    public static final String DATAWEDGE_EXTRA          =   "com.symbol.datawedge.data_string";

    /**
     * Setup the datawedge profile for application
     * @param context Application Context
     */
    public static void setDWProfile(Context context) {
        //Main
        Bundle bMain = new Bundle();
        bMain.putString("PROFILE_NAME",context.getString(R.string.app_name));
        bMain.putString("PROFILE_ENABLED","true");
        bMain.putString("CONFIG_MODE","CREATE_IF_NOT_EXIST");

        //PLUGIN_CONFIG
        Bundle bConfig = new Bundle();
        bConfig.putString("PLUGIN_NAME","INTENT");
        bConfig.putString("RESET_CONFIG","true");

        //PARAM_LIST
        Bundle bParams = new Bundle();
        bParams.putString("intent_output_enabled","true");
        bParams.putString("intent_action",INTENT_NAME);
        bParams.putString("intent_category", Intent.CATEGORY_DEFAULT);
        bParams.putInt("intent_delivery",2);                                                        //Use "0" for Start Activity, "1" for Start Service, "2" for Broadcast
        bConfig.putBundle("PARAM_LIST", bParams);
        bMain.putBundle("PLUGIN_CONFIG", bConfig);

        // APP_LIST
        Bundle bundleApp1 = new Bundle();
        bundleApp1.putString("PACKAGE_NAME", BuildConfig.APPLICATION_ID);
        bundleApp1.putStringArray("ACTIVITY_LIST", new String[]{ "*" });
        bMain.putParcelableArray("APP_LIST", new Bundle[] { bundleApp1 });

        //Send Intent to add Config
        Intent i = new Intent();
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain);
        context.sendBroadcast(i);

        //****************** Turn Off Keystroke *****************************
        bMain = new Bundle();
        bMain.putString("PROFILE_NAME",context.getString(R.string.app_name));
        bMain.putString("PROFILE_ENABLED","true");
        bMain.putString("CONFIG_MODE","UPDATE");

        //PLUGIN_CONFIG
        bConfig = new Bundle();
        bConfig.putString("PLUGIN_NAME","KEYSTROKE");
        bConfig.putString("RESET_CONFIG","true");

        //PARAM_LIST
        bParams = new Bundle();
        bParams.putString("keystroke_output_enabled","false");

        bConfig.putBundle("PARAM_LIST", bParams);
        bMain.putBundle("PLUGIN_CONFIG", bConfig);
        i.setAction("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.SET_CONFIG", bMain);
        context.sendBroadcast(i);
    }

    /**
     * Enable/Disable scanner
     * @param context Application context
     * @param bEnabled State
     */
    public static void setDWEnabled(Context context, boolean bEnabled)
    {
        Intent i = new Intent("com.symbol.datawedge.api.ACTION");
        i.putExtra("com.symbol.datawedge.api.ENABLE_DATAWEDGE",bEnabled);
        context.sendBroadcast(i);
    }

}
