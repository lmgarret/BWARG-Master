package com.bwarg.master.network;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import com.bwarg.master.MjpegActivity;
import com.bwarg.master.SlaveStreamPreferences;

import org.apache.http.conn.util.InetAddressUtils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

import eu.hgross.blaubot.android.BlaubotAndroid;
import eu.hgross.blaubot.android.BlaubotAndroidFactory;
import eu.hgross.blaubot.core.BlaubotDevice;
import eu.hgross.blaubot.core.IBlaubotAdapter;
import eu.hgross.blaubot.core.IBlaubotDevice;
import eu.hgross.blaubot.core.ILifecycleListener;
import eu.hgross.blaubot.core.acceptor.discovery.IBlaubotBeacon;
import eu.hgross.blaubot.ethernet.BlaubotBonjourBeacon;
import eu.hgross.blaubot.ethernet.BlaubotEthernetAdapter;
import eu.hgross.blaubot.messaging.BlaubotMessage;
import eu.hgross.blaubot.messaging.IBlaubotChannel;
import eu.hgross.blaubot.messaging.IBlaubotMessageListener;

/**
 * Created by LM on 07.04.2016.
 */
public class BwargServer extends Observable {
    private static final String TAG = "BwargServer";

    private static final String APP_UUID_STRING = "52260110-f8f0-11e5-a837-0800200c9a66";
    private static final int APP_PORT = 5606;
    private BlaubotAndroid blaubot;
    private IBlaubotChannel mainChannel;

    private HashMap<String, SlaveStreamPreferences> connectedDevices = new HashMap<>();
    private ArrayList<SSPListener> sspListeners = new ArrayList<>();

    public BwargServer(){
        final UUID APP_UUID = UUID.fromString(APP_UUID_STRING);
        IBlaubotBeacon beacon = new BlaubotBonjourBeacon(getIPv4InetAddress(), APP_PORT);
        IBlaubotAdapter adapter = new BlaubotEthernetAdapter(new BlaubotDevice(),APP_PORT+1,getIPv4InetAddress());
        blaubot = BlaubotAndroidFactory.createBlaubot(APP_UUID, new BlaubotDevice(), adapter, beacon);
    }

    /**Should be called only once, in the very first onCreate
     *
     * @param context
     */
    public void init(Context context, SSPListener sspListener){
        addSSPListener(sspListener);
        blaubot.startBlaubot();
        blaubot.registerReceivers(context);
        blaubot.setContext(context);
        Log.i(TAG, "Device ID : " + blaubot.getOwnDevice().getUniqueDeviceID());
        mainChannel = blaubot.createChannel((short)1);
        mainChannel.publish("Channel started".getBytes());
        mainChannel.subscribe(new IBlaubotMessageListener() {
            @Override
            public void onMessage(BlaubotMessage blaubotMessage) {
                // we got a message - our payload is a byte array
                // deserialize
                String msg = new String(blaubotMessage.getPayload());
                //Structure of messages : HEADER_[fromID]XXX_[toID]XXX_DATA
                Message message = null;
                try {
                    message = Message.parseMessage(msg);
                    Log.d(TAG, "Valid message : \"" + msg + "\"");
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    Log.d(TAG, "Not a valid message : \"" + msg + "\"");
                }
                if (message != null) {
                    switch (message.getHeader().toString()) {
                        case "SSP": //manage complete settings rewrite here
                            if (receivedToSelf(message) || receivedToAll(message)) {
                                SlaveStreamPreferences receivedPrefs = SlaveStreamPreferences.fromGson(message.getData());
                                for(SSPListener sspListener : sspListeners){
                                    sspListener.applySettings(receivedPrefs, message.getFromID());
                                }
                                connectedDevices.put(message.getFromID(), receivedPrefs);
                                //idTuples.put(, message.getFromID());
                            }
                            break;
                        case "IPA":
                            if(receivedToAll(message)){
                                //We know it's a broadcast
                            }
                                default:
                            Log.d(TAG, "Invalid header : \"" + message.getHeader() + "\"");
                            break;
                    }
                }
            }
        });
        blaubot.addLifecycleListener(new ILifecycleListener() {
            @Override
            public void onDisconnected() {
                // THIS device disconnected from the network
                Log.i(TAG, "Disconnected.");
            }

            @Override
            public void onDeviceLeft(IBlaubotDevice blaubotDevice) {
                // ANOTHER device disconnected from the network
                Log.d(TAG, blaubotDevice.getUniqueDeviceID() + " disconnected");
                setChanged();
                notifyObservers(blaubotDevice);
                connectedDevices.remove(blaubotDevice.getUniqueDeviceID());
            }

            @Override
            public void onDeviceJoined(IBlaubotDevice blaubotDevice) {
                Log.d(TAG, blaubotDevice.getUniqueDeviceID()+ " connected");
                // ANOTHER device connected to the network THIS device is on
            }

            @Override
            public void onConnected() {
                // THIS device connected to a network
                // you can now subscribe to channels and use them:
                //mainChannel.subscribe();
                Log.d(TAG, "Connected.");
                // onDeviceJoined(...) calls will follow for each OTHER device that was already connected
            }

            @Override
            public void onPrinceDeviceChanged(IBlaubotDevice oldPrince, IBlaubotDevice newPrince) {
                // if the network's king goes down, the prince will rule over the remaining peasants
            }

            @Override
            public void onKingDeviceChanged(IBlaubotDevice oldKing, IBlaubotDevice newKing) {

            }
        });
    }
    public void sendAEL(boolean auto_exposure_locked){
        Message.Broadcast message = new Message.Broadcast(Header.AEL, blaubot.getOwnDevice(), String.valueOf(auto_exposure_locked));
        mainChannel.publish((message.toString()).getBytes());
    }
    public void sendSSPTo(String uuid,SlaveStreamPreferences prefs){
        Message message = new Message(Header.SSP, blaubot.getOwnDevice().getUniqueDeviceID(),uuid, prefs.toGson());
        mainChannel.publish((message.toString()).getBytes());
    }
    private boolean receivedFromSelf(Message message){
        return  message.getFromID().equals(blaubot.getOwnDevice().getUniqueDeviceID());
    }
    private boolean receivedToSelf(Message message){
        return  message.getToID().equals(blaubot.getOwnDevice().getUniqueDeviceID());
    }
    private boolean receivedToSelfIP(Message message){
        return  message.getToID().equals(getIPv4Address());
    }
    private boolean receivedToAll(Message message){
        return  message.getToID().equals(Message.ALL_ID);
    }
    public void addSSPListener(SSPListener ssp_listener){
        if(!sspListeners.contains(ssp_listener))
            this.sspListeners.add(ssp_listener);
    }
    public void onCreate(Context context){
        blaubot.startBlaubot();
        blaubot.registerReceivers(context);
        blaubot.setContext(context);
    }
    public void onResume(Context context){
        blaubot.startBlaubot();
        blaubot.registerReceivers(context);
        blaubot.setContext(context);
    }
    public void onPause(Activity activity){
        blaubot.unregisterReceivers(activity);
        blaubot.onPause(activity);
    }
    public void onStop(){
        blaubot.stopBlaubot();
    }
    public void  onDestroy(){
        blaubot.stopBlaubot();
    }
    private static String getIPv4Address()
    {
        try
        {
            final Enumeration<NetworkInterface> en =
                    NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements())
            {
                final NetworkInterface intf = en.nextElement();
                final Enumeration<InetAddress> enumIpAddr =
                        intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements())
                {
                    final  InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        final String addr = inetAddress.getHostAddress().toUpperCase();
                        if (InetAddressUtils.isIPv4Address(addr))
                        {
                            return addr;
                        }
                    } // if
                } // while
            } // for
        } // try
        catch (final Exception e)
        {
            // Ignore
        } // catch
        return null;
    } // getIpV4Address()
    private static InetAddress getIPv4InetAddress()
    {
        try
        {
            final Enumeration<NetworkInterface> en =
                    NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements())
            {
                final NetworkInterface intf = en.nextElement();
                final Enumeration<InetAddress> enumIpAddr =
                        intf.getInetAddresses();
                while (enumIpAddr.hasMoreElements())
                {
                    final  InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress())
                    {
                        final String addr = inetAddress.getHostAddress().toUpperCase();
                        if (InetAddressUtils.isIPv4Address(addr))
                        {
                            return inetAddress;
                        }
                    } // if
                } // while
            } // for
        } // try
        catch (final Exception e)
        {
            // Ignore
        } // catch
        return null;
    } // getIPv4InetAddress()
}
