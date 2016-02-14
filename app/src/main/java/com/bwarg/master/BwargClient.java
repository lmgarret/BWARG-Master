package com.bwarg.master;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;

/**
 * Created by LM on 13.02.2016.
 */
public class BwargClient extends AsyncTask<Context, String, String> {
    public final static int PORT = 8888;
    private DatagramSocket c;
    private CallbackReceiver receiver = null;
    private HashMap<String, ServerProfile> serverMap = new HashMap<>();
    private final static String TAG = "BwargClient";
    private final static int REQUEST_SETTINGS_UDP = 0;
    private Context context;

    private NsdManager.RegistrationListener mRegistrationListener;
    public static String NSD_SERVICE_NAME = "BWARG";
    private ServerSocket mServerSocket;
    private int mLocalPort;
    private NsdManager mNsdManager;
    private NsdManager.DiscoveryListener mDiscoveryListener;

    public BwargClient(Context context){
        this.context = context;
        // Find the server using UDP broadcast
        try {
            //Open a random port to send the package
            c = new DatagramSocket();
            c.setBroadcast(true);

            byte[] sendData = context.getResources().getString(R.string.discover_request).getBytes();


            InetAddress broadcast_ip = getAdressBroadcast();

            try {
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, getAdressBroadcast(), PORT);
                c.send(sendPacket);
            } catch (Exception e) {
            }
            Log.d(TAG, "Request packet sent to: " + broadcast_ip);



        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // Initialize a server socket on the next available port.
        try {
            mServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Store the chosen port.
        mLocalPort =  mServerSocket.getLocalPort();

    }

    @Override
    protected String doInBackground(Context... params) {
        Context context = params[0];
        while (c != null && !c.isClosed()){
            //Wait for a response
            byte[] recvBuf = new byte[15000];
            DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
            try {
                c.receive(receivePacket);
                String src_ip = receivePacket.getAddress().getHostAddress();

                //We have a response
                Log.d(TAG,"Packet received from " + src_ip);

                //Check if the message is correct
                String message = new String(receivePacket.getData()).trim();

                boolean message_received = false;
                if (message.startsWith(context.getResources().getString(R.string.discover_response))) {
                    //DO SOMETHING WITH THE SERVER'S IP (for example, store it in your controller)
                    ServerProfile serverProfile = new ServerProfile();
                    serverProfile.setIpNoPort(src_ip);
                    serverMap.put(src_ip, serverProfile);
                    message_received = true;
                    Log.d(TAG,"Discover response from " + src_ip);

                }else if(message.startsWith(context.getResources().getString(R.string.name_response))){
                    ServerProfile profile = serverMap.get(src_ip);
                    profile.device_name = message.substring(context.getResources().getString(R.string.name_response).length()-1);
                    serverMap.put(src_ip, profile);
                    message_received = true;
                    Log.d(TAG,"Name response from " + src_ip+ " : "+profile.device_name);

                }else if(message.startsWith(context.getResources().getString(R.string.port_response))){
                    ServerProfile profile = serverMap.get(src_ip);
                    profile.setPort(Integer.parseInt(message.substring(context.getResources().getString(R.string.port_response).length() - 1)));
                    serverMap.put(src_ip, profile);
                    Log.d(TAG, "Port response from " + src_ip + " : " + profile.getPort());

                    receiver.onReceiveData(profile);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return "";
    }

    @Override
    protected void onPostExecute(String result) {
        c.close();
    }
    public void setOnDataReceivedListener(CallbackReceiver receiver){
        this.receiver = receiver;
    }

    public class ServerProfile{
        private String device_name = "None";
        private int[] ipTab = {0, 0, 0, 0, 8080};

        protected void setIpNoPort(String ip){
            int[] ipResult = new int[5];
            String temp = "";

            int ipIndex = 0;
            for (int i=0; i<ip.length(); i++){
                char c = ip.charAt(i);
                if( c != '.' && c!= ':'){
                    temp+=c;
                }else{
                    ipResult[ipIndex] = Integer.parseInt(temp);
                    ipIndex++;
                    temp = "";
                }
            }
            ipTab = ipResult;
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
    }

    private InetAddress getAdressBroadcast() throws UnknownHostException {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                // Save the service name.  Android may have changed it in order to
                // resolve a conflict, so update the name you initially requested
                // with the name Android actually used.
                NSD_SERVICE_NAME = NsdServiceInfo.getServiceName();
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Registration failed!  Put debugging code here to determine why.

            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                // Service has been unregistered.  This only happens when you call
                // NsdManager.unregisterService() and pass in this listener.
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
                // Unregistration failed.  Put debugging code here to determine why.
            }
        };
    }
    public void registerService() {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(NSD_SERVICE_NAME);
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(mLocalPort);

        mNsdManager = (NsdManager)context.getSystemService(Context.NSD_SERVICE);

        mNsdManager.registerService(
                serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
    }
    /*public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            //  Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found!  Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + mServiceName);
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
    }*/
}
