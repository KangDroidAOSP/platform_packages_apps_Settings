/*
 * Copyright (C) 2015 SlimRoms Project
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

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.SlimSeekBarPreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.DialogCreatable;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.kangdroid.KangDroidSlimRecentsSettings;

import com.android.internal.util.slim.DeviceUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class KangDroidRecentsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    @Override
     protected int getMetricsCategory() {
        return MetricsLogger.DEVELOPMENT;
    }

    private static final String TAG = "KangDroidRecentsSettings";

    private static final String SHOW_RECENTS_SEARCHBAR = "recents_show_search_bar";
    private static final String SHOW_MEMBAR_RECENTS = "systemui_recents_mem_display";
    private static final String SHOW_FULLSCREEN_RECENTS = "recents_full_screen";
    private static final String SHOW_CLEAR_ALL_RECENTS = "show_clear_all_recents";
    private static final String RECENTS_DISMISS_ALL = "recents_clear_all_dismiss_all";
    private static final String RECENTS_CLEAR_ALL_LOCATION = "recents_clear_all_location";

    private SwitchPreference mRecentsSearchBar;
    private SwitchPreference mRecentsMemBar;
    private SwitchPreference mRecentsFullscreen;
    private SwitchPreference mRecentsClearAll;
    private SwitchPreference mRecentsDismissAll;
    private ListPreference mRecentsClearAllLocation;
	
	KangDroidSlimRecentsSettings mKDPSlimRecents;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.kangdroid_recents_settings);
        ContentResolver resolver = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();

        mRecentsSearchBar = (SwitchPreference) prefSet.findPreference(SHOW_RECENTS_SEARCHBAR);
        mRecentsMemBar = (SwitchPreference) prefSet.findPreference(SHOW_MEMBAR_RECENTS);
        mRecentsFullscreen = (SwitchPreference) prefSet.findPreference(SHOW_FULLSCREEN_RECENTS);
        mRecentsDismissAll = (SwitchPreference) prefSet.findPreference(RECENTS_DISMISS_ALL);

        mRecentsClearAll = (SwitchPreference) prefSet.findPreference(SHOW_CLEAR_ALL_RECENTS);
        mRecentsClearAll.setChecked(Settings.System.getIntForUser(resolver,
            Settings.System.SHOW_CLEAR_ALL_RECENTS, 1, UserHandle.USER_CURRENT) == 1);
        mRecentsClearAll.setOnPreferenceChangeListener(this);

        mRecentsClearAllLocation = (ListPreference) prefSet.findPreference(RECENTS_CLEAR_ALL_LOCATION);
        int location = Settings.System.getIntForUser(resolver,
                Settings.System.RECENTS_CLEAR_ALL_LOCATION, 3, UserHandle.USER_CURRENT);
        mRecentsClearAllLocation.setValue(String.valueOf(location));
        mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntry());
        mRecentsClearAllLocation.setOnPreferenceChangeListener(this);

    }

    public void disablePreferenceAndUpdate() {
            mRecentsSearchBar.setEnabled(false);
            mRecentsMemBar.setEnabled(false);
            mRecentsFullscreen.setEnabled(false);
            mRecentsClearAll.setEnabled(false);
            mRecentsDismissAll.setEnabled(false);
            mRecentsClearAllLocation.setEnabled(false);
    }
	
	public void enablePreferenceAndUpdate() {
        mRecentsSearchBar.setEnabled(true);
        mRecentsMemBar.setEnabled(true);
        mRecentsFullscreen.setEnabled(true);
        mRecentsClearAll.setEnabled(true);
        mRecentsDismissAll.setEnabled(true);
        mRecentsClearAllLocation.setEnabled(true);
	}

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mRecentsClearAll) {
            Settings.System.putInt(getContentResolver(), Settings.System.SHOW_CLEAR_ALL_RECENTS,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mRecentsClearAllLocation) {
            int location = Integer.valueOf((String) newValue);
            int index = mRecentsClearAllLocation.findIndexOfValue((String) newValue);
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.RECENTS_CLEAR_ALL_LOCATION, location, UserHandle.USER_CURRENT);
            mRecentsClearAllLocation.setSummary(mRecentsClearAllLocation.getEntries()[index]);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}