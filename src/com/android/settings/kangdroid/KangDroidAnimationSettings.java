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

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Color;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.widget.Toast;
import android.provider.SearchIndexableResource;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.internal.logging.MetricsLogger;

import java.util.ArrayList;
import java.util.List;

public class KangDroidAnimationSettings extends SettingsPreferenceFragment implements Indexable, Preference.OnPreferenceChangeListener {
	
	private static final String KEY_TOAST_ANIMATION = "toast_animation";
	private ListPreference mToastAnimation;
	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.kangdroid_animation_settings);
		PreferenceScreen prefSet = getPreferenceScreen();

        mToastAnimation = (ListPreference)findPreference(KEY_TOAST_ANIMATION);
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        int CurrentToastAnimation = Settings.System.getInt(getContentResolver(), Settings.System.TOAST_ANIMATION, 1);
        mToastAnimation.setValueIndex(CurrentToastAnimation);
        mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
        mToastAnimation.setOnPreferenceChangeListener(this);
    }
	
    @Override
    public void onResume() {
        super.onResume();
    }
	
    public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == mToastAnimation) {
	         int index = mToastAnimation.findIndexOfValue((String) newValue);
	         Settings.System.putString(getContentResolver(), Settings.System.TOAST_ANIMATION, (String) newValue);
	         mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
	         Toast.makeText(getActivity(), "Toast Test", Toast.LENGTH_SHORT).show();
	         return true;
        }
        return false;
    }
	
    protected int getMetricsCategory()
    {
	return MetricsLogger.APPLICATION;
    }
}