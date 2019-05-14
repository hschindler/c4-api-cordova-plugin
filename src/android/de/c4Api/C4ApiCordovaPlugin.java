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

    private boolean runFlag = true;
    private boolean startFlag = false;

    private String _errorLog;

    private int _uhfPort = 13;

    private int _barcodePort = 0;
    private int _barcodePower = SerialPort.Power_Scaner;
    private int _barcodeBaudrate = 9600;

    private Handler barcodeHandler = new Handler() {
        public void handleMessage(final android.os.Message msg) {
            cordova.getActivity().runOnUiThread(new Runnable() {

                public void run() {

                    if (msg.what == Barcode1DManager.Barcode1D) {
                        String data = msg.getData().getString("data");

                        if (_barcodeCallBackContext != null) {

                            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, data);
                            pluginResult.setKeepCallback(true);
                            _barcodeCallBackContext.sendPluginResult(pluginResult);

                        }
                    }
                }
            });
        };
    };

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);

        // Log.d(TAG, "Initializing C4ApiCordovaPlugin");

        _errorLog = "";

        this._uhfCallBackContext = null;
        this._barcodeCallBackContext = null;
        this._uhfManager = null;
        UhfManager.Port = _uhfPort;

        try {
            _uhfManager = UhfManager.getInstance();
        } catch (Exception e) {
            _errorLog = e.getMessage();
            e.printStackTrace();
            // Log.d(TAG, "Error: " + e.getMessage());
        }

        try {
            _barcodeManager = new Barcode1DManager();
            _barcodeManager.Open(barcodeHandler);
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

        Thread thread = new InventoryThread();
        thread.start();

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
                final byte[] firmwareVersion = _uhfManager.getFirmware();

                cordova.getActivity().runOnUiThread(new Runnable() {

                    public void run() {

                        // String test = "test 1111";
                        // callbackContext.success(firmwareVersion);

                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, firmwareVersion);
                        callbackContext.sendPluginResult(pluginResult);
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

            if (this._uhfManager == null) {
                // callbackContext.error("UHF API not installed");
                callbackContext.error("UHF API not installed");
                return true;
            }

            // start inventory thread
            startFlag = true;

            _listEPCObject = new ArrayList<EPC>();

            this._uhfCallBackContext = callbackContext;

            return true;

        } else if (action.equals("stopInventory")) {
            startFlag = false;
            return true;
        } else if (action.equals("setOutputPower")) {

            if (this._uhfManager == null) {
                // callbackContext.error("UHF API not installed");
                callbackContext.error("UHF API not installed");
                return true;
            }

            // values = (default), 16, 17, 18, (19, 20), 21, 22, 23
            int power = args.getInt(0);

            boolean result = _uhfManager.setOutputPower(power);
            return result;

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
                _barcodeManager.Scan();
            } catch (Exception e) {
                _errorLog = e.getMessage();

                PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, _errorLog);
                pluginResult.setKeepCallback(true);
                _barcodeCallBackContext.sendPluginResult(pluginResult);

                return false;
            }

            return true;

            // this._barcodeCallBackContext = callbackContext;

            // String result = "";
            // // byte[] resultByte = null;

            // if (_barcodeInitFlag) {
            // try {
            // boolean is = _barcodeScaner.callbackKeepGoing();
            // Thread.sleep(50);
            // _barcodeScaner.waitForDecodeTwo(3000, _barcodeScanerResult);
            // //_barcodeScaner.waitForDecode(3000) ;
            // result = _barcodeScaner.getBarcodeData();
            // // resultByte = _barcodeScaner.getBarcodeByteData();
            // if (result != null) {
            // // Log.e(TAG, result);
            // PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, result);
            // _barcodeCallBackContext.sendPluginResult(pluginResult);
            // }
            // } catch (DecoderException e) {
            // // TODO Auto-generated catch block
            // // e.printStackTrace();
            // _errorLog = e.getMessage();

            // PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
            // _errorLog);
            // pluginResult.setKeepCallback(true);
            // _barcodeCallBackContext.sendPluginResult(pluginResult);

            // return true;
            // } catch (InterruptedException e) {
            // // TODO Auto-generated catch block
            // // e.printStackTrace();

            // _errorLog = e.getMessage();

            // PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
            // _errorLog);
            // pluginResult.setKeepCallback(true);
            // _barcodeCallBackContext.sendPluginResult(pluginResult);

            // return true;
            // }
            // } else {

            // callbackContext.error("Barcode API not installed");
            // return true;
            // }

            // callbackContext.error("Barcode read success");
            // return true;

            // -------
            // try {
            // this._barcodeCallBackContext = callbackContext;
            // _barcodeManager.Scan();
            // } catch (Exception e) {
            // _errorLog = e.getMessage();

            // PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
            // _errorLog);
            // pluginResult.setKeepCallback(true);
            // _barcodeCallBackContext.sendPluginResult(pluginResult);

            // return false;
            // }

            // return true;
        }

        return false;
    }

    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume(false);

        UhfManager.Port = _uhfPort;
        UhfManager.BaudRate = 115200;
        UhfManager.Power = SerialPort.Power_Rfid;

        this._uhfManager = UhfManager.getInstance();

        Barcode1DManager.BaudRate = _barcodeBaudrate;
        Barcode1DManager.Port = _barcodePort;
        Barcode1DManager.Power = _barcodePower;
        // if (manager == null) {
        // textVersion.setText(getString(R.string.serialport_init_fail_));
        // setButtonClickable(buttonClear, false);
        // setButtonClickable(buttonStart, false);
        // return;
        // }
        // try {
        // Thread.sleep(1000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

        // manager.setOutputPower(power);
        // manager.setWorkArea(area);
        // byte[] version_bs = manager.getFirmware();
        // setTitle(R.string.uhf_demo);
        // if (version_bs!=null){
        // textView_title_config.append("-"+new String(version_bs));
        // }
    }

    @Override
    public void onDestroy() {
        runFlag = false;
        if (this._uhfManager != null) {
            this._uhfManager.close();
        }

        if (_barcodeManager != null) {
            _barcodeManager.Close();
        }

        // closeBarcode();

        super.onDestroy();
    }

    public void onPause() {
        startFlag = false;
        this._uhfManager.close();
        super.onPause(false);
    }

    // private JSONArray ConvertList(List<String> list) {
    // org.json.JSONArray jsonArray = new org.json.JSONArray();
    // for(String value : list) {
    // jsonArray.put(value);
    // }

    // return jsonArray;
    // }

    // private void initBarcode() {
    // try {
    // _barcodeScaner.disableSymbology(SymbologyID.SYM_ALL);
    // _barcodeScaner.enableSymbology(SymbologyID.SYM_ALL);
    // // mDecoder.enableSymbology(SymbologyID.SYM_QR);
    // // mDecoder.enableSymbology(SymbologyID.SYM_PDF417);
    // // mDecoder.enableSymbology(SymbologyID.SYM_EAN13);
    // // mDecoder.enableSymbology(SymbologyID.SYM_DATAMATRIX) ;
    // //
    // // mDecoder.enableSymbology(SymbologyID.SYM_INT25);

    // // mDecoder.enableSymbology(SymbologyID.SYM_UPCA);
    // // mDecoder.enableSymbology(SymbologyID.SYM_CHINAPOST);
    // //
    // // mDecoder.enableSymbology(SymbologyID.SYM_CODE39);
    // // mDecoder.enableSymbology(SymbologyID.SYM_CODE128);
    // // mDecoder.enableSymbology(SymbologyID.SYM_EAN8);
    // // mDecoder.enableSymbology(SymbologyID.SYM_CODE32);
    // //EAN13
    // SymbologyConfig config = new SymbologyConfig(SymbologyID.SYM_EAN13);
    // config.Flags = 5;
    // config.Mask = 1;
    // _barcodeScaner.setSymbologyConfig(config);

    // _barcodeScaner.setLightsMode(LightsMode.ILLUM_AIM_ON);
    // try {
    // _barcodeScaner.startScanning();
    // Thread.sleep(50);
    // _barcodeScaner.stopScanning();
    // } catch (Exception e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }

    // } catch (DecoderException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }

    // public void closeBarcode() {
    // if (_barcodeInitFlag) {
    // try {
    // _barcodeScaner.disconnectDecoderLibrary();
    // } catch (DecoderException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    // }
    // }

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
            while (runFlag) {
                if (startFlag) {
                    // manager.stopInventoryMulti()
                    epcList = _uhfManager.inventoryRealTime(); // inventory real time
                    if (epcList != null && !epcList.isEmpty()) {
                        // play sound
                        // Util.play(1, 0);
                        tidList = new ArrayList<String>();

                        for (byte[] epc : epcList) {
                            // String epcStr = Tools.Bytes2HexString(epc,
                            // epc.length);
                            // addToList(_listEPCObject, epcStr);
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
                    epcList = null;
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            } // while
        } // run

        private boolean SelectEPC(byte[] epc) {
            try {
                _uhfManager.selectEPC(epc);

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

            try {
                byte[] pw = new byte[4];
                byte[] tid = _uhfManager.readFrom6C(2, 0, tidLength, pw);

                if (tid != null && tid.length > 1) {
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