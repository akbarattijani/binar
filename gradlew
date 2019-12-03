/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions an
 * limitations under the License.
 */

package com.android.server.usb;

import android.annotation.Nullable;
import android.content.ComponentName;
import android.content.Context;
import android.hardware.usb.UsbConfiguration;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Slog;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.usb.descriptors.UsbDescriptorParser;
import com.android.server.usb.descriptors.report.TextReportCanvas;
import com.android.server.usb.descriptors.tree.UsbDescriptorsTree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * UsbHostManager manages USB state in host mode.
 */
public class UsbHostManager {
    private static final String TAG = UsbHostManager.class.getSimpleName();
    private static final boolean DEBUG = false;

    private final Context mContext;

    // contains all connected USB devices
    private final HashMap<String, UsbDevice> mDevices = new HashMap<>();

    // USB busses to exclude from USB host support
    private final String[] mHostBlacklist;

    private final Object mLock = new Object();

    private UsbDevice mNewDevice;
    private UsbConfiguration mNewConfiguration;
    private UsbInterface mNewInterface;
    private ArrayList<UsbConfiguration> mNewConfigurations;
    private ArrayList<UsbInterface> mNewInterfaces;
    private ArrayList<UsbEndpoint> mNewEndpoints;

    private final UsbAlsaManager mUsbAlsaManager;
    private final UsbSettingsManager mSettingsManager;

    @GuardedBy("mLock")
    private UsbProfileGroupSettingsManager mCurrentSettings;

    @GuardedBy("mLock")
    private ComponentName mUsbDeviceConnectionHandler;

    public UsbHostManager(Context context, UsbAlsaManager alsaManager,
            UsbSettingsManager settingsManager) {
        mContext = context;

        mHostBlacklist = context.getResources().getStringArray(
                com.android.internal.R.array.config_usbHostBlacklist);
        mUsbAlsaManager = alsaManager;
        mSettingsManager = settingsManager;
        String deviceConnectionHandler = context.getResources().getString(
                com.android.internal.R.string.config_UsbDeviceConnectionHandling_component);
        if (!TextUtils.isEmpty(deviceConnectionHandler)) {
            setUsbDeviceConnectionHandler(ComponentName.unflattenFromString(
                    deviceConnectionHandler));
        }
    }

    public void setCurrentUserSettings(UsbProfileGroupSettingsManager settings) {
        synchronized (mLock) {
            mCurrentSettings = settings;
        }
    }

    private UsbProfileGroupSettingsManager getCurrentUserSettings() {
        synchronized (mLock) {
            return mCurrentSettings;
        }
    }

    public void setUsbDeviceConnectionHandler(@Nullable ComponentName usbDeviceConnectionHandler) {
        synchronized (mLock) {
            mUsbDeviceConnectionHandler = usbDeviceConnectionHandler;
        }
    }

    private @Nullable ComponentName getUsbDeviceConnectionHandler() {
        synchronized (mLock) {
            return mUsbDeviceConnectionHandler;
        }
    }

    private boolean isBlackListed(String deviceName) {
        int count = mHostBlacklist.length;
        for (int i = 0; i < count; i++) {
            if (deviceName.startsWith(mHostBlacklist[i])) {
                return true;
            }
        }
        return false;
    }

    /* returns true if the USB device should not be accessible by applications */
    private boolean isBlackListed(int clazz, int subClass) {
        // blacklist hubs
        if (clazz == UsbConstants.USB_CLASS_HUB) return true;

        // blacklist HID boot devices (mouse and keyboard)
        return clazz == UsbConstants.USB_CLASS_HID
                && subClass == UsbConstants.USB_INTERFACE_SUBCLASS_BOOT;

    }

    /* Called from JNI in monitorUsbHostBus() to report new USB devices
       Returns true if successful, in which case the JNI code will continue adding configurations,
       interfaces and endpoints, and finally call endUsbDeviceAdded after all descriptors
       have been processed
     */
    @SuppressWarnings("unused")
    private boolean beginUsbDeviceAdded(String deviceName, int vendorID, int productID,
            int deviceClass, int deviceSubclass, int deviceProtocol,
            String manuf