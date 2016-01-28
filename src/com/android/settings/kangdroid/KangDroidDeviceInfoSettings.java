/*
 * Copyright (C) 2008 The Android Open Source Project
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
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.kangdroid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SELinux;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.text.TextUtils;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.view.View;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Calendar;
import java.util.HashMap;

public class KangDroidDeviceInfoSettings extends SettingsPreferenceFragment {
	
    private static final String KEY_MOD_VERSION = "mod_version";
    private static final String KEY_MOD_API_LEVEL = "mod_api_level";
    private static final String KEY_DEVICE_CPU = "device_cpu";
    private static final String KEY_DEVICE_MEMORY = "device_memory";
    private static final String KEY_MOD_BUILD_DATE = "build_date";
    private static final String FILENAME_PROC_MEMINFO = "/proc/meminfo";
    private static final String FILENAME_PROC_CPUINFO = "/proc/cpuinfo";
	
	long[] mHits = new long[3];
	
    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DEVICEINFO;
    }
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        addPreferencesFromResource(R.xml.kangdroid_device_info);
        setValueSummary(KEY_MOD_VERSION, cyanogenmod.os.Build.CYANOGENMOD_DISPLAY_VERSION);
        findPreference(KEY_MOD_VERSION).setEnabled(true);
        setExplicitValueSummary(KEY_MOD_API_LEVEL, constructApiLevelString());
        findPreference(KEY_MOD_API_LEVEL).setEnabled(true);
        setValueSummary(KEY_MOD_BUILD_DATE, "ro.build.date");
        findPreference(KEY_MOD_BUILD_DATE).setEnabled(true);
		
        String cpuInfo = getCPUInfo();
        String memInfo = getMemInfo();

        if (cpuInfo != null) {
            setStringSummary(KEY_DEVICE_CPU, cpuInfo);
        } else {
            getPreferenceScreen().removePreference(findPreference(KEY_DEVICE_CPU));
        }

        if (memInfo != null) {
            setStringSummary(KEY_DEVICE_MEMORY, memInfo);
        } else {
            getPreferenceScreen().removePreference(findPreference(KEY_DEVICE_MEMORY));
        }
		
	}
	
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		if (preference.getKey().equals(KEY_MOD_VERSION)) {
		            System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
		            mHits[mHits.length-1] = SystemClock.uptimeMillis();
		            if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
		                Intent intent = new Intent(Intent.ACTION_MAIN);
		                intent.putExtra("is_cm", true);
		                intent.setClassName("android",
		                        com.android.internal.app.PlatLogoActivity.class.getName());
		                try {
		                    startActivity(intent);
		                } catch (Exception e) {
		                    Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
		                }
		            }
		        } else if (preference.getKey().equals(KEY_MOD_BUILD_DATE)) {
		            System.arraycopy(mHits, 1, mHits, 0, mHits.length-1);
		            mHits[mHits.length-1] = SystemClock.uptimeMillis();
		            if (mHits[0] >= (SystemClock.uptimeMillis()-500)) {
		                Intent intent = new Intent();
		                intent.setClassName("com.android.systemui",
		                        "com.android.systemui.tuner.TunerActivity$DemoModeActivity");
		                try {
		                    startActivity(intent);
		                } catch (Exception e) {
		                    Log.e(LOG_TAG, "Unable to start activity " + intent.toString());
		                }
		            }
				}
    	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
	
    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            findPreference(preference).setSummary(
                getResources().getString(R.string.device_info_default));
        }
    }

    private void setValueSummary(String preference, String property) {
        try {
            findPreference(preference).setSummary(
                    SystemProperties.get(property,
                            getResources().getString(R.string.device_info_default)));
        } catch (RuntimeException e) {
            // No recovery
        }
    }
	
    private void setExplicitValueSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            // No recovery
        }
    }
	
    private static String constructApiLevelString() {
        int sdkInt = cyanogenmod.os.Build.CM_VERSION.SDK_INT;
        StringBuilder builder = new StringBuilder();
        builder.append(cyanogenmod.os.Build.getNameForSDKInt(sdkInt))
                .append(" (" + sdkInt + ")");
        return builder.toString();
    }
	
    private String getMemInfo() {
        String result = null;

        try {
            /* /proc/meminfo entries follow this format:
             * MemTotal:         362096 kB
             * MemFree:           29144 kB
             * Buffers:            5236 kB
             * Cached:            81652 kB
             */
            String firstLine = readLine(FILENAME_PROC_MEMINFO);
            if (firstLine != null) {
                String parts[] = firstLine.split("\\s+");
                if (parts.length == 3) {
                    result = Long.parseLong(parts[1])/1024 + " MB";
                }
            }
        } catch (IOException e) {}

        return result;
    }

    private String getCPUInfo() {
        String result = null;

        try {
            /* The expected /proc/cpuinfo output is as follows:
             * Processor   : ARMv7 Processor rev 2 (v7l)
             * BogoMIPS    : 272.62
             *
             * This needs updating, since
             *
             * Hammerhead output :
             * Processor   : ARMv7 Processor rev 0 (v7l)
             * processor   : 0
             * BogoMIPS    : xxx
             *
             * Shamu output :
             * processor   : 0
             * model name  : ARMv7 Processor rev 1 (v7l)
             * BogoMIPS    : xxx
             *
             * Continue reading the file until running into a line starting
             * with either "model name" or "Processor" to meet both
             */
            
            BufferedReader reader = new BufferedReader(new FileReader(FILENAME_PROC_CPUINFO), 256);

            String Line = reader.readLine();

            while (Line != null) {
                if (Line.indexOf("model name") == -1 &&
                    Line.indexOf("Processor" ) == -1    ) {
                    Line = reader.readLine();
                } else {
                    result = Line.split(":")[1].trim();
                    break;
                }
            }

            reader.close();

        } catch (IOException e) {}

        return result;
    }
	
}