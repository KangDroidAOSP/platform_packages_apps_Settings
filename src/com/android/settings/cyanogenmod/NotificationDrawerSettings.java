/*
* Copyright (C) 2014 The CyanogenMod Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.android.settings.cyanogenmod;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.SwitchPreference;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cyanogenmod.providers.CMSettings;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class NotificationDrawerSettings extends SettingsPreferenceFragment  implements OnPreferenceChangeListener{
    private static final String FORCE_EXPANDED_NOTIFICATIONS = "force_expanded_notifications";
    private static final String PREF_CUSTOM_HEADER_DEFAULT = "status_bar_custom_header_default";
	private static final String PREF_ENABLE_TASK_MANAGER = "enable_task_manager";

    private SwitchPreference mForceExpanded;
	private SwitchPreference mEnableTaskManager;
    private ListPreference mCustomHeaderDefault;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.notification_drawer_settings);
        PreferenceScreen prefSet = getPreferenceScreen();
        final ContentResolver resolver = getActivity().getContentResolver();

		mForceExpanded = (SwitchPreference) findPreference(FORCE_EXPANDED_NOTIFICATIONS);
        mForceExpanded.setChecked((Settings.System.getInt(resolver, Settings.System.FORCE_EXPANDED_NOTIFICATIONS, 0) == 1));

        // Status bar custom header default
        mCustomHeaderDefault = (ListPreference) findPreference(PREF_CUSTOM_HEADER_DEFAULT);
        mCustomHeaderDefault.setOnPreferenceChangeListener(this);
        int customHeaderDefault = Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUS_BAR_CUSTOM_HEADER_DEFAULT, 0);
        mCustomHeaderDefault.setValue(String.valueOf(customHeaderDefault));
        mCustomHeaderDefault.setSummary(mCustomHeaderDefault.getEntry());
        // Task manager
        mEnableTaskManager = (SwitchPreference) prefSet.findPreference(PREF_ENABLE_TASK_MANAGER);
        mEnableTaskManager.setChecked((Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.ENABLE_TASK_MANAGER, 0) == 1));
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.MAIN_SETTINGS;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
    	ContentResolver resolver = getActivity().getContentResolver();
		if (preference == mCustomHeaderDefault) {
            int customHeaderDefault = Integer.valueOf((String) newValue);
            int index = mCustomHeaderDefault.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(), 
                Settings.System.STATUS_BAR_CUSTOM_HEADER_DEFAULT, customHeaderDefault);
            mCustomHeaderDefault.setSummary(mCustomHeaderDefault.getEntries()[index]);
            createCustomView();
            return true;
		}
    	return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if  (preference == mForceExpanded) {
            boolean checked = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.FORCE_EXPANDED_NOTIFICATIONS, checked ? 1:0);
            return true;
        } else if  (preference == mEnableTaskManager) {
            boolean enabled = ((SwitchPreference)preference).isChecked();
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.ENABLE_TASK_MANAGER, enabled ? 1:0);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
