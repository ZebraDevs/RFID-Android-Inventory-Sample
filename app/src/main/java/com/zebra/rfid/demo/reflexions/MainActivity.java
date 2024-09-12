package com.zebra.rfid.demo.reflexions;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.FileUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zebra.rfid.api3.TagData;
import com.zebra.rfid.demo.reflexions.Utils.DataWedge;
import com.zebra.rfid.demo.reflexions.Utils.RecyclerTouchListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements RFIDHandler.ResponseHandlerInterface {
    final static String TAG = "INVENTORY_RFID";
    //DATA STRUCTURES
    private final HashMap<String, ItemData> epcItemMap = new HashMap<String, ItemData>();
    private final HashMap<String, Short> scannedEPC = new HashMap<String, Short>();
    private ArrayList<ItemData> itemDataToShow = new ArrayList<ItemData>();
    private final HashMap<String, Integer> expectedSKUCount = new HashMap<String, Integer>();
    private int count = 0;
    //RFID
    public RFIDHandler rfidHandler;
    public RFIDHandler.ResponseHandlerInterface response;
    public TextView statusTextViewRFID;
    private int currentPower; //remember to multiply by 10
    //SHARED PREFS
    private SharedPreferences sharedPreferences;
    final static String KEY_POWER = "power"; //shared preferences
    final static String KEY_URI = "key_uri";
    //UI
    private InventoryVM vm;
    private ItemAdapter myAdapter;

    //URI variable
    public static String savedUri;
    private Uri uri = null;

    private ExecutorService executorService;
    private Context ctx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        hideNavBar();
        ctx = this;
        response = this;

        setContentView(R.layout.activity_main);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        //shared preferences
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        savedUri = sharedPreferences.getString(KEY_URI, null);  //check for saved directory path
        if(sharedPreferences.getString(KEY_URI, null) == null) {
            selectDirectory();
        }
        if(savedUri != null && savedUri.length() > 0) {
            copyImages(Uri.parse(savedUri)); //copy images from directory
        }

        //UI
        myAdapter = new ItemAdapter(this, itemDataToShow);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        ((TextView) findViewById(R.id.textViewTotalSeen)).setText("Total found: " + count); //set found to zero

        vm = new ViewModelProvider(this).get(InventoryVM.class);
        vm.setItems(itemDataToShow);

        // in below two lines we are setting LayoutManager and adapter to our recycler view.
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(myAdapter);

        //recyclerview touch listener
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(this, recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //none
            }

            @Override
            public void onLongClick(View view, int position) {
                if(itemDataToShow.get(position).getSeencount() == 0) {
                    Log.i(TAG, "onLongClick: No tags have been read for this item.");
                } else {
                    String locateEpc = findEpc(itemDataToShow.get(position).getSku());
                    setEPC(locateEpc);
                    setName(itemDataToShow.get(position).getItemname());
                    Log.i(TAG, "onLongClick: vm array size before launch: " + vm.getItems().size());
                    launchScreen(new Intent(MainActivity.this, LocatorActivity.class));
                    Log.i(TAG, "onLongClick: vm array size after launch: " + vm.getItems().size());
                }
            }
        }));

        //settings button function
        findViewById(R.id.settingsBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Initializing the popup menu and giving the reference as current context
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, findViewById(R.id.settingsBtn));

                // Inflating popup menu from popup_menu.xml file
                popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();

                        //noinspection SimplifiableIfStatement
                        if (id == R.id.action_settings) {
                            View v = getLayoutInflater().inflate(R.layout.setting_antenna, null);
                            final SeekBar antenna = v.findViewById(R.id.seekBar);
                            final TextView seekValue = v.findViewById(R.id.antennaPower);
                            //set current value here
                            seekValue.setText(Integer.toString(currentPower));
                            antenna.setProgress(currentPower);
                            antenna.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                    seekValue.setText(Integer.toString(antenna.getProgress()));
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            });
                            new AlertDialog.Builder(ctx)
                                    .setTitle("Settings")
                                    .setView(v)
                                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            //save rfid antenna settings
                                            int power = antenna.getProgress();
                                            currentPower = power;
                                            PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit().putInt(KEY_POWER, power).commit();
                                            try {
                                                rfidHandler.setPower(currentPower);
                                                Toast.makeText(MainActivity.this, "Power set success: " + currentPower, Toast.LENGTH_SHORT).show();
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                                Toast.makeText(MainActivity.this, "Failed to set Power: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                            Toast.makeText(MainActivity.this, "Power: " + power, Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .show();
                        }
                        return true;
                    }
                });
                popupMenu.show();
            }
        });

        //clear button function
        findViewById(R.id.clearBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = 0; //reset count
                ((TextView) findViewById(R.id.textViewTotalSeen)).setText("Total found: " + count);
                itemDataToShow.clear();
                scannedEPC.clear();
                loadList(); //reload empty list into adapter
                myAdapter.notifyDataSetChanged();
            }
        });
        executorService = Executors.newSingleThreadExecutor();
    }

    //use to launch an activity
    private void launchScreen(Intent i)
    {
        startActivity(i);
        overridePendingTransition(0,0);
    }

    private void hideNavBar() {
        View decorView = getWindow().getDecorView();

        final int flags =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        decorView.setSystemUiVisibility(flags);

        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if((visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0) {
                    // The system bars are visible. Make any desired
                    // adjustments to your UI, such as showing the action bar or
                    // other navigational controls.
                    decorView.setSystemUiVisibility(flags);
                } else {
                    // The system bars are NOT visible. Make any desired
                    // adjustments to your UI, such as hiding the action bar or
                    // other navigational controls.

                }
            }
        });
    }

    public String findEpc(String sku) {
        ArrayList<String> items = new ArrayList<>();
        Short peakRSSI = -1000;
        String highestRSSI = "";

        for (Map.Entry<String, ItemData> epc : epcItemMap.entrySet()) {
            if(Objects.equals(epc.getValue().getSku(), sku)) {
                if(scannedEPC.containsKey(epc.getKey())) {
                    items.add(epc.getKey());
                }
            }
        }

        for(String epc : items) {
            Short epcRssi = scannedEPC.get(epc);

            if(Objects.requireNonNull(scannedEPC.get(epc)).compareTo(peakRSSI) > 0) { //if epc RSSI is greater than peakRSSI
                //new peakRSSI val && set epc to highestRSSI
                peakRSSI = epcRssi;
                highestRSSI = epc;
            } else {
                Log.i(TAG, "findEpc: less than peak rssi");
                Log.i(TAG, "findEpc: logged RSSI" + epcRssi);
            }
        }
        return highestRSSI;
    }

    public void setEPC(String epc) {
        LocatorActivity.epc = epc;
    }

    public void setName(String name) {
        LocatorActivity.name = name;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //power
        currentPower = PreferenceManager.getDefaultSharedPreferences(this).getInt(KEY_POWER, 30);

        //RFID
        statusTextViewRFID = findViewById(R.id.textStatus);       //rfid connection status
        rfidHandler = RFIDHandler.getInstance(currentPower, this);
        rfidHandler.setHandler(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        savedUri = sharedPreferences.getString(KEY_URI, null);
        if(savedUri != null && savedUri.length() > 0) {
            Log.d(TAG, "Uri is : " + savedUri);
            uri = Uri.parse(savedUri);
        }
        if(uri != null || vm.getItems().size() == 0) { // if directory is selected
            if(vm.getItems() == null || vm.getItems().size() == 0) {
                readConfig(); //reads config file with the items
                loadList(); //loads empty list of items
            } else {
                itemDataToShow = vm.getItems();
                Log.i(TAG, "onResume: retrieving items");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: destroy");
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }

    /**
     * Select a directory that you want the application to be able to read from
     */
    private void selectDirectory() {
        // Choose a directory using the system's file picker.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);

        //permissions
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        someActivityResultLauncher.launch(intent);

    }

    //handles directory selection
    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data == null) { //checks if data is null
                            Log.d(TAG, "No data");
                            return;
                        }
                        Uri treeUri = data.getData();
                        try {
                            getContentResolver().takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            Log.d(TAG, "Permissions Granted");
                            uri = treeUri;

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, "Permissions not set");
                        }

                        Log.d(TAG, "Uri: " + uri.toString()); //saved Value
                        //save uri
                        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
                        sharedPreferences.edit().putString(KEY_URI, uri.toString()).apply(); //edits and commits (saves) preference in one line

                        savedUri = sharedPreferences.getString(KEY_URI, null);
                        if(savedUri != null && savedUri.length() > 0) {
                            copyImages(Uri.parse(savedUri));
                        }
                    }
                }
            });

    /**
     * Selects a specific document from the directory and gets it's URI path
     * @param uri - path of the selected directory
     * @param docName - name of file
     * @return - uri of the document that will be read
     */
    private Uri loadDocument(Uri uri, String docName) {
        DocumentFile rootTree = null;
        if(uri != null) {
            rootTree = DocumentFile.fromTreeUri(ctx, uri); //reads from the chosen directory tree
        }

        if (rootTree == null) {
            Log.i(TAG, "loadDocument: rootTree is null, no URI");
            return null;
        } else {
            DocumentFile docFile = rootTree.findFile(docName); //finds file by display name
            if (docFile != null) {
                return docFile.getUri();
            } else {
                Log.i(TAG, "loadDocument: docFile is null, file not found");
                return null;
            }
        }
    }

    /**
     * Copies images from the chosen directory to internal storage
     * @param uri - selected directory path
     */
    private void copyImages(Uri uri) {
        DocumentFile rootTree = DocumentFile.fromTreeUri(ctx, uri); //reads from the chosen directory tree
        DocumentFile[] fileList;
        if (rootTree != null) {
            fileList = rootTree.listFiles(); //lists files
            for (DocumentFile documentFile : fileList) {
                if (documentFile.getName() != null && (documentFile.getName().contains(".jpeg") ||
                        documentFile.getName().contains(".jpg") || documentFile.getName().contains(".png"))
                ) {
                    Uri source = documentFile.getUri();
                    Log.i(TAG, "copyImages: " + source);
                    File destination = getApplicationContext().getFilesDir();
                    //unsure how to get the "file://" to appear automatically like in Kotlin
                    Uri destinationUri = Uri.parse("file://" + destination + "/" + documentFile.getName());
                    Log.i(TAG, "copyImages: " + destination);
                    Log.i(TAG, "copyImages: " + destinationUri);
                    try {
                        InputStream inputStream = getApplicationContext().getContentResolver().openInputStream(source);
                        OutputStream outputStream = getApplicationContext().getContentResolver().openOutputStream(destinationUri);
                        if(inputStream != null && outputStream != null) {
                            FileUtils.copy(inputStream, outputStream);
                            inputStream.close();
                            outputStream.close();
                        } else {
                            Log.i(TAG, "copyImages: input or output stream is null");
                        }
                        Log.i(TAG, "copyImages: image copied " + documentFile.getName());
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.i(TAG, "copyImages: copy failed");
                    }

                }
            }
        }
    }

    @Override
    public void handleTagdata(final TagData[] tagData) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Data: " + tagData);
                updateTagData(tagData);

            }
        });
    }

    @Override
    public void handleTriggerPress(boolean pressed) {
        Log.d(TAG, "TRIGGER WAS PRESSED");
        if (pressed) {
            rfidHandler.performInventory();

        } else {
            rfidHandler.stopInventory();
            replenishItemsDlg(itemDataToShow); //calls dialogs to send post for items in need of replenishment
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
    
    //alert dialog to replenish items
    //build string here for the html table
    public void replenishItemsDlg(final ArrayList<ItemData> seenItems) {
        boolean send = false;
        StringBuilder itemsMissing = new StringBuilder();

        for (int i = 0; i < seenItems.size(); i++) {
            final ItemData replenish = seenItems.get(i);
            double percent = ((double) replenish.getSeencount() / replenish.getExpectedCount()); //percent in store/inventory
            if (percent < .6) { //less than 60% percent of expected stock in store
                if (replenish.getItemname() == null) {
                    return;
                } else {
                    send = true;
                    itemsMissing.append("- ").append(replenish.getItemname()).append(", ").append(replenish.getSize()).append("\n");
                    Log.i(TAG, replenish.getItemname() + " needs replenishment.");
                }
            } else {
                Log.i(TAG, replenish.getItemname() + " doesn't need to be replenished.");
            }
        }

        if (send) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage("These items are missing:\n" + itemsMissing + "\n")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() { //Send post
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() { //Cancel
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                            dialog.dismiss();
                        }
                    });
            // show dialog
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    builder.show();
                }
            });
        } else {
            Log.i(TAG, "replenishItemsDlg: Inventory is good.");
        }
    }

    private void readConfig() {
        BufferedReader reader;
        Uri docToLoad = loadDocument(uri, "config.txt");
        epcItemMap.clear();
        expectedSKUCount.clear();
        try {
            if (docToLoad == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, "Config document not found.", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            } else {
                InputStream inputStream = getContentResolver().openInputStream(docToLoad);
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = reader.readLine();
                while (line != null) {
                    String[] splitLine = line.split(",");
                    String epc = splitLine[0];
                    String image = splitLine[1];
                    String itemname = splitLine[2];
                    String size = splitLine[3];
                    String sku = splitLine[4];
                    epcItemMap.put(epc, new ItemData(epc,size,itemname,image,sku));
                    if(!expectedSKUCount.containsKey(sku)){
                        expectedSKUCount.put(sku,1);
                    }else{
                        expectedSKUCount.put(sku,expectedSKUCount.get(sku)+1);
                    }
                    Log.d(TAG, "Added new item : "+epc+" "+itemname+" "+image+" "+size);
                    line = reader.readLine();
                }
                reader.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.textViewTotalExpected)).setText("Total expected: "+epcItemMap.size());
            }
        });
    }

    private void loadList() {
        for (Map.Entry<String, ItemData> epc : epcItemMap.entrySet()) {
            String tagID = epc.getKey();
            boolean newitem = true;
            for (ItemData item : itemDataToShow) {
                if (item.getSku().equals(epcItemMap.get(tagID).getSku())) {
                    newitem = false;
                    break;
                }
            }
            if (newitem) {
                ItemData itemdata = epcItemMap.get(tagID);
                String name = itemdata.getItemname();
                String sku = itemdata.getSku();
                String size = itemdata.getSize();
                String image = itemdata.getImage();
                itemDataToShow.add(new ItemData(tagID, size, name, image, sku, 0, expectedSKUCount.get(sku)));
            }
        }
    }


    private void updateTagData(TagData[] tagData) {

        for (int index = 0; index < tagData.length; index++) {
            TagData tag = tagData[index];
            String tagID = tag.getTagID();
            Short rssi = tag.getPeakRSSI(); //TODO: save rssi value to use later
            Log.i(TAG, "updateTagData: " + rssi);
            if (epcItemMap.containsKey(tagID)) {
                boolean newitem = true;
                if (!scannedEPC.containsKey(tagID)) {
                    scannedEPC.put(tagID, rssi);
                    count++;
                    for (ItemData item : itemDataToShow) {
                        if (item.getSku().equals(epcItemMap.get(tagID).getSku())) {
                            newitem = false;
                            item.increaseCount();

                        }
                    }

                    if (newitem) {
                        ItemData itemdata = epcItemMap.get(tagID);
                        String name = itemdata.getItemname();
                        String sku = itemdata.getSku();
                        String size = itemdata.getSize();
                        String image = itemdata.getImage();
                        itemDataToShow.add(new ItemData(tagID, size, name, image, sku, 1, expectedSKUCount.get(sku)));
                        count++;
                    } else {
                        return;
                    }
                } else {
                    //update rssi for the tag that was alreadt read
                    scannedEPC.put(tagID, rssi);
                }

            } else {
                Log.d(TAG, "Don't know tag " + tagID);
            }
        }
        ((TextView) findViewById(R.id.textViewTotalSeen)).setText("Total found: " + count);

        myAdapter.notifyDataSetChanged();

    }
}