/**
 */
package de.c4Api;



import android.util.Log;

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

import cn.pda.serialport.Tools;


public class C4ApiCordovaPlugin extends CordovaPlugin {
  
    private static final String TAG = "C4ApiCordovaPlugin";

    private CallbackContext _callBackContext;

    private UhfManager _uhfManager;

	private ArrayList<String> _listepc = new ArrayList<String>();
	private ArrayList<String> _listTID = new ArrayList<String>();
    private ArrayList<EPC> _listEPCObject;

    private boolean runFlag = true;
	private boolean startFlag = false;

	private String _errorLog;


  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing C4ApiCordovaPlugin");

    _errorLog = "";

    this._callBackContext = null;
	this._uhfManager = null;
	UhfManager.Port = 13;

    try {
		_uhfManager = UhfManager.getInstance();
    } catch (Exception e) {
		_errorLog = e.getMessage();
    //   e.printStackTrace();
    //   Log.d(TAG, "Error: " + e.getMessage());
    }

     Thread thread = new InventoryThread();
     thread.start();
   
  }

  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {

	if (_errorLog.length() > 0) {
		callbackContext.error(_errorLog);
		return true;
	}

    if (this._uhfManager == null) {
      // callbackContext.error("UHF API not installed");
      callbackContext.error("UHF API not installed");
      return true;
    }


    if (action.equals("echo")) {
      String phrase = args.getString(0);
      // Echo back the first argument
      Log.d(TAG, phrase);
    } else if (action.equals("getFirmware")) {

		final byte[] firmwareVersion = _uhfManager.getFirmware();
      // cordova.getThreadPool().execute(new Runnable() {
        // public void run() {
      

      cordova.getActivity().runOnUiThread(new Runnable() {

        public void run() {

          // String test = "test 1111";
          // callbackContext.success(firmwareVersion);

			    PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, firmwareVersion);
			    callbackContext.sendPluginResult(pluginResult);
        }
        
      });
      
      return true;
    } else if (action.equals("startInventory")) {

      //start inventory thread
      startFlag = true;

			_listEPCObject = new ArrayList<EPC>();

      this._callBackContext = callbackContext;
     
			return true;
			
    } else if(action.equals("stopInventory")) {
			startFlag = false;
			return true;
		}
      return false;
  }


	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume(false);
		// SharedPreferences preference = getSharedPreferences("serialport",
		// 		Context.MODE_PRIVATE);
		// UhfManager.Port = preference.getInt(what + "port", SerialPort.com13);
		// UhfManager.BaudRate = preference.getInt(what + "baudrate",
		// 		SerialPort.baudrate115200);
		// UhfManager.Power = preference.getInt(what + "power",
		// 		SerialPort.Power_Rfid);// this power is power source

		// String powerString = "";
		// switch (UhfManager.Power) {
		// case SerialPort.Power_3v3:
		// 	powerString = "power_3V3";
		// 	break;
		// case SerialPort.Power_5v:
		// 	powerString = "power_5V";
		// 	break;
		// case SerialPort.Power_Scaner:
		// 	powerString = "scan_power";
		// 	break;
		// case SerialPort.Power_Psam:
		// 	powerString = "psam_power";
		// 	break;
		// case SerialPort.Power_Rfid:
		// 	powerString = "rfid_power";
		// 	break;
		// default:
		// 	break;
		// }
		// TextView textView_title_config;
		// textView_title_config = (TextView) findViewById(R.id.textview_title_config);
		// textView_title_config.setText("Port:com" + UhfManager.Port
		// 		+ ";Baudrate:" + UhfManager.BaudRate + ";Power:" + powerString);
		this._uhfManager = UhfManager.getInstance();
		// if (manager == null) {
		// 	textVersion.setText(getString(R.string.serialport_init_fail_));
		// 	setButtonClickable(buttonClear, false);
		// 	setButtonClickable(buttonStart, false);
		// 	return;
		// }
		// try {
		// 	Thread.sleep(1000);
		// } catch (InterruptedException e) {
		// 	e.printStackTrace();
		// }
		

		// manager.setOutputPower(power);
		// manager.setWorkArea(area);
		// byte[] version_bs = manager.getFirmware();
		// setTitle(R.string.uhf_demo);
		// if (version_bs!=null){
		// 	textView_title_config.append("-"+new String(version_bs));
		// }
	}

  @Override
	public void onDestroy() {
		runFlag = false;
		if (this._uhfManager != null) {
			this._uhfManager.close();
		}
		super.onDestroy();
  }

	public void onPause() {
		startFlag = false;
		this._uhfManager.close();
		super.onPause(false);
	}

	// private JSONArray ConvertList(List<String> list) {
	// 	org.json.JSONArray jsonArray = new org.json.JSONArray();
	// 	for(String value : list) {
	// 		jsonArray.put(value);
	// 	}

	// 	return jsonArray;
	// }

	private JSONArray ConvertArrayList(ArrayList<String> list) {
		org.json.JSONArray jsonArray = new org.json.JSONArray();
		for(String value : list) {
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
							// 		epc.length);
							// addToList(_listEPCObject, epcStr);
							if(SelectEPC(epc)) {
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
				if (_callBackContext != null) {
					PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Fehler-SelectEPC: " + ex.getMessage());
					pluginResult.setKeepCallback(true);
					_callBackContext.sendPluginResult(pluginResult);
				}

				return false;
			}

			return true;
		}

		// first select tag by epc
		private byte[] GetTID() {
			// Parameters: int memBank store RESEVER zone 0, EPC District 1, TID District 2, USER District 3;
			// int startAddr starting address (not too large, depending on the size of the data area);
			// int length read data length, in units of word (1word = 2bytes); byte [] accessPassword password 4 bytes
			int tidLength = 6; // in word 1 word  = 2 byte
			//byte[] tid; // = new byte[tidLength*2];
			
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
							PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Fehler-GetTID tid error code: " + Tools.Bytes2HexString(tid, tid.length));
							pluginResult.setKeepCallback(true);
							_callBackContext.sendPluginResult(pluginResult);
							return null;
						}
					}
					return null;
				}

			} catch (Exception ex) {
				
				if (_callBackContext != null) {
					PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR, "Fehler-GetTID: " + ex.getMessage());
					pluginResult.setKeepCallback(true);
					_callBackContext.sendPluginResult(pluginResult);
				}
			}

			return null;
		}
	} // end inventory thread


	//add TIDs to view
	private void returnCurrentTIDs(final ArrayList<String> tidList) {
		cordova.getActivity().runOnUiThread(new Runnable() {
			
			public void run() {
				if (_callBackContext != null) {
					if (!tidList.isEmpty()) {
						PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, ConvertArrayList(tidList));
						pluginResult.setKeepCallback(true);
						_callBackContext.sendPluginResult(pluginResult);
					}
					
				}
			}
		});
	}

	// // EPC add to VIEW
	// private void addToList(final List<EPC> list, final String epc) {
	// 	cordova.getActivity().runOnUiThread(new Runnable() {
			
	// 		public void run() {
	// 			// The epc for the first time
	// 			if (list.isEmpty()) {
	// 				EPC epcTag = new EPC();
	// 				epcTag.setEpc(epc);
	// 				epcTag.setCount(1);
	// 				list.add(epcTag);

	// 			} else {
	// 				for (int i = 0; i < list.size(); i++) {
	// 					EPC mEPC = list.get(i);
	// 					// list contain this epc 
	// 					if (epc.equals(mEPC.getEpc())) {
	// 						mEPC.setCount(mEPC.getCount() + 1);
	// 						list.set(i, mEPC);
	// 						break;
	// 					} else if (i == (list.size() - 1)) {
	// 						// list doesn't contain this epc
	// 						EPC newEPC = new EPC();
	// 						newEPC.setEpc(epc);
	// 						newEPC.setCount(1);
	// 						list.add(newEPC);
  //             _listepc.add(epc);
              
  //             if (_callBackContext != null) {
  //               PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, ConvertArrayList(_listepc));
  //               pluginResult.setKeepCallback(true);
	// 			        _callBackContext.sendPluginResult(pluginResult);
  //             }
	// 					}
	// 				}
	// 			}
	// 		}
	// 	});
  // }
  
}