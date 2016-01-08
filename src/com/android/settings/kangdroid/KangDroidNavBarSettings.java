/*
* Copyright (C) 2015 Benzo Rom
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

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.Handler;
import android.preference.ListPreference;
import android.preference.SlimSeekBarPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;

import android.util.Log;
import android.view.IWindowManager;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.WindowManagerGlobal;

import com.android.internal.logging.MetricsLogger;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.cyanogenmod.ButtonBacklightBrightness;

import cyanogenmod.providers.CMSettings;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import cyanogenmod.hardware.CMHardwareManager;

import java.util.List;

import com.android.internal.util.temasek.TemasekUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class KangDroidNavBarSettings extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener  {
				
	private static final String TAG = "KDPNavBar";
				
	private static final String NAVIGATION_BAR_TINT = "navigation_bar_tint";
	private static final String KEY_ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";
    private static final String KEY_NAVIGATION_RECENTS_LONG_PRESS = "navigation_recents_long_press";
	
	//SlimDim
    private static final String DIM_NAV_BUTTONS = "dim_nav_buttons";
    private static final String DIM_NAV_BUTTONS_TOUCH_ANYWHERE = "dim_nav_buttons_touch_anywhere";
    private static final String DIM_NAV_BUTTONS_TIMEOUT = "dim_nav_buttons_timeout";
    private static final String DIM_NAV_BUTTONS_ALPHA = "dim_nav_buttons_alpha";
    private static final String DIM_NAV_BUTTONS_ANIMATE = "dim_nav_buttons_animate";
    private static final String DIM_NAV_BUTTONS_ANIMATE_DURATION = "dim_nav_buttons_animate_duration";
	
	private ColorPickerPreference mNavbarButtonTint;
	private SwitchPreference mEnableNavigationBar;
    private SwitchPreference mNavigationBarLeftPref;
    private ListPreference mNavigationRecentsLongPressAction;
	
	//SlimDim
    SwitchPreference mDimNavButtons;
    SwitchPreference mDimNavButtonsTouchAnywhere;
    SlimSeekBarPreference mDimNavButtonsTimeout;
    SlimSeekBarPreference mDimNavButtonsAlpha;
    SwitchPreference mDimNavButtonsAnimate;
    SlimSeekBarPreference mDimNavButtonsAnimateDuration;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.kangdroid_nav_bar_settings);
		
        // Navigation bar button color
        mNavbarButtonTint = (ColorPickerPreference) findPreference(NAVIGATION_BAR_TINT);
        mNavbarButtonTint.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.NAVIGATION_BAR_TINT, 0xffffffff);
        String hexColor = String.format("#%08x", (0xffffffff & intColor));
        mNavbarButtonTint.setSummary(hexColor);
        mNavbarButtonTint.setNewPreviewColor(intColor);
		
        // Navigation bar keys switch
        mEnableNavigationBar = (SwitchPreference) findPreference(KEY_ENABLE_NAVIGATION_BAR);
		
        // Internal bool to check if the device have a navbar by default or not!
        boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1;
        mEnableNavigationBar.setChecked(enableNavigationBar);
        mEnableNavigationBar.setOnPreferenceChangeListener(this);
		
        // Navigation bar left
        mNavigationBarLeftPref = (SwitchPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);
//        if (mNavigationBarLeftPref != null) {
  //          mNavigationBarLeftPref.setEnabled(enabled);
    //    }
		
        // Navigation bar recents long press activity needs custom setup
        mNavigationRecentsLongPressAction =
                initRecentsLongPressAction(KEY_NAVIGATION_RECENTS_LONG_PRESS);
		
		//SlimDim
        mDimNavButtons = (SwitchPreference) findPreference(DIM_NAV_BUTTONS);
        mDimNavButtons.setOnPreferenceChangeListener(this);

        mDimNavButtonsTouchAnywhere = (SwitchPreference) findPreference(DIM_NAV_BUTTONS_TOUCH_ANYWHERE);
        mDimNavButtonsTouchAnywhere.setOnPreferenceChangeListener(this);

        mDimNavButtonsTimeout = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_TIMEOUT);
        mDimNavButtonsTimeout.setDefault(3000);
        mDimNavButtonsTimeout.isMilliseconds(true);
        mDimNavButtonsTimeout.setInterval(1);
        mDimNavButtonsTimeout.minimumValue(100);
        mDimNavButtonsTimeout.multiplyValue(100);
        mDimNavButtonsTimeout.setOnPreferenceChangeListener(this);

        mDimNavButtonsAlpha = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_ALPHA);
        mDimNavButtonsAlpha.setDefault(50);
        mDimNavButtonsAlpha.setInterval(1);
        mDimNavButtonsAlpha.setOnPreferenceChangeListener(this);

        mDimNavButtonsAnimate = (SwitchPreference) findPreference(DIM_NAV_BUTTONS_ANIMATE);
        mDimNavButtonsAnimate.setOnPreferenceChangeListener(this);

        mDimNavButtonsAnimateDuration = (SlimSeekBarPreference) findPreference(DIM_NAV_BUTTONS_ANIMATE_DURATION);
        mDimNavButtonsAnimateDuration.setDefault(2000);
        mDimNavButtonsAnimateDuration.isMilliseconds(true);
        mDimNavButtonsAnimateDuration.setInterval(1);
        mDimNavButtonsAnimateDuration.minimumValue(100);
        mDimNavButtonsAnimateDuration.multiplyValue(100);
        mDimNavButtonsAnimateDuration.setOnPreferenceChangeListener(this);
		
		updateNavBarSettings();
    }
	
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mNavbarButtonTint) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_TINT, intHex);
            return true;
		} else if (preference == mEnableNavigationBar) {
            mEnableNavigationBar.setEnabled(true);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW,
                        ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mNavigationRecentsLongPressAction) {
            // RecentsLongPressAction is handled differently because it intentionally uses
            // Settings.System
            String putString = (String) newValue;
            int index = mNavigationRecentsLongPressAction.findIndexOfValue(putString);
            CharSequence summary = mNavigationRecentsLongPressAction.getEntries()[index];
            // Update the summary
            mNavigationRecentsLongPressAction.setSummary(summary);
            if (putString.length() == 0) {
                putString = null;
            }
            CMSettings.Secure.putString(getContentResolver(),
                    CMSettings.Secure.RECENTS_LONG_PRESS_ACTIVITY, putString);
            return true;
        } else if (preference == mDimNavButtons) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsTouchAnywhere) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_TOUCH_ANYWHERE,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsTimeout) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_TIMEOUT, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mDimNavButtonsAlpha) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_ALPHA, Integer.parseInt((String) newValue));
            return true;
        } else if (preference == mDimNavButtonsAnimate) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_ANIMATE,
                    ((Boolean) newValue) ? 1 : 0);
            return true;
        } else if (preference == mDimNavButtonsAnimateDuration) {
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.DIM_NAV_BUTTONS_ANIMATE_DURATION,
                Integer.parseInt((String) newValue));
            return true;
		}
		return false;
	}

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DEVELOPMENT;
    }
	
    private ListPreference initRecentsLongPressAction(String key) {
        ListPreference list = (ListPreference) getPreferenceScreen().findPreference(key);
        list.setOnPreferenceChangeListener(this);

        // Read the componentName from Settings.Secure, this is the user's prefered setting
        String componentString = CMSettings.Secure.getString(getContentResolver(),
                CMSettings.Secure.RECENTS_LONG_PRESS_ACTIVITY);
        ComponentName targetComponent = null;
        if (componentString == null) {
            list.setSummary(getString(R.string.hardware_keys_action_last_app));
        } else {
            targetComponent = ComponentName.unflattenFromString(componentString);
        }

        // Dyanamically generate the list array,
        // query PackageManager for all Activites that are registered for ACTION_RECENTS_LONG_PRESS
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(cyanogenmod.content.Intent.ACTION_RECENTS_LONG_PRESS);
        List<ResolveInfo> recentsActivities = pm.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        if (recentsActivities.size() == 0) {
            // No entries available, disable
            list.setSummary(getString(R.string.hardware_keys_action_last_app));
            CMSettings.Secure.putString(getContentResolver(),
                    CMSettings.Secure.RECENTS_LONG_PRESS_ACTIVITY, null);
            list.setEnabled(false);
            return list;
        }

        CharSequence[] entries = new CharSequence[recentsActivities.size() + 1];
        CharSequence[] values = new CharSequence[recentsActivities.size() + 1];
        // First entry is always default last app
        entries[0] = getString(R.string.hardware_keys_action_last_app);
        values[0] = "";
        list.setValue(values[0].toString());
        int i = 1;
        for (ResolveInfo info : recentsActivities) {
            try {
                // Use pm.getApplicationInfo for the label,
                // we cannot rely on ResolveInfo that comes back from queryIntentActivities.
                entries[i] = pm.getApplicationInfo(info.activityInfo.packageName, 0).loadLabel(pm);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Error package not found: " + info.activityInfo.packageName, e);
                // Fallback to package name
                entries[i] = info.activityInfo.packageName;
            }

            // Set the value to the ComponentName that will handle this intent
            ComponentName entryComponent = new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);
            values[i] = entryComponent.flattenToString();
            if (targetComponent != null) {
                if (entryComponent.equals(targetComponent)) {
                    // Update the selected value and the preference summary
                    list.setSummary(entries[i]);
                    list.setValue(values[i].toString());
                }
            }
            i++;
        }
        list.setEntries(entries);
        list.setEntryValues(values);
        return list;
    }
	
    private void updateNavBarSettings() {
         boolean enableNavigationBar = Settings.System.getInt(getContentResolver(),
                 Settings.System.NAVIGATION_BAR_SHOW,
                 TemasekUtils.isNavBarDefault(getActivity()) ? 1 : 0) == 1;
         mEnableNavigationBar.setChecked(enableNavigationBar);

         if (mDimNavButtons != null) {
             mDimNavButtons.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.DIM_NAV_BUTTONS, 0) == 1);
         }

         if (mDimNavButtonsTouchAnywhere != null) {
             mDimNavButtonsTouchAnywhere.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.DIM_NAV_BUTTONS_TOUCH_ANYWHERE, 0) == 1);
         }

         if (mDimNavButtonsTimeout != null) {
             final int dimTimeout = Settings.System.getInt(getContentResolver(),
                     Settings.System.DIM_NAV_BUTTONS_TIMEOUT, 3000);
             // minimum 100 is 1 interval of the 100 multiplier
             mDimNavButtonsTimeout.setInitValue((dimTimeout / 100) - 1);
         }

         if (mDimNavButtonsAlpha != null) {
             int alphaScale = Settings.System.getInt(getContentResolver(),
                     Settings.System.DIM_NAV_BUTTONS_ALPHA, 50);
             mDimNavButtonsAlpha.setInitValue(alphaScale);
         }

         if (mDimNavButtonsAnimate != null) {
             mDimNavButtonsAnimate.setChecked(Settings.System.getInt(getContentResolver(),
                     Settings.System.DIM_NAV_BUTTONS_ANIMATE, 0) == 1);
         }

         if (mDimNavButtonsAnimateDuration != null) {
             final int animateDuration = Settings.System.getInt(getContentResolver(),
                     Settings.System.DIM_NAV_BUTTONS_ANIMATE_DURATION, 2000);
             // minimum 100 is 1 interval of the 100 multiplier
             mDimNavButtonsAnimateDuration.setInitValue((animateDuration / 100) - 1);
         }

         updateNavbarPreferences(enableNavigationBar);
     }

     private void updateNavbarPreferences(boolean show) {
         mDimNavButtons.setEnabled(show);
         mDimNavButtonsTimeout.setEnabled(show);
         mDimNavButtonsAlpha.setEnabled(show);
         mDimNavButtonsAnimate.setEnabled(show);
         mDimNavButtonsAnimateDuration.setEnabled(show);
     }
}