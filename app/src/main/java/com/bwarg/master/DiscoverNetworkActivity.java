package com.bwarg.master;

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

import com.google.gson.Gson;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

public class DiscoverNetworkActivity extends ActionBarActivity{
    private final static String TAG = "DiscoverNetworkActivity";
    private static final int DISCOVER_PORT_DEF = 8888;
    public static boolean USE_NSD_MANAGER = false;

    private CustomArrayAdapter cAdapter;
    private ArrayList<ServerProfile> profilesLists = new ArrayList<>();

    private ListView mListView;
    public final static int REQUEST_SETTINGS_UDP = 1;

    public final static String SERVICE_NAME_MASTER = "Master BWARG";
    public final static String SERVICE_NAME = "BWARG";
    public final static String SERVICE_TYPE = "_http._tcp.";

    private NetworkClientTask clientTask;



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
        //client.execute(this);
        // NSD Stuff

        /*if(discoveryTask!=null)
            discoveryTask.closeNetworkDiscovery();
        discoveryTask = new Task();
        discoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");*/
        if(clientTask == null && USE_NSD_MANAGER)
            clientTask = new NetworkClientTask();
        clientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
    }
    @Override
    protected void onStop()  {
        super.onStop();
        /*if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            mNsdManager = null;
        }*/
        cAdapter.clear();
        if(clientTask!= null){
            clientTask.close();
            clientTask = null;
        }
       /* if(discoveryTask!=null)
            discoveryTask.closeNetworkDiscovery();*/
        super.onStop();
    }
    @Override
    protected void onPause() {
        /*if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            cAdapter.clear();
        }*/
        cAdapter.clear();
        if(clientTask!= null){
            clientTask.close();
            clientTask = null;
        }
        /*if(discoveryTask!=null)
            discoveryTask.closeNetworkDiscovery();*/
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if (mNsdManager != null) {
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }*/
        if(clientTask == null && USE_NSD_MANAGER) {
            clientTask = new NetworkClientTask();
            clientTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
        }
        /*discoveryTask = new Task();
        discoveryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");*/
    }

    @Override
    protected void onDestroy() {
        /*if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            mNsdManager = null;
        }*/
        if(clientTask!= null){
            clientTask.close();
            clientTask = null;
        }
        cAdapter.clear();
       /* if(discoveryTask!=null)
            discoveryTask.closeNetworkDiscovery();*/
        super.onDestroy();
    }

    public class ServerProfile{
        private String device_name = StreamPreferences.UNKNOWN_NAME;
        private int[] ipTab = {0, 0, 0, 0, 8080};

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
    protected class NetworkClientTask extends AsyncTask<String, Void, Void>{
        private NsdManager mNsdManager;
        private NsdServiceInfo mService;
        NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Called when the resolve fails. Use the error code to debug.
                Log.e(TAG, "Resolve failed " + errorCode);
                Log.e(TAG, "service = " + serviceInfo);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.d(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(SERVICE_NAME_MASTER)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }else if(serviceInfo.getServiceName().startsWith(SERVICE_NAME)){
                    //SLAVE found!
                    mService = serviceInfo;
                    int port = mService.getPort();
                    InetAddress host = mService.getHost();
                    String name = serviceInfo.getServiceName().substring(SERVICE_NAME.length()).replace("\\032", " ");

                    ServerProfile servProfile = new ServerProfile();
                    servProfile.setIpNoPort(host.toString());
                    servProfile.setPort(port);
                    servProfile.setDevice_name(name);


                    addServerProfile(servProfile);
                }


            }
        };
        NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success : " + service);
                Log.d(TAG, "Host = "+ service.getServiceName());
                Log.d(TAG, "port = " + String.valueOf(service.getPort()));

                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(SERVICE_NAME_MASTER)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + SERVICE_NAME_MASTER);
                } else {
                    Log.d(TAG, "Diff Machine : " + service.getServiceName());
                    // connect to the service and obtain serviceInfo
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost" + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
                clearServerProfiles();
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
                cAdapter.clear();
            }
        };


        @Override
        protected Void doInBackground(String... params) {
            if(mNsdManager ==null) {
                mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
            }
            while(!isCancelled()){
                mNsdManager.discoverServices(SERVICE_TYPE,
                        NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result){
            close();
        }
        protected void close(){
            cancel(true);
            //mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            mNsdManager = null;
        }
    }
}