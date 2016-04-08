package com.bwarg.master.network;

import com.bwarg.master.SlaveStreamPreferences;
import com.bwarg.master.StreamPreferences;

/**
 * Created by LM on 07.04.2016.
 */
public interface SSPListener {
    public void applySettings(SlaveStreamPreferences prefs, String uniqueID);
}
