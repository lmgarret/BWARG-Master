package com.camera.simplemjpeg;

/**
 * Created by LM on 10.02.2016.
 */
public class StreamPreferences {
    private int width = 640;
    private int height = 480;

    private int ip_ad1 = 192;
    private int ip_ad2 = 168;
    private int ip_ad3 = 2;
    private int ip_ad4 = 1;
    private int ip_port = 80;
    private String URL = "";

    public StreamPreferences(){

    }
    public StreamPreferences(int[] ip, int width, int height){
        if(ip.length == 5) {
            this.ip_ad1 = ip[0];
            this.ip_ad2 = ip[1];
            this.ip_ad3 = ip[2];
            this.ip_ad4 = ip[3];
            this.ip_port = ip[4];

            StringBuilder sb = new StringBuilder();
            String s_http = "http://";
            String s_dot = ".";
            String s_colon = ":";
            String s_slash = "/";
            sb.append(s_http);
            sb.append(ip_ad1);
            sb.append(s_dot);
            sb.append(ip_ad2);
            sb.append(s_dot);
            sb.append(ip_ad3);
            sb.append(s_dot);
            sb.append(ip_ad4);
            sb.append(s_colon);
            sb.append(ip_port);
            sb.append(s_slash);
            URL = new String(sb);
        }else{
            throw new IllegalArgumentException("IP length too small!");
        }
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
        sb.append(ip_ad1);
        sb.append(s_dot);
        sb.append(ip_ad2);
        sb.append(s_dot);
        sb.append(ip_ad3);
        sb.append(s_dot);
        sb.append(ip_ad4);
        sb.append(s_colon);
        sb.append(ip_port);
        sb.append(s_slash);
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

    public void setURL(String URL) {
        this.URL = URL;
    }
}
