package com.bwarg.master;

import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.net.InetAddress;
import java.util.ArrayList;

public class DiscoverUDPActivity extends ActionBarActivity implements CallbackReceiver{
    private final static String TAG = "DiscoverUDPActivity";
    private static final int DISCOVER_PORT_DEF = 8888;

    // private BwargClient client;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listItems=new ArrayList<String>();
    private ArrayList<ServerProfile> profilesLists = new ArrayList<>();
    private ListView mListView;
    public final static int REQUEST_SETTINGS_UDP = 1;

    private String SERVICE_NAME_MASTER = "BWARG Master";
    private String SERVICE_NAME_SLAVE = "BWARG Slave";

    private String SERVICE_TYPE = "_http._tcp.";
    private NsdManager mNsdManager;
    private InetAddress hostAddress;
    private int hostPort;
    private NsdServiceInfo mService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getResources().getString(R.string.title_settings_discover_udp));
        setContentView(R.layout.activity_discover_udp);
        //client  = new BwargClient(this);
       //client.setOnDataReceivedListener(this);

        if (mListView == null) {
            mListView = (ListView) findViewById(R.id.udp_list);
        }

        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {


            @Override
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                Intent intent = new Intent();
                ServerProfile profSel = profilesLists.get((int) id);
                intent.putExtra("ip_ad1", profSel.getIP()[0]);
                intent.putExtra("ip_ad2", profSel.getIP()[1]);
                intent.putExtra("ip_ad3", profSel.getIP()[2]);
                intent.putExtra("ip_ad4", profSel.getIP()[3]);
                intent.putExtra("ip_port", profSel.getIP()[4]);

                setResult(RESULT_OK, intent);
                finish();
            }


        });
        //client.execute(this);
        // NSD Stuff
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        mNsdManager.discoverServices(SERVICE_TYPE,
                NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    @Override
    public void onReceiveData(BwargClient.ServerProfile servProfile) {
        adapter.add(servProfile.getDevice_name());
        //profilesLists.add(servProfile);
    }
    @Override
    protected void onPause() {
        if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }

        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdManager != null) {
            mNsdManager.discoverServices(
                    SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }

    }

    @Override
    protected void onDestroy() {
        if (mNsdManager != null) {
            mNsdManager.stopServiceDiscovery(mDiscoveryListener);
        }
        super.onDestroy();
    }


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
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }
    };

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
            }else if(serviceInfo.getServiceName().equals(SERVICE_NAME_SLAVE) ||serviceInfo.getServiceName().equals(SERVICE_NAME_SLAVE.replace(" ", "\\032"))){
                //SLAVE found!
                mService = serviceInfo;
                int port = mService.getPort();
                InetAddress host = mService.getHost();

                ServerProfile servProfile = new ServerProfile();
                servProfile.setIpNoPort(host.toString());
                servProfile.setPort(port);

                // Obtain port and IP
                hostPort = serviceInfo.getPort();
                hostAddress = serviceInfo.getHost();

                addServerProfile(servProfile);
            }


        }
    };

    public class ServerProfile{
        private String device_name = "None";
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
    }
    public void addServerProfile(ServerProfile servProfile){
        //adapter.add(servProfile.getDevice_name());
        adapter.add(servProfile.getStringIP());
        profilesLists.add(servProfile);
    }
}
