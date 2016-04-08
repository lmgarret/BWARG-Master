package com.bwarg.master;

import android.util.Log;

import com.google.gson.Gson;

import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Created by LM on 10.02.2016.
 */
public class StreamPreferences {
    public final static String TAG = "StreamPreferences";
    public final static String UNKNOWN_NAME = "(Unknown)";

    //Images setting attributes
    private int width = 640;
    private int height = 480;

    private ArrayList<Resolution> supportedResolutions = new ArrayList<>();
    private boolean useFlashLight = false;
    private int quality = 40;
    private boolean auto_exposure_lock = false;
    private boolean auto_exposure_lock_supported = false;
    private boolean auto_white_balance_lock = false;
    private boolean auto_white_balance_lock_supported = false;
    private String white_balance = "auto";
    private String focus_mode = "auto";
    private boolean image_stabilization = false;
    private boolean image_stabilization_supported = false;
    private String iso = "auto";
    private int fast_fps_mode = 0;
    private boolean fast_fps_mode_supported = false;
    private int sizeIndex = 0;

    //Network settings attributes
    private String device_unique_id = "None";
    private int ip_ad1 = 192;
    private int ip_ad2 = 168;
    private int ip_ad3 = 2;
    private int ip_ad4 = 1;
    private int ip_port = 8080;
    private String URL = "";
    private String name = UNKNOWN_NAME;
    private String command ="";
    private boolean no_port = false;

    public StreamPreferences(){

    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getIp_ad1() {
        return ip_ad1;
    }

    public int getIp_ad2() {
        return ip_ad2;
    }

    public int getIp_ad3() {
        return ip_ad3;
    }

    public int getIp_ad4() {
        return ip_ad4;
    }

    public int getIp_port() {
        return ip_port;
    }

    public String getURL() {
        StringBuilder sb = new StringBuilder();
        String s_http = "http://";
        String s_dot = ".";
        String s_colon = ":";
        String s_slash = "/";
        sb.append(s_http);
        sb.append(getIP(true));
        sb.append(s_slash);
        URL = new String(sb);
        return URL;
    }
    private String getIP(boolean withPort) {
        StringBuilder sb = new StringBuilder();
        String s_dot = ".";
        String s_colon = ":";
        sb.append(ip_ad1);
        sb.append(s_dot);
        sb.append(ip_ad2);
        sb.append(s_dot);
        sb.append(ip_ad3);
        sb.append(s_dot);
        sb.append(ip_ad4);
        if(!hasNo_port()&&withPort){
            sb.append(s_colon);
            sb.append(ip_port);
        }
        URL = new String(sb);
        return URL;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setIp_ad1(int ip_ad1) {
        this.ip_ad1 = ip_ad1;
    }

    public void setIp_ad2(int ip_ad2) {
        this.ip_ad2 = ip_ad2;
    }

    public void setIp_ad3(int ip_ad3) {
        this.ip_ad3 = ip_ad3;
    }

    public void setIp_ad4(int ip_ad4) {
        this.ip_ad4 = ip_ad4;
    }

    public void setIp_port(int ip_port) {
        this.ip_port = ip_port;
    }

    public void setIpNoPort(String ip){
        int[] ipResult = new int[5];
        String temp = "";

        int ipIndex = 0;
        for (int i=0; i<ip.length(); i++){
            char c = ip.charAt(i);
            if(Character.isDigit(c)){
                temp+=c;
            }else if( c == '.' || c== ':'){
                ipResult[ipIndex] = Integer.parseInt(temp);
                ipIndex++;
                temp = "";
            }
        }
        if(!temp.equals("")){
            ipResult[ipIndex] = Integer.parseInt(temp);
            ipIndex++;
            temp = "";
        }
        ip_ad1 = ipResult[0];
        ip_ad2 = ipResult[1];
        ip_ad3 = ipResult[2];
        ip_ad4 = ipResult[3];
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public boolean hasNo_port() {
        return no_port;
    }

    public void setNo_port(boolean no_port) {
        this.no_port = no_port;
    }

    public boolean isAutoExposureLockSupported() {
        return auto_exposure_lock_supported;
    }

    public void setAutoExposureLockSupported(boolean auto_exposure_lock_supported) {
        this.auto_exposure_lock_supported = auto_exposure_lock_supported;
    }

    public boolean isAutoWhiteBalanceLockSupported() {
        return auto_white_balance_lock_supported;
    }

    public void setAutoWhiteBalanceLockSupported(boolean auto_white_balance_lock_supported) {
        this.auto_white_balance_lock_supported = auto_white_balance_lock_supported;
    }

    public boolean isImageStabilizationSupported() {
        return image_stabilization_supported;
    }

    public void setImageStabilizationSupported(boolean image_stabilization_supported) {
        this.image_stabilization_supported = image_stabilization_supported;
    }

    public boolean isFastFpsModeSupported() {
        return fast_fps_mode_supported;
    }

    public void setFastFpsModeSupported(boolean fast_fps_mode_supported) {
        this.fast_fps_mode_supported = fast_fps_mode_supported;
    }
    public static String defaultGsonString(){
        Gson gson = new Gson();
        return gson.toJson(new StreamPreferences());
    }

    public class Resolution{
        private int index=0;
        private String resolution = "unknown";

        Resolution(int index, String resolution){
            this.index = index;
            this.resolution = resolution;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public String getResolution() {
            return resolution;
        }

        public void setResolution(String resolution) {
            this.resolution = resolution;
        }

    }

    public int getSizeIndex() {
        return sizeIndex;
    }

    public void setSizeIndex(int sizeIndex) {
        this.sizeIndex = sizeIndex;
    }

    public String getDeviceUniqueId() {
        return device_unique_id;
    }

    public void setDeviceUniqueId(String device_unique_id) {
        this.device_unique_id = device_unique_id;
    }

    public void copyFrom(SlaveStreamPreferences streamPreferences){
        this.auto_exposure_lock = streamPreferences.getAutoExposureLock();
        this.auto_exposure_lock_supported = streamPreferences.isAutoExposureLockSupported();
        this.auto_white_balance_lock = streamPreferences.getAutoWhiteBalanceLock();
        this.auto_white_balance_lock_supported = streamPreferences.isAutoWhiteBalanceLockSupported();
        this.fast_fps_mode = streamPreferences.getFastFpsMode();
        this.fast_fps_mode_supported = streamPreferences.isFastFpsModeSupported();
        this.focus_mode = streamPreferences.getFocusMode();
        this.image_stabilization = streamPreferences.getImageStabilization();
        this.image_stabilization_supported = streamPreferences.isImageStabilizationSupported();
        this.iso = streamPreferences.getIso();
        this.name = streamPreferences .getName();
        this.quality = streamPreferences.getQuality();
        this.useFlashLight = streamPreferences.useFlashLight();
        this.white_balance = streamPreferences.getWhiteBalance();
        this.sizeIndex = streamPreferences.getSizeIndex();
        ArrayList<Resolution> resolutions_supported = new ArrayList<>();
        for(int i = 0; i< streamPreferences.getResolutionsSupported().size(); i++){
            resolutions_supported.add(new Resolution(i, streamPreferences.getResolutionsSupported().get(i)));
        }
        this.supportedResolutions = resolutions_supported;
        this.setIpNoPort(streamPreferences.getIpAdress());
    }
    public SlaveStreamPreferences toSSP(){
        SlaveStreamPreferences ssp = new SlaveStreamPreferences();
        ssp.setAutoExposureLock(this.auto_exposure_lock);
        ssp.setAutoExposureLockSupported(this.auto_exposure_lock_supported);
        ssp.setAutoWhiteBalanceLock(this.auto_white_balance_lock);
        ssp.setAutoWhiteBalanceLockSupported(this.auto_white_balance_lock_supported);
        ssp.setFastFpsMode(this.fast_fps_mode);
        ssp.setFastFpsModeSupported(this.fast_fps_mode_supported);
        ssp.setFocusMode(this.focus_mode);
        ssp.setImageStabilization(this.image_stabilization);
        ssp.setImageStabilizationSupported(this.isImageStabilizationSupported());
        ssp.setIso(this.iso);
        ssp.setName(this.name);
        ssp.setQuality(this.quality);
        ssp.setUseFlashLight(this.useFlashLight);
        ssp.setWhiteBalance(this.white_balance);
        ssp.setSizeIndex(this.sizeIndex);
        ArrayList<String> resolutions_supported = new ArrayList<>();
        for(int i = 0; i< this.supportedResolutions.size(); i++){
            resolutions_supported.add(supportedResolutions.get(i).getResolution());
        }
        ssp.setResolutionsSupported(resolutions_supported);
        ssp.setIpAdress(this.getIP(false));
        ssp.setIpPort(this.getIp_port());
        return ssp;
    }
}
