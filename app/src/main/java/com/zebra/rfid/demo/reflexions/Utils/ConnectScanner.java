package com.zebra.rfid.demo.reflexions.Utils;

import android.os.AsyncTask;

import com.zebra.scannercontrol.SDKHandler;

public class ConnectScanner extends AsyncTask {
    int scannerId;
    SDKHandler sdkHandler;

    public ConnectScanner(int scannerId, SDKHandler sdkHandler){
        this.scannerId=scannerId;
        this.sdkHandler=sdkHandler;
    }

//    protected Void doInBackground(Void... voids) {
//        sdkHandler.dcssdkEstablishCommunicationSession(scannerId);
//        return null;
//    }

    @Override
    protected Object doInBackground(Object[] objects) {
        sdkHandler.dcssdkEstablishCommunicationSession(scannerId);
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }
}
