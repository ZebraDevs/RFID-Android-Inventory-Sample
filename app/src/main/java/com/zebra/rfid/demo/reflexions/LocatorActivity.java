package com.zebra.rfid.demo.reflexions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.demo.reflexions.Utils.RangeGraph;

public class LocatorActivity extends AppCompatActivity implements RFIDHandler.ResponseHandlerInterface{

    public Context ctx;
    public TextView statusTextViewRFID;
    public RFIDHandler rfidHandler;
    public TextView nameOfItem;
    public TextView tagEPC;
    public static String epc;
    public static String name;
    private RangeGraph locationBar;

    final static String TAG = "LocatorActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        hideNavBar();
        setContentView(R.layout.activity_locator);
        ctx = this;

        //UI Elements
        statusTextViewRFID = findViewById(R.id.textStatus);       //rfid connection status
        nameOfItem = findViewById(R.id.nameOfItem);               //name of item being located
        tagEPC = findViewById(R.id.tagEPC);                       //tag number of item being located
        locationBar = (RangeGraph) findViewById(R.id.locationBar); //range graph

        //product found button
        findViewById(R.id.product_found).setOnClickListener((v) -> { getOnBackPressedDispatcher().onBackPressed();});

        nameOfItem.setText(name);
        tagEPC.setText(epc);

    }

    @Override
    protected void onResume() {
        super.onResume();

        //RFID Handler
        rfidHandler = RFIDHandler.getInstance(30,this);  // creates rfid scanner profile
        rfidHandler.setHandler(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void hideNavBar() {
        View decorView = getWindow().getDecorView();

        final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(flags);

        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        if((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                            // TODO: The system bars are visible. Make any desired
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                            decorView.setSystemUiVisibility(flags);
                        } else {
                            // TODO: The system bars are NOT visible. Make any desired
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.

                        }
                    }
                });
    }

    @Override
    public void handleTagdata(TagData[] tagData) {
        //filter by tag number
        for(TagData tag : tagData) {
            int percent = tag.LocationInfo.getRelativeDistance();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    locationBar.setValue(percent);
                    locationBar.invalidate();
                }
            });
        }
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        if (pressed) {
            rfidHandler.locate(epc); //reads and shows inventory
        } else {
            rfidHandler.stopInventory(); //on release stops showing any new inventory
        }

    }

    @Override
    public void handleSetText(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusTextViewRFID.setText(msg);
            }
        });
    }

}
