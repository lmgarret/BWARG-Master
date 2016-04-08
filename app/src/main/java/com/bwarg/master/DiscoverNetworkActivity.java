package com.bwarg.master;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bwarg.master.network.SSPListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import eu.hgross.blaubot.core.BlaubotDevice;


public class DiscoverNetworkActivity extends ActionBarActivity implements Observer{
    private final static String TAG = "DiscoverNetworkActivity";
    private static final int DISCOVER_PORT_DEF = 8888;
    public static boolean USE_NSD_MANAGER = false;

    private CustomArrayAdapter cAdapter;
    private ArrayList<ServerProfile> profilesLists = new ArrayList<>();

    private ListView mListView;
    public final static int REQUEST_SETTINGS_UDP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.title_settings_discover_udp));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_discover_udp);
        if (mListView == null) {
            mListView = (ListView) findViewById(R.id.udp_list);
        }

        cAdapter = new CustomArrayAdapter(this, profilesLists);
        mListView.setAdapter(cAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                Intent intent = new Intent();
                ServerProfile profSel = profilesLists.get((int) id);
                Gson gson = new Gson();
                intent.putExtra("stream_prefs", gson.toJson(profSel.toStreamPreferences()));

                setResult(RESULT_OK, intent);
                finish();
            }


        });
        MjpegActivity.BWARG_SERVER.onCreate(this);
        MjpegActivity.BWARG_SERVER.addSSPListener(new SSPListener() {
            @Override
            public void applySettings(SlaveStreamPreferences prefs, String uniqueID) {
                boolean containsProfile = false;
                for (int i = 0; i < profilesLists.size() && !containsProfile; i++) {
                    containsProfile = profilesLists.get(i).getUniqueID().equals(uniqueID);
                }
                if (!containsProfile)
                    addServerProfile(new ServerProfile(uniqueID, prefs.getName(), prefs.getIpAdress(), prefs.getIpPort()));
            }
        });
        MjpegActivity.BWARG_SERVER.addObserver(this);
    }
    @Override
    protected void onStop()  {
        super.onStop();
        /*if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            mNsdManager = null;
        }*/
        cAdapter.clear();
        MjpegActivity.BWARG_SERVER.onStop();
        super.onStop();
    }
    @Override
    protected void onPause() {
        /*if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            cAdapter.clear();
        }*/
        cAdapter.clear();
        MjpegActivity.BWARG_SERVER.onPause(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        MjpegActivity.BWARG_SERVER.onResume(this);
    }

    @Override
    protected void onDestroy() {
        MjpegActivity.BWARG_SERVER.deleteObserver(this);
        MjpegActivity.BWARG_SERVER.onDestroy();

        cAdapter.clear();
        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.d("BwargServer","must delete : "+ ((BlaubotDevice)data).getUniqueDeviceID());
        removeServerProfile(((BlaubotDevice) data).getUniqueDeviceID());
    }

    public class ServerProfile{
        private String device_name = StreamPreferences.UNKNOWN_NAME;
        private int[] ipTab = {0, 0, 0, 0, 8080};
        private String uniqueID="none";

        public ServerProfile(String uniqueID){
            this.uniqueID = uniqueID;
        }
        public ServerProfile(String uniqueID, String name, String ip, int port){
            this(uniqueID);
            setIpNoPort(ip);
            setPort(port);
            setDevice_name(name);
        }
        protected void setIpNoPort(String ip){
            int[] ipResult = new int[5];
            String temp = "";

            int ipIndex = 0;
            for (int i=0; i<ip.length(); i++){
                char c = ip.charAt(i);
                if(Character.isDigit(c)){
                    temp+=c;
                }else if( c == '.' || c== ':'){
                    ipResult[ipIndex] = Integer.parseInt(temp);
                    Log.d(TAG, temp+" parsed to "+Integer.parseInt(temp) +" stored in "+ipIndex);
                    ipIndex++;
                    temp = "";
                }
            }
            if(!temp.equals("")){
                ipResult[ipIndex] = Integer.parseInt(temp);
                Log.d(TAG, temp+" parsed to "+Integer.parseInt(temp) +" stored in "+ipIndex);
                ipIndex++;
                temp = "";
            }
            ipTab = ipResult;

            Log.d(TAG, "received "+ip +", computed "+getStringIP());
        }
        protected void setPort(int port){
            ipTab[4] = port;
        }

        public String getDevice_name() {
            return device_name;
        }

        public String getUniqueID() {
            return uniqueID;
        }

        public void setUniqueID(String uniqueID) {
            this.uniqueID = uniqueID;
        }

        public void setDevice_name(String device_name) {
            this.device_name = device_name;
        }
        protected int getPort(){
            return ipTab[4];
        }
        protected int[] getIP(){
            return ipTab;
        }
        protected String getStringIP(){
            String result = "";
            for (int i = 0; i< ipTab.length-1; i++){
                result+=Integer.toString(ipTab[i])+'.';
            }
            return result.substring(0, result.length()-1)+":"+ipTab[ipTab.length-1  ];
        }
        protected StreamPreferences toStreamPreferences(){
            StreamPreferences temp = new StreamPreferences();
            temp.setIp_ad1(ipTab[0]);
            temp.setIp_ad2(ipTab[1]);
            temp.setIp_ad3(ipTab[2]);
            temp.setIp_ad4(ipTab[3]);
            temp.setIp_port(ipTab[4]);
            temp.setName(device_name);
            return temp;
        }
    }
    public void addServerProfile(ServerProfile servProfile){
        final ServerProfile pf = servProfile;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!profilesLists.contains(pf))
                    cAdapter.add(pf);
                return;
            }
        });
    }
    public void removeServerProfile(final ServerProfile serverProfile){
        final ServerProfile pf = serverProfile;
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = profilesLists.indexOf(pf);
                if(index != -1)
                    cAdapter.remove(serverProfile);
                return;
            }
        });
    }
    public void removeServerProfile(final String uniqueID){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int index = -1;
                int i= 0;
                for(ServerProfile sp : profilesLists){
                    if(sp.getUniqueID().equals(uniqueID)) {
                        index = i;
                        break;
                    }
                    i++;
                }
                if(index != -1)
                    cAdapter.remove(profilesLists.get(index));
                return;
            }
        });
    }

        public void clearServerProfiles(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cAdapter.clear();
                return;
            }
        });
    }
    public class CustomArrayAdapter extends ArrayAdapter<ServerProfile> {
        private final Context context;
        private final ArrayList<ServerProfile> values;

        public CustomArrayAdapter(Context context, ArrayList<ServerProfile> values){
            super(context, R.layout.rowlayout, values);
            this.context = context;
            this.values = values;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.rowlayout, parent, false);
            TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
            textView.setText(values.get(position).getDevice_name());
            TextView descriptionView = (TextView) rowView.findViewById(R.id.secondLine);
            descriptionView.setText(values.get(position).getStringIP());

            return rowView;
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}