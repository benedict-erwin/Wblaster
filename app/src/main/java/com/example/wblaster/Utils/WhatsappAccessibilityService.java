package com.example.wblaster.Utils;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import com.example.wblaster.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

public class WhatsappAccessibilityService extends AccessibilityService {
    @Override
    public void onAccessibilityEvent (AccessibilityEvent event) {
        // Test JSON
        String jsonString = "{ \"broadcast\": [ { \"name\": \"Erwin\", \"phone\": \"085749518656\", \"msg_id\": \"1\", \"msg_content\": \"This is a whatsapp message Send By WBLASTER\" }, { \"name\": \"ArRay\", \"phone\": \"081249666266\", \"msg_id\": \"2\", \"msg_content\": \"This is a whatsapp message Send By WBLASTER\" }, { \"name\": \"Basuki\", \"phone\": \"085755566649\", \"msg_id\": \"2\", \"msg_content\": \"This is a whatsapp message Send By WBLASTER\" } ] }";
        try {
            JSONObject jsonObj = new JSONObject(jsonString);
            JSONArray broadCast = jsonObj.getJSONArray("broadcast");
            Log.d("DBG_X", "MainActivity :: TESTjson :: length : " + broadCast.length());

            if (broadCast.length() > 0){
                for (int i = 0; i < broadCast.length(); i++){
                    JSONObject data = broadCast.getJSONObject(i);
                    String name = data.getString("name");
                    String phone = data.getString("phone");
                    String msgId = data.getString("msg_id");
                    String message = data.getString("msg_content");
                    sendMessage(phone, message);
                    Log.d("DBG_X", "MainActivity :: TESTjson :: data >> Name : " + name + ", Phone : " + phone + ", msgId : " + msgId + ", message : '" + message +"'");

                    try {
                        Thread.sleep (3000);
                    } catch (InterruptedException ignored) {}

                    Log.d("DBG_X", "WhatsappAccessibilityService :: onAccessibilityEvent :: start");
                    if (getRootInActiveWindow () == null) {
                        return;
                    }

                    AccessibilityNodeInfoCompat rootInActiveWindow = AccessibilityNodeInfoCompat.wrap (getRootInActiveWindow ());
                    Log.d("DBG_X", "WhatsappAccessibilityService :: rootInActiveWindow :: step 1 - " + rootInActiveWindow.isVisibleToUser());

                    // Check npe
                    if (!rootInActiveWindow.isVisibleToUser()){
                        sendMessage(phone, message);
                        try {
                            Thread.sleep (3000);
                        } catch (InterruptedException ignored) {}
                    }

                    // Whatsapp Message EditText id
                    List<AccessibilityNodeInfoCompat> messageNodeList = rootInActiveWindow.findAccessibilityNodeInfosByViewId ("com.whatsapp:id/entry");
                    Log.d("DBG_X", "WhatsappAccessibilityService :: rootInActiveWindow :: step 2");
                    if (messageNodeList == null || messageNodeList.isEmpty ()) {
                        return;
                    }

                    // check if the whatsapp message EditText field is filled with text and ending with your suffix (explanation above)
                    AccessibilityNodeInfoCompat messageField = messageNodeList.get (0);
                    Log.d("DBG_X", "WhatsappAccessibilityService :: rootInActiveWindow :: step 3");
                    if (messageField.getText () == null || messageField.getText ().length () == 0
                            || !messageField.getText ().toString ().endsWith (getApplicationContext ().getString (R.string.whatsapp_suffix))) { // So your service doesn't process any message, but the ones ending your apps suffix
                        return;
                    }

                    // Whatsapp send button id
                    List<AccessibilityNodeInfoCompat> sendMessageNodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId ("com.whatsapp:id/send");
                    Log.d("DBG_X", "WhatsappAccessibilityService :: rootInActiveWindow :: step 4");
                    if (sendMessageNodeInfoList == null || sendMessageNodeInfoList.isEmpty ()) {
                        return;
                    }

                    AccessibilityNodeInfoCompat sendMessageButton = sendMessageNodeInfoList.get (0);
                    Log.d("DBG_X", "WhatsappAccessibilityService :: rootInActiveWindow :: step 5");
                    Log.d("DBG_X", "WhatsappAccessibilityService :: messageField :: " + sendMessageButton.isVisibleToUser());
                    if (!sendMessageButton.isVisibleToUser ()) {
                        return;
                    }

                    // Now fire a click on the send button
                    Log.d("DBG_X", "WhatsappAccessibilityService :: rootInActiveWindow :: step 6");
                    sendMessageButton.performAction (AccessibilityNodeInfo.ACTION_CLICK);


                    // Now go back to your app by clicking on the Android back button twice:
                    // First one to leave the conversation screen
                    // Second one to leave whatsapp
                    try {
                        Thread.sleep (1000); // hack for certain devices in which the immediate back click is too fast to handle
                        performGlobalAction (GLOBAL_ACTION_BACK);
                        Log.d("DBG_X", "WhatsappAccessibilityService :: rootInActiveWindow :: step 7");
                    } catch (InterruptedException ignored) {}
                    //performGlobalAction (GLOBAL_ACTION_BACK);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    private void sendMessage(String phone, String message){
        if (phone.startsWith("0")){
            phone = phone.replaceFirst("0", "62");
        }

        try {
            PackageManager packageManager = this.getPackageManager();
            Intent i = new Intent(Intent.ACTION_VIEW);
            String url = "whatsapp://send?phone="+ phone +"&text=" + URLEncoder.encode(message, "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                Log.d("DBG_X", "WhatsappAccessibilityService :: sendMessage :: sendTo : " + phone);
                this.startActivity(i);
            }
        } catch (UnsupportedEncodingException e) {
            Log.d("DBG_X", "WhatsappAccessibilityService :: sendMessage :: errorCatched" + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {
        Log.d("DBG_6", "WhatsappAccessibilityService: onInterrupt");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.d("DBG_X", "WhatsappAccessibilityService :: onServiceConnected");
    }

}
