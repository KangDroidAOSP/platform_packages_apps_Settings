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

import com.android.internal.logging.MetricsLogger;

import android.app.Activity;
import android.os.Bundle;
import android.content.ContentResolver;
import android.content.Context;
import android.os.SystemProperties;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.widget.Toast;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import com.android.internal.logging.MetricsLogger;

public class KangDroidOtherSettings extends SettingsPreferenceFragment
            implements OnPreferenceChangeListener  {
		
	private static final String TAG = "KangDroidOtherSettings";
	
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.kangdroid_other_settings);
		PreferenceScreen prefSet = getPreferenceScreen();
        final Activity activity = getActivity();
        final ContentResolver resolver = activity.getContentResolver();
    }
	
    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
		return false;
	}
	
    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.DEVELOPMENT;
    }
}