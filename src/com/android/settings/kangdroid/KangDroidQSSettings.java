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
package com.android.settings.kangdroid;

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
import android.os.UserHandle;
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
import com.android.settings.kangdroid.SeekBarPreference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import cyanogenmod.providers.CMSettings;
import org.cyanogenmod.internal.util.CmLockPatternUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class KangDroidQSSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
	private static final String PREF_BLOCK_ON_SECURE_KEYGUARD = "block_on_secure_keyguard";
	private static final String STATUS_BAR_QUICK_QS_PULLDOWN = "qs_quick_pulldown";
	private static final String PREF_SMART_PULLDOWN = "smart_pulldown";
    private static final String PREF_TILE_ANIM_STYLE = "qs_tile_animation_style";
    private static final String PREF_TILE_ANIM_DURATION = "qs_tile_animation_duration";
	private static final String PREF_TILE_ANIM_INTERPOLATOR = "qs_tile_animation_interpolator";
    private static final String PREF_QS_TRANSPARENT_SHADE = "qs_transparent_shade";
	private static final String PREF_QS_TRANSPARENT_HEADER = "qs_transparent_header";
	
    private SwitchPreference mBlockOnSecureKeyguard;
	private ListPreference mQuickPulldown;
	private ListPreference mSmartPulldown;
	private ListPreference mNumColumns;
	private ListPreference mNumRows;
    private ListPreference mTileAnimationStyle;
    private ListPreference mTileAnimationDuration;
	private ListPreference mTileAnimationInterpolator;
    private SeekBarPreference mQSShadeAlpha;
	private SeekBarPreference mQSHeaderAlpha;
	
    private static final int MY_USER_ID = UserHandle.myUserId();
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();
		final CmLockPatternUtils lockPatternUtils = new CmLockPatternUtils(getActivity());
		
        addPreferencesFromResource(R.xml.kangdroid_qs_settings);
		
        // Block QS on secure LockScreen
        mBlockOnSecureKeyguard = (SwitchPreference) findPreference(PREF_BLOCK_ON_SECURE_KEYGUARD);
        if (lockPatternUtils.isSecure(MY_USER_ID)) {
            mBlockOnSecureKeyguard.setChecked(Settings.Secure.getIntForUser(resolver,
                    Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD, 1, UserHandle.USER_CURRENT) == 1);
            mBlockOnSecureKeyguard.setOnPreferenceChangeListener(this);
        } else if (mBlockOnSecureKeyguard != null) {
            getPreferenceScreen().removePreference(mBlockOnSecureKeyguard);
        }
		
		// Quick Pulldown Preferences
		mQuickPulldown = (ListPreference) findPreference(STATUS_BAR_QUICK_QS_PULLDOWN);
        int quickPulldown = CMSettings.System.getInt(resolver,
                CMSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, 1);
        mQuickPulldown.setValue(String.valueOf(quickPulldown));
        updatePulldownSummary(quickPulldown);
        mQuickPulldown.setOnPreferenceChangeListener(this);
		
        // Smart pulldown
        mSmartPulldown = (ListPreference) findPreference(PREF_SMART_PULLDOWN);
        mSmartPulldown.setOnPreferenceChangeListener(this);
        int smartPulldown = Settings.System.getInt(resolver,
                Settings.System.QS_SMART_PULLDOWN, 0);
        mSmartPulldown.setValue(String.valueOf(smartPulldown));
        updateSmartPulldownSummary(smartPulldown);
		
        // Number of QS Columns 3,4,5
        mNumColumns = (ListPreference) findPreference("sysui_qs_num_columns");
        int numColumns = Settings.System.getIntForUser(resolver,
                Settings.System.QS_NUM_TILE_COLUMNS, getDefaultNumColumns(),
                UserHandle.USER_CURRENT);
        mNumColumns.setValue(String.valueOf(numColumns));
        updateNumColumnsSummary(numColumns);
        mNumColumns.setOnPreferenceChangeListener(this);
		
        // Number of QS Rows 3,4
        mNumRows = (ListPreference) findPreference("sysui_qs_num_rows");
        int numRows = Settings.System.getIntForUser(resolver,
                Settings.System.QS_NUM_TILE_ROWS, getDefaultNumRows(),
                UserHandle.USER_CURRENT);
        mNumRows.setValue(String.valueOf(numRows));
        updateNumRowsSummary(numRows);
        mNumRows.setOnPreferenceChangeListener(this);
		
        // Add animations to quick settings
        mTileAnimationStyle = (ListPreference) findPreference(PREF_TILE_ANIM_STYLE);
        int tileAnimationStyle = Settings.System.getIntForUser(resolver,
                Settings.System.ANIM_TILE_STYLE, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationStyle.setValue(String.valueOf(tileAnimationStyle));
        updateTileAnimationStyleSummary(tileAnimationStyle);
        updateAnimTileStyle(tileAnimationStyle);
        mTileAnimationStyle.setOnPreferenceChangeListener(this);

        mTileAnimationDuration = (ListPreference) findPreference(PREF_TILE_ANIM_DURATION);
        int tileAnimationDuration = Settings.System.getIntForUser(resolver,
                Settings.System.ANIM_TILE_DURATION, 1500,
                UserHandle.USER_CURRENT);
        mTileAnimationDuration.setValue(String.valueOf(tileAnimationDuration));
        updateTileAnimationDurationSummary(tileAnimationDuration);
        mTileAnimationDuration.setOnPreferenceChangeListener(this);
		
        mTileAnimationInterpolator = (ListPreference) findPreference(PREF_TILE_ANIM_INTERPOLATOR);
        int tileAnimationInterpolator = Settings.System.getIntForUser(resolver,
                Settings.System.ANIM_TILE_INTERPOLATOR, 0,
                UserHandle.USER_CURRENT);
        mTileAnimationInterpolator.setValue(String.valueOf(tileAnimationInterpolator));
        updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
        mTileAnimationInterpolator.setOnPreferenceChangeListener(this);
		
        // QS shade alpha
        mQSShadeAlpha = (SeekBarPreference) findPreference(PREF_QS_TRANSPARENT_SHADE);
        int qSShadeAlpha = Settings.System.getInt(resolver,
                Settings.System.QS_TRANSPARENT_SHADE, 255);
        mQSShadeAlpha.setValue(qSShadeAlpha / 1);
        mQSShadeAlpha.setOnPreferenceChangeListener(this);
		
        // QS header alpha
        mQSHeaderAlpha =
               (SeekBarPreference) findPreference(PREF_QS_TRANSPARENT_HEADER);
        int qSHeaderAlpha = Settings.System.getInt(resolver,
                Settings.System.QS_TRANSPARENT_HEADER, 255);
        mQSHeaderAlpha.setValue(qSHeaderAlpha / 1);
        mQSHeaderAlpha.setOnPreferenceChangeListener(this);
	}
	
    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.APPLICATION;
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		ContentResolver resolver = getActivity().getContentResolver();
		if (preference == mBlockOnSecureKeyguard) {
        Settings.Secure.putInt(resolver,
                Settings.Secure.STATUS_BAR_LOCKED_ON_SECURE_KEYGUARD,
                 (Boolean) newValue ? 1 : 0);
        return true;
    } else if (preference == mQuickPulldown) {
        int quickPulldown = Integer.valueOf((String) newValue);
        CMSettings.System.putInt(
                resolver, CMSettings.System.STATUS_BAR_QUICK_QS_PULLDOWN, quickPulldown);
        updatePulldownSummary(quickPulldown);
        return true;
    } else if (preference == mSmartPulldown) {
        int smartPulldown = Integer.valueOf((String) newValue);
        Settings.System.putInt(resolver, Settings.System.QS_SMART_PULLDOWN, smartPulldown);
        updateSmartPulldownSummary(smartPulldown);
        return true;
    } else if (preference == mNumColumns) {
        int numColumns = Integer.valueOf((String) newValue);
        Settings.System.putIntForUser(resolver, Settings.System.QS_NUM_TILE_COLUMNS,
                numColumns, UserHandle.USER_CURRENT);
        updateNumColumnsSummary(numColumns);
        return true;
    } else if (preference == mNumRows) {
        int numRows = Integer.valueOf((String) newValue);
        Settings.System.putIntForUser(resolver, Settings.System.QS_NUM_TILE_ROWS,
                numRows, UserHandle.USER_CURRENT);
        updateNumRowsSummary(numRows);
        return true;
	} else if (preference == mTileAnimationStyle) {
            int tileAnimationStyle = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_STYLE,
                    tileAnimationStyle, UserHandle.USER_CURRENT);
            updateTileAnimationStyleSummary(tileAnimationStyle);
            updateAnimTileStyle(tileAnimationStyle);
            return true;
        } else if (preference == mTileAnimationDuration) {
            int tileAnimationDuration = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_DURATION,
                    tileAnimationDuration, UserHandle.USER_CURRENT);
            updateTileAnimationDurationSummary(tileAnimationDuration);
            return true;
        } else if (preference == mTileAnimationInterpolator) {
            int tileAnimationInterpolator = Integer.valueOf((String) newValue);
            Settings.System.putIntForUser(resolver, Settings.System.ANIM_TILE_INTERPOLATOR,
                    tileAnimationInterpolator, UserHandle.USER_CURRENT);
            updateTileAnimationInterpolatorSummary(tileAnimationInterpolator);
            return true;
	    } else if (preference == mQSShadeAlpha) {
	        int alpha = (Integer) newValue;
	        Settings.System.putInt(resolver,
	                Settings.System.QS_TRANSPARENT_SHADE, alpha * 1);
	        return true;
	    } else if (preference == mQSHeaderAlpha) {
	        int alpha = (Integer) newValue;
	        Settings.System.putInt(resolver,
	                Settings.System.QS_TRANSPARENT_HEADER, alpha * 1);
	        return true;
	}
		return false;
	}
	
    private void updateSmartPulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // Smart pulldown deactivated
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_off));
        } else {
            String type = null;
            switch (value) {
                case 1:
                    type = res.getString(R.string.smart_pulldown_dismissable);
                    break;
                case 2:
                    type = res.getString(R.string.smart_pulldown_persistent);
                    break;
                default:
                    type = res.getString(R.string.smart_pulldown_all);
                    break;
            }
            // Remove title capitalized formatting
            type = type.toLowerCase();
            mSmartPulldown.setSummary(res.getString(R.string.smart_pulldown_summary, type));
        }
    }
	
    private void updatePulldownSummary(int value) {
        Resources res = getResources();

        if (value == 0) {
            // quick pulldown deactivated
            mQuickPulldown.setSummary(res.getString(R.string.status_bar_quick_qs_pulldown_off));
        } else {
            String direction = res.getString(value == 2
                    ? R.string.status_bar_quick_qs_pulldown_summary_left
                    : R.string.status_bar_quick_qs_pulldown_summary_right);
            mQuickPulldown.setSummary(res.getString(R.string.status_bar_quick_qs_pulldown_summary, direction));
        }
    }
	
    private void updateNumRowsSummary(int numRows) {
        String prefix = (String) mNumRows.getEntries()[mNumRows.findIndexOfValue(String
                .valueOf(numRows))];
        mNumRows.setSummary(getResources().getString(R.string.qs_num_rows_showing, prefix));
    }
	
    private void updateNumColumnsSummary(int numColumns) {
        String prefix = (String) mNumColumns.getEntries()[mNumColumns.findIndexOfValue(String
                .valueOf(numColumns))];
        mNumColumns.setSummary(getResources().getString(R.string.qs_num_columns_showing, prefix));
    }
	
    private int getDefaultNumRows() {
        try {
            Resources res = getActivity().getPackageManager()
                    .getResourcesForApplication("com.android.systemui");
            int val = res.getInteger(res.getIdentifier("quick_settings_num_rows", "integer",
                    "com.android.systemui")); // better not be larger than 4, that's as high as the
                                              // list goes atm
            return Math.max(1, val);
        } catch (Exception e) {
            return 3;
        }
    }

    private int getDefaultNumColumns() {
        try {
            Resources res = getActivity().getPackageManager()
                    .getResourcesForApplication("com.android.systemui");
            int val = res.getInteger(res.getIdentifier("quick_settings_num_columns", "integer",
                    "com.android.systemui")); // better not be larger than 5, that's as high as the
                                              // list goes atm
            return Math.max(1, val);
        } catch (Exception e) {
            return 3;
        }
    }
	
    private void updateTileAnimationStyleSummary(int tileAnimationStyle) {
        String prefix = (String) mTileAnimationStyle.getEntries()[mTileAnimationStyle.findIndexOfValue(String
                .valueOf(tileAnimationStyle))];
        mTileAnimationStyle.setSummary(getResources().getString(R.string.qs_set_animation_style, prefix));
    }

    private void updateTileAnimationDurationSummary(int tileAnimationDuration) {
        String prefix = (String) mTileAnimationDuration.getEntries()[mTileAnimationDuration.findIndexOfValue(String
                .valueOf(tileAnimationDuration))];
        mTileAnimationDuration.setSummary(getResources().getString(R.string.qs_set_animation_duration, prefix));
    }

    private void updateAnimTileStyle(int tileAnimationStyle) {
        if (mTileAnimationDuration != null) {
            if (tileAnimationStyle == 0) {
                mTileAnimationDuration.setSelectable(false);
				mTileAnimationInterpolator.setSelectable(false);
            } else {
                mTileAnimationDuration.setSelectable(true);
				mTileAnimationInterpolator.setSelectable(true);
            }
        }
    }
	
    private void updateTileAnimationInterpolatorSummary(int tileAnimationInterpolator) {
        String prefix = (String) mTileAnimationInterpolator.getEntries()[mTileAnimationInterpolator.findIndexOfValue(String
                .valueOf(tileAnimationInterpolator))];
        mTileAnimationInterpolator.setSummary(getResources().getString(R.string.qs_set_animation_interpolator, prefix));
    }
}
