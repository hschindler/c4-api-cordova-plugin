/**
 */
package de.c4Api;

import android.os.Handler;

import com.handheld.Barcode1D.Barcode1DManager;
import com.handheld.UHF.UhfManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import cn.pda.serialport.SerialPort;
import cn.pda.serialport.Tools;

import android.util.Log;

public class C4ApiCordovaPlugin extends CordovaPlugin {

    private static final String TAG = "C4ApiCordovaPlugin";

    private CallbackContext _uhfCallBackContext;
    private CallbackContext _barcodeCallBackContext;

    private UhfManager _uhfManager;
    private Barcode1DManager _barcodeManager;

    private boolean _barcodeInitFlag = false;
    private boolean _initRuns = false;

    private ArrayList<String> _listepc = new ArrayList<String>();
    private ArrayList<String> _listTID = new ArrayList<String>();
    private ArrayList<EPC> _listEPCObject;

    private boolean runFlag = false;
    private boolean startFlag = false;

    private String _errorLog;

    private int _uhfPort = 13;

    private int _barcodePort = 0;
    private int _barcodePower = SerialPort.Power_Scaner;
    private int _barcodeBaudrate = 9600;

    private int _outputPower = 0;

    private Thread _scanThread;

    private Handler barcodeHandler = new Handler() {
        public void handleMessage(final android.os.Message msg) {
            cordova.getActivity().runOnUiThread(new Runnable() {

                public void run() {

                    Log.d(TAG, "handleMessage");

                    if (msg.what == Barcode1DManager.Barcode1D) {

                        Log.d(TAG, "handleMessage - Barcode1D");

                        String data = msg.getData().getString("data");

                        if (_barcodeCallBackContext != null) {

                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
                            pluginResult.setKeepCallback(true);
                            _barcodeCallBackContext.sendPluginResult(pluginResult);

                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException var2) {

                            }
                        }
                    }

                    Log.d(TAG, "handleMessage - close Barcode");
                    closeBarcodeManager();

                }
            });
        };
    };

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        Log.d(TAG, "Initializing C4ApiCordovaPlugin");

        _errorLog = "";

        this._uhfCallBackContext = null;
        this._barcodeCallBackContext = null;
        // this._uhfManager = null;

        // this.initializeUHFManager();

        Barcode1DManager.BaudRate = _barcodeBaudrate;
        Barcode1DManager.Port = _barcodePort;
        Barcode1DManager.Power = _barcodePower;

        try {
            if (_barcodeManager == null) {
                _barcodeManager = new Barcode1DManager();
            }
        } catch (Exception e) {
            _errorLog = e.getMessage();
            e.printStackTrace();
        }

        // try {
        // Thread.sleep(3000);
        // } catch (InterruptedException e)
        // {
        //
        // }

        // Thread thread = new InventoryThread();
        // thread.start();

    }

    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

        if (_errorLog.length() > 0) {
            callbackContext.error(_errorLog);
            return true;
        }

        if (action.equals("getFirmware")) {

            if (_uhfManager == null) {
                callbackContext.error("UHF API not installed");
                return true;
            }

            try {

                this.initializeUHFManager();

                final byte[] firmwareVersion = _uhfManager.getFirmware();

                this.disposeUHFManager();

                cordova.getActivity().runOnUiThread(new Runnable() {

                    public void run() {

                        // String test = "test 1111";
                        // callbackContext.success(firmwareVersion);

                        if (firmwareVersion == null) {
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, new byte[0]);
                            callbackContext.sendPluginResult(pluginResult);
                        } else {
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, firmwareVersion);
                            callbackContext.sendPluginResult(pluginResult);
                        }

                    }

                });
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            // cordova.getThreadPool().execute(new Runnable() {
            // public void run() {

            return true;
        } else if (action.equals("startInventory")) {

            Log.d(TAG, "startInventory");

            this.StartInventoryThread();
            _listEPCObject = new ArrayList<EPC>();

            this._uhfCallBackContext = callbackContext;

            return true;

        } else if (action.equals("stopInventory")) {
            Log.d(TAG, "stopInventory");
            this.StopInventoryThread();
            return true;
        } else if (action.equals("setOutputPower")) {

            // this.initializeUHFManager();

            // if (this._uhfManager == null) {
            // // callbackContext.error("UHF API not installed");
            // callbackContext.error("UHF API not installed");
            // return true;
            // }

            // values = (default), 16, 17, 18, (19, 20), 21, 22, 23
            if (args == null) {
                return false;
            }

            this._outputPower = args.getInt(0);

            // int power = args.getInt(0);
            // boolean result = _uhfManager.setOutputPower(power);
            return true;

        } else if (action.equals("openBarcode")) {

            return false;

            // callbackContext.success("OK");
            // return true;
        } else if (action.equals("closeBarcode")) {
            // try {
            // _barcodeManager.Close();
            // } catch (Exception e) {
            // _errorLog = e.getMessage();
            // return false;
            // }

            return true;
        } else if (action.equals("scanBarcode")) {

            try {
                this._barcodeCallBackContext = callbackContext;

                Barcode1DManager.BaudRate = _barcodeBaudrate;
                Barcode1DManager.Port = _barcodePort;
                Barcode1DManager.Power = _barcodePower;

                _barcodeManager.Open(barcodeHandler);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                _barcodeManager.Scan();

            } catch (Exception e) {
                _errorLog = e.getMessage();

                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, _errorLog);
                pluginResult.setKeepCallback(true);
                _barcodeCallBackContext.sendPluginResult(pluginResult);

                return false;
            }

            return true;

        }

        return false;
    }

    public void onResume(boolean multitasking) {
        // TODO Auto-generated method stub
        // super.onResume(multitasking);

        Log.d(TAG, "onResume - runFlag: " + String.valueOf(startFlag));

        // this.initializeUHFManager();

        if (this.runFlag == true) {
            this.StartInventoryThread();
        }

    }

    public void onRestart() {
        // TODO Auto-generated method stub
        // super.onRestart();

        Log.d(TAG, "onRestart");

        // this.initializeUHFManager();

        if (this.runFlag == true) {
            this.StartInventoryThread();
        }
    }

    public void onDestroy() {
        // super.onDestroy();
        Log.d(TAG, "onDestroy");

        this.StopInventoryThread();

        this.disposeUHFManager();

        this.closeBarcodeManager();

        // closeBarcode();

    }

    public void onStop() {
        // super.onStop();

        Log.d(TAG, "onStop");
        this.StopInventoryThread();

        this.closeBarcodeManager();

    }

    public void onPause(boolean multitasking) {
        // super.onPause(multitasking);
        Log.d(TAG, "onPause");
        this.StopInventoryThread();

        this.closeBarcodeManager();
    }

    private void initializeUHFManager() {

        if (this._uhfManager == null) {
            UhfManager.Port = _uhfPort;
            UhfManager.BaudRate = 115200;
            UhfManager.Power = SerialPort.Power_Rfid;

            try {
                this._uhfManager = UhfManager.getInstance();

                if (this._outputPower > 0) {
                    boolean result = _uhfManager.setOutputPower(this._outputPower);
                }

            } catch (Exception e) {
                _errorLog = e.getMessage();
                e.printStackTrace();
                // Log.d(TAG, "Error: " + e.getMessage());
            }
        }
    }

    private void closeBarcodeManager() {
        if (_barcodeManager != null) {
            Log.d(TAG, "closeBarcodeManager");

            try {
                _barcodeManager.Close();
            } catch (Exception e) {
                _errorLog = e.getMessage();
            }

        }
    }

    private void disposeUHFManager() {

        if (this._uhfManager != null) {
            Log.d(TAG, "disposeUHFManager");

            try {
                this._uhfManager.close();
            } catch (Exception e) {
                _errorLog = e.getMessage();
            }

            this._uhfManager = null;
        }
    }

    private void StartInventoryThread() {

        Log.d(TAG, "StartInventoryThread");

        // start inventory thread
        startFlag = true;
        runFlag = true;

        if (this._scanThread == null) {
            Log.d(TAG, "StartInventoryThread - create new thread");
            this._scanThread = new InventoryThread();
        }

        Log.d(TAG, "StartInventoryThread - start thread");
        this._scanThread.start();
    }

    private void StopInventoryThread() {
        runFlag = false;
        startFlag = false;
    }

    private void PauseInventoryThread() {
        startFlag = false;
    }

    private JSONArray ConvertArrayList(ArrayList<String> list) {
        org.json.JSONArray jsonArray = new org.json.JSONArray();
        for (String value : list) {
            jsonArray.put(value);
        }

        return jsonArray;
    }

    /**
     * Inventory EPC Thread
     */
    class InventoryThread extends Thread {
        private List<byte[]> epcList;
        private ArrayList<String> tidList;

        @Override
        public void run() {
            super.run();

            Log.d(TAG, "InventoryThread starting...");

            initializeUHFManager();
            // if (_uhfManager == null) {
            // Log.d(TAG, "InventoryThread creating new _uhfManager");
            // _uhfManager = UhfManager.getInstance();

            // }

            Log.d(TAG, "InventoryThread startflag = " + String.valueOf(startFlag));

            while (startFlag) {

                Log.d(TAG, "Waiting for timeout..");

                if (_uhfManager != null) {

                    epcList = _uhfManager.inventoryRealTime(); // inventory real time

                    if (epcList != null && !epcList.isEmpty()) {
                        // play sound
                        // Util.play(1, 0);
                        tidList = new ArrayList<String>();

                        for (byte[] epc : epcList) {

                            if (SelectEPC(epc)) {
                                byte[] tid = GetTID();

                                if (tid != null) {
                                    String tidStr = Tools.Bytes2HexString(tid, tid.length);
                                    tidList.add(tidStr);
                                }
                            }
                        }

                        if (!tidList.isEmpty()) {
                            if (tidList.size() > 0) {
                                returnCurrentTIDs(tidList);
                            }

                        }

                    }

                } else {
                    returnCurrentTIDs(null);
                }

                epcList = null;

                try {
                    Thread.sleep(40);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // }
            } // while

            Log.d(TAG, "InventoryThread is closing...");

            disposeUHFManager();

        } // run

        private boolean SelectEPC(byte[] epc) {
            try {
                if (_uhfManager != null) {
                    _uhfManager.selectEPC(epc);
                }
            } catch (Exception ex) {
                if (_uhfCallBackContext != null) {
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                            "Fehler-SelectEPC: " + ex.getMessage());
                    pluginResult.setKeepCallback(true);
                    _uhfCallBackContext.sendPluginResult(pluginResult);
                }

                return false;
            }

            return true;
        }

        // first select tag by epc
        private byte[] GetTID() {
            // Parameters: int memBank store RESEVER zone 0, EPC District 1, TID District 2,
            // USER District 3;
            // int startAddr starting address (not too large, depending on the size of the
            // data area);
            // int length read data length, in units of word (1word = 2bytes); byte []
            // accessPassword password 4 bytes
            int tidLength = 6; // in word 1 word = 2 byte
            // byte[] tid; // = new byte[tidLength*2];

            if (_uhfManager == null) {
                return null;
            }

            Log.d(TAG, "GetTID");

            try {
                byte[] pw = new byte[4];
                byte[] tid = _uhfManager.readFrom6C(2, 0, tidLength, pw);

                if (tid != null && tid.length > 1) {

                    Log.d(TAG, "GetTID - " + tid);
                    return tid;

                } else {
                    if (tid != null) {
                        // tid has error code

                        // try again with small tid (8 byte)
                        tidLength = 4;
                        tid = _uhfManager.readFrom6C(2, 0, tidLength, pw);

                        if (tid != null && tid.length > 1) {
                            return tid;
                        } else {
                            // tid has error code
                            PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                                    "Fehler-GetTID tid error code: " + Tools.Bytes2HexString(tid, tid.length));
                            pluginResult.setKeepCallback(true);
                            _uhfCallBackContext.sendPluginResult(pluginResult);
                            return null;
                        }
                    }
                    return null;
                }

            } catch (Exception ex) {

                if (_uhfCallBackContext != null) {
                    PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
                            "Fehler-GetTID: " + ex.getMessage());
                    pluginResult.setKeepCallback(true);
                    _uhfCallBackContext.sendPluginResult(pluginResult);
                }
            }

            return null;
        }
    } // end inventory thread class

    // add TIDs to view
    private void returnCurrentTIDs(final ArrayList<String> tidList) {
        cordova.getActivity().runOnUiThread(new Runnable() {

            public void run() {
                if (_uhfCallBackContext != null) {
                    if (!tidList.isEmpty()) {
                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, ConvertArrayList(tidList));
                        pluginResult.setKeepCallback(true);
                        _uhfCallBackContext.sendPluginResult(pluginResult);
                    }

                }
            }
        });
    }

}