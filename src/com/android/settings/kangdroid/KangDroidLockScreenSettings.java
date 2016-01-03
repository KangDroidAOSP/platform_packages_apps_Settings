/*
 * Copyright (C) 2014 The KangDroid Project
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

import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.provider.SearchIndexableResource;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.kangdroid.SeekBarPreference;
import com.android.settings.search.Indexable;

import com.android.internal.logging.MetricsLogger;

import java.util.ArrayList;
import java.util.List;

public class KangDroidLockScreenSettings extends SettingsPreferenceFragment implements Indexable, Preference.OnPreferenceChangeListener {
	
	private static final String KEY_LOCKSCREEN_BLUR_RADIUS = "lockscreen_blur_radius";
	private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
	
	private SeekBarPreference mBlurRadius;
	private ListPreference mLockClockFonts;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.kangdroid_lockscreen_settings);
		
		ContentResolver resolver = getActivity().getContentResolver();
		
        mBlurRadius = (SeekBarPreference) findPreference(KEY_LOCKSCREEN_BLUR_RADIUS);
        mBlurRadius.setValue(Settings.System.getInt(resolver,
                Settings.System.LOCKSCREEN_BLUR_RADIUS, 14));
        mBlurRadius.setOnPreferenceChangeListener(this);
		
        mLockClockFonts = (ListPreference) findPreference(LOCK_CLOCK_FONTS);
        mLockClockFonts.setValue(String.valueOf(Settings.System.getInt(
                resolver, Settings.System.LOCK_CLOCK_FONTS, 0)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);
    }
	
    @Override
    public void onResume() {
        super.onResume();
    }
	
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getApplicationContext().getContentResolver();
         if (preference == mBlurRadius) {
            int width = ((Integer)newValue).intValue();
            Settings.System.putInt(resolver,
                    Settings.System.LOCKSCREEN_BLUR_RADIUS, width);
            return true;
         } else if (preference == mLockClockFonts) {
            Settings.System.putInt(resolver, Settings.System.LOCK_CLOCK_FONTS,
                    Integer.valueOf((String) newValue));
            mLockClockFonts.setValue(String.valueOf(newValue));
            mLockClockFonts.setSummary(mLockClockFonts.getEntry());
            return true;
        }
        return false;
    }
	
    protected int getMetricsCategory()
    {
	return MetricsLogger.APPLICATION;
    }
}