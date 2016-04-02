package com.bwarg.master;

import com.google.gson.Gson;

import java.util.ArrayList;

/**
 * Created by LM on 10.02.2016.
 */
public class SlaveStreamPreferences {
    public final static String UNKNOWN_NAME = "(Unknown)";
    public final static  int SIDE_LEFT = 0;
    public final static int SIDE_RIGHT = 1;

    private String ip_adress = "127.0.0.1";

    private int ip_port = 8080;
    private String name = UNKNOWN_NAME;
    private int camIndex =0;
    private int sizeIndex = 0;
    private ArrayList<String> resolutions_supported = new ArrayList<>();
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

    private int preferred_side = SIDE_LEFT;

    public int getIpPort() {
        return ip_port;
    }

    public void setIpPort(int ip_port) {
        this.ip_port = ip_port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCamIndex() {
        return camIndex;
    }

    public void setCamIndex(int camIndex) {
        this.camIndex = camIndex;
    }

    public boolean useFlashLight() {
        return useFlashLight;
    }

    public void setUseFlashLight(boolean useFlashLight) {
        this.useFlashLight = useFlashLight;
    }

    public int getQuality() {
        return quality;
    }

    public void setQuality(int quality) {
        this.quality = quality;
    }

    public boolean getAutoExposureLock() {
        return auto_exposure_lock;
    }

    public void setAutoExposureLock(boolean auto_exposure_lock) {
        this.auto_exposure_lock = auto_exposure_lock;
    }

    public boolean getAutoWhiteBalanceLock() {
        return auto_white_balance_lock;
    }

    public void setAutoWhiteBalanceLock(boolean auto_white_balance_lock) {
        this.auto_white_balance_lock = auto_white_balance_lock;
    }

    public String getFocusMode() {
        return focus_mode;
    }

    public void setFocusMode(String focus_mode) {
        this.focus_mode = focus_mode;
    }

    public boolean getImageStabilization() {
        return image_stabilization;
    }

    public void setImageStabilization(boolean image_stabilization) {
        this.image_stabilization = image_stabilization;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    public String getWhiteBalance() {
        return white_balance;
    }

    public void setWhiteBalance(String whitebalance) {
        this.white_balance = whitebalance;
    }

    public int getSizeIndex() {
        return sizeIndex;
    }

    public void setSizeIndex(int sizeIndex) {
        this.sizeIndex = sizeIndex;
    }

    public int getFastFpsMode() {
        return fast_fps_mode;
    }

    public void setFastFpsMode(int fast_fps_mode) {
        this.fast_fps_mode = fast_fps_mode;
    }

    public static String defaultGsonString(){
        Gson gson = new Gson();
        return gson.toJson(new SlaveStreamPreferences());
    }
    public String toGson(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    public static SlaveStreamPreferences fromGson(String s){
        Gson gson = new Gson();
        return gson.fromJson(s, SlaveStreamPreferences.class);
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
    public void addResolutionSupported(String res){
        resolutions_supported.add(res);
    }
    public ArrayList<String> getResolutionsSupported() {
        return resolutions_supported;
    }

    public void setResolutionsSupported(ArrayList<String> resolutions_supported) {
        this.resolutions_supported = resolutions_supported;
    }

    public int getPreferredSide() {
        return preferred_side;
    }

    public void setPreferredSide(int preferred_side) {
        this.preferred_side = preferred_side;
    }

    public String getIpAdress() {
        return ip_adress;
    }

    public void setIpAdress(String ip_adress) {
        this.ip_adress = ip_adress;
    }
}
