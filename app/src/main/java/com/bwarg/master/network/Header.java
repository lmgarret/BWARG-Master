package com.bwarg.master.network;

/**
 * Created by LM on 08.04.2016.
 */
public enum Header {
    AEL("AEL"), //Auto Exposure Lock
    SSP("SSP"), //Slave Streaming Profile
    IPA("IPA"); //Internet Protocol Address

    public final String name;

    private Header(String s) {
        name = s;
    }

    public boolean equalsName(String otherName) {
        return (otherName == null) ? false : name.equals(otherName);
    }

    public String toString() {
        return this.name;
    }
}
