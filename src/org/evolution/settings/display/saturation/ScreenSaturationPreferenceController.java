/*
 * SPDX-FileCopyrightText: 2026 Ahmet033
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.display.saturation;

import android.content.Context;

import androidx.preference.Preference;

import com.android.settings.core.BasePreferenceController;

import org.evolution.settings.preferences.CustomSeekBarPreference;

public class ScreenSaturationPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public ScreenSaturationPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        ((CustomSeekBarPreference) preference).setValue(ScreenSaturation.get(mContext));
        // Changing the color mode resets the saturation, restore it when coming back
        ScreenSaturation.restore(mContext);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ScreenSaturation.set(mContext, (Integer) newValue);
        return true;
    }
}
