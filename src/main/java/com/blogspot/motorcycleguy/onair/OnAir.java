package com.blogspot.motorcycleguy.onair;

import com.sun.jna.platform.win32.Advapi32Util;
import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;

import java.io.FileReader;
import java.io.IOException;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class OnAir {
    public static final String MICROPHONE = "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\CapabilityAccessManager\\ConsentStore\\microphone";
    public static final String MICROPHONE2 = MICROPHONE + "\\NonPackaged";
    public static final String STOPTIME = "LastUsedTimeStop";
    public static final long WINDOWS_TIME_OFFSET = 116444736000000000l;
    public static final long WINDOWS_TIME_DIVISOR = 10000l;
    public static final long POLL_TIME = 3000l;
    public static void main(String args[]) {
        Thread me = Thread.currentThread();
        boolean currentState = isMicrophoneInUse();
        while (!Thread.interrupted()) {
            setLampState(currentState);
            do {
                try {
                    Thread.sleep(POLL_TIME);
                } catch (InterruptedException e) {
                    me.interrupt();
                }
            } while (currentState == isMicrophoneInUse());
            currentState = !currentState;
        }
    }

    public static boolean isMicrophoneInUse() {
        return getApplicationUsingMicrophone() != null;
    }

    public static String getApplicationUsingMicrophone() {
        String app = findAppUsingMicrophone(MICROPHONE);
        if (app == null) {
            app = findAppUsingMicrophone(MICROPHONE2);
        }
        return app;
    }

    public static String findAppUsingMicrophone(String base) {
        String keys[] = getKeys(base);

        for (String key: keys) {
            String app = base + "\\" + key;
            try {
                long value = Advapi32Util.registryGetLongValue(HKEY_CURRENT_USER, app, STOPTIME);
                if (value == 0) {
                    return key;
                }
            } catch (Exception e) {
                // STOPTIME Key does not exist
            }
        }
        return null;
    }

    public static String[] getKeys(String path) {
        return Advapi32Util.registryGetKeys(HKEY_CURRENT_USER, path);
    }

    public static long getJavaTime(long windowsTime) {
        return (windowsTime - WINDOWS_TIME_OFFSET) / WINDOWS_TIME_DIVISOR;
    }

    public static long getWindowsTime(long javaTime) {
        return javaTime * WINDOWS_TIME_DIVISOR + WINDOWS_TIME_OFFSET;
    }

    public static void setLampState(boolean isOn) {
        if (isOn) {
            turnOn();
        } else {
            turnOff();
        }
    }
    public static void turnOn() {
        execNodeScript("c:\\temp\\onair\\turnon.js");
    }
    public static void turnOff() {
        execNodeScript("c:\\temp\\onair\\turnoff.js");
    }

    public static void execNodeScript(String path) {
        try {
            Runtime.getRuntime().exec("node " + path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
