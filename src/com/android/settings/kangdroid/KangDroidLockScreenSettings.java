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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.DialogInterface;
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
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.util.ArrayList;
import java.util.List;

public class KangDroidLockScreenSettings extends SettingsPreferenceFragment implements Indexable, Preference.OnPreferenceChangeListener {
	
	private static final String KEY_LOCKSCREEN_BLUR_RADIUS = "lockscreen_blur_radius";
	private static final String LOCK_CLOCK_FONTS = "lock_clock_fonts";
    private static final String LOCKSCREEN_OWNER_INFO_COLOR = "lockscreen_owner_info_color";
    private static final String LOCKSCREEN_ALARM_COLOR = "lockscreen_alarm_color";
    private static final String LOCKSCREEN_CLOCK_COLOR = "lockscreen_clock_color";
    private static final String LOCKSCREEN_CLOCK_DATE_COLOR = "lockscreen_clock_date_color";
    private static final String LOCKSCREEN_COLORS_RESET = "lockscreen_colors_reset";
	
	private SeekBarPreference mBlurRadius;
	private ListPreference mLockClockFonts;
    private ColorPickerPreference mLockscreenOwnerInfoColorPicker;
    private ColorPickerPreference mLockscreenAlarmColorPicker;
    private ColorPickerPreference mLockscreenClockColorPicker;
    private ColorPickerPreference mLockscreenClockDateColorPicker;
    private Preference mLockscreenColorsReset;

    static final int DEFAULT = 0xffffffff;
	

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
                resolver, Settings.System.LOCK_CLOCK_FONTS, 4)));
        mLockClockFonts.setSummary(mLockClockFonts.getEntry());
        mLockClockFonts.setOnPreferenceChangeListener(this);
		
        int intColor;
        String hexColor;

        mLockscreenOwnerInfoColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_OWNER_INFO_COLOR);
        mLockscreenOwnerInfoColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mLockscreenOwnerInfoColorPicker.setSummary(hexColor);
        mLockscreenOwnerInfoColorPicker.setNewPreviewColor(intColor);

        mLockscreenAlarmColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_ALARM_COLOR);
        mLockscreenAlarmColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_ALARM_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mLockscreenAlarmColorPicker.setSummary(hexColor);
        mLockscreenAlarmColorPicker.setNewPreviewColor(intColor);

        mLockscreenClockColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_CLOCK_COLOR);
        mLockscreenClockColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_CLOCK_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mLockscreenClockColorPicker.setSummary(hexColor);
        mLockscreenClockColorPicker.setNewPreviewColor(intColor);

        mLockscreenClockDateColorPicker = (ColorPickerPreference) findPreference(LOCKSCREEN_CLOCK_DATE_COLOR);
        mLockscreenClockDateColorPicker.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                    Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mLockscreenClockDateColorPicker.setSummary(hexColor);
        mLockscreenClockDateColorPicker.setNewPreviewColor(intColor);

        mLockscreenColorsReset = (Preference) findPreference(LOCKSCREEN_COLORS_RESET);
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
        } else if (preference == mLockscreenOwnerInfoColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, intHex);
            return true;
        } else if (preference == mLockscreenAlarmColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_ALARM_COLOR, intHex);
            return true;
        } else if (preference == mLockscreenClockColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK_COLOR, intHex);
            return true;
        } else if (preference == mLockscreenClockDateColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, intHex);
            return true;
        }
        return false;
    }
	
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mLockscreenColorsReset) {
            resetToDefault();
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.lockscreen_colors_reset_title);
        alertDialog.setMessage(R.string.lockscreen_colors_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
        ContentResolver resolver = getActivity().getContentResolver();
        Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_OWNER_INFO_COLOR, DEFAULT);
        mLockscreenOwnerInfoColorPicker.setNewPreviewColor(DEFAULT);
        mLockscreenOwnerInfoColorPicker.setSummary(R.string.default_string);
        Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_ALARM_COLOR, DEFAULT);
        mLockscreenAlarmColorPicker.setNewPreviewColor(DEFAULT);
        mLockscreenAlarmColorPicker.setSummary(R.string.default_string);
        Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_CLOCK_COLOR, DEFAULT);
        mLockscreenClockColorPicker.setNewPreviewColor(DEFAULT);
        mLockscreenClockColorPicker.setSummary(R.string.default_string);
        Settings.System.putInt(resolver,
                Settings.System.LOCKSCREEN_CLOCK_DATE_COLOR, DEFAULT);
        mLockscreenClockDateColorPicker.setNewPreviewColor(DEFAULT);
        mLockscreenClockDateColorPicker.setSummary(R.string.default_string);
    }
	
    protected int getMetricsCategory()
    {
	return MetricsLogger.APPLICATION;
    }
}