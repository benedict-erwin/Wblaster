package com.example.wblaster;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wblaster.Utils.ContactUtils;
import com.example.wblaster.Utils.WhatsappAccessibilityService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private Button btnCheck;
    private EditText txPhone;
    private ContactUtils contactClass;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dashboard);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        // btnCheck click
        btnCheck = findViewById(R.id.btCheck);
        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkContact();
            }
        });

        // Test JSON
        String jsonString = "{ \"broadcast\": [ { \"name\": \"Erwin\", \"phone\": \"085749518656\", \"msg_id\": \"1\", \"msg_content\": \"This is a whatsapp message Send By WBLASTER\" }, { \"name\": \"Pranata\", \"phone\": \"085749518656\", \"msg_id\": \"2\", \"msg_content\": \"This is a whatsapp message Send By WBLASTER\" } ] }";
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
                    Log.d("DBG_X", "MainActivity :: TESTjson :: data >> Name : " + name + ", Phone : " + phone + ", msgId : " + msgId + ", message : '" + message +"'");
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // Check If Contact Exists
    private void checkContact(){
        txPhone = findViewById(R.id.txPhone);
        String name = "Erwin";
        String email = "cyb3rwin@gmail.com";
        String phoneNumber = txPhone.getText().toString();
        String waNumber;
        String message = "This is a whatsapp message Send By WBLASTER";

        Log.d("DBG_1", "checkContact: " + phoneNumber);
        contactClass = new ContactUtils();
        if(contactClass.contactExists(this, phoneNumber)){
            Toast.makeText(MainActivity.this, "Exists " + phoneNumber, Toast.LENGTH_SHORT).show();
        }else {
            contactClass.saveContact(this, name, email, phoneNumber);
            Toast.makeText(MainActivity.this, "New Contact Added!", Toast.LENGTH_SHORT).show();
        }

        if (phoneNumber.startsWith("0")){
            waNumber = phoneNumber.replaceFirst("0", "62");
        }else {
            waNumber = phoneNumber;
        }
        this.sendWhatsAppMessage(waNumber, message);
    }

    private void sendWhatsAppMessage(String phone, String message){
        PackageManager packageManager = this.getPackageManager();//        Intent i = new Intent(Intent.ACTION_VIEW);

        try {
//            if (i.resolveActivity(packageManager) != null) {
//                this.startActivity(i);
//            }
            Log.d("DBG_7", "isAccessibilityOn: check");
            if (!isAccessibilityOn(this, WhatsappAccessibilityService.class)) {
                Intent intent = new Intent (Settings.ACTION_ACCESSIBILITY_SETTINGS);
                this.startActivity(intent);
            }else {
                Intent i = new Intent(Intent.ACTION_VIEW);
                String url = "https://api.whatsapp.com/send?phone="+ phone +"&text=" + URLEncoder.encode(message, "UTF-8");
                i.setPackage("com.whatsapp");
                i.setData(Uri.parse(url));
                if (i.resolveActivity(packageManager) != null) {
                    this.startActivity(i);
                }
            }


        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean isAccessibilityOn (Context context, Class<? extends AccessibilityService> clazz) {
        String TAG = "DBG_X";
        int accessibilityEnabled = 0;
        final String service = context.getPackageName () + "/" + clazz.getCanonicalName ();
        Log.d(TAG, "-------------- > Service :: " + service);
        try {
            accessibilityEnabled = Settings.Secure.getInt (context.getApplicationContext ().getContentResolver (), Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.d(TAG, "-------------- > accessibilityEnabled :: " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException ignored) {  }

        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter (':');

        if (accessibilityEnabled == 1) {
            Log.d(TAG, "-------------- > accessibilityEnabled :: ***IS ENABLED***");
            String settingValue = Settings.Secure.getString (context.getApplicationContext ().getContentResolver (), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.d(TAG, "-------------- > settingValue :: " + settingValue);
            if (settingValue != null) {
                Log.d(TAG, "-------------- > settingValue :: NOT NULL");
                colonSplitter.setString (settingValue);
                while (colonSplitter.hasNext()) {
                    String accessibilityService = colonSplitter.next();
                    Log.d(TAG, "-------------- > accessibilityServiceEqual 1 :: " + accessibilityService);
                    Log.d(TAG, "-------------- > accessibilityServiceEqual 2 :: " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.d(TAG, "-------------- > isTrue :: *** TRUE ***");
                        return true;
                    }
                }
            }
        }
        Log.d(TAG, "-------------- > isTrue :: *** FALSE ***");
        return false;
    }


    public static boolean isAccessibilitySettingsOn(Context mContext, Class<? extends AccessibilityService> clazz) {
        String TAG = "DBG_X";
        int accessibilityEnabled = 0;
        //your package /   accesibility service path/class
        final String service = mContext.getPackageName () + "/" + clazz.getCanonicalName ();
        Log.v(TAG, "-------------- > Service :: " + service);
        boolean accessibilityFound = false;
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILIY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            Log.v(TAG, "-------------- > settingValue :: " + settingValue);
            if (settingValue != null) {
                TextUtils.SimpleStringSplitter splitter = mStringColonSplitter;
                splitter.setString(settingValue);
                Log.v(TAG, "-------------- > splitter :: " + splitter.toString());
                while (splitter.hasNext()) {
                    String accessabilityService = splitter.next();

                    Log.v(TAG, "-------------- > accessabilityService :: " + accessabilityService);
                    if (accessabilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILIY IS DISABLED***");
        }

        Log.v(TAG, "not return true");
        return accessibilityFound;
    }

}
