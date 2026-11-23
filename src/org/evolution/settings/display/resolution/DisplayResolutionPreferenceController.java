/*
 * SPDX-FileCopyrightText: 2026 Ahmet033
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.display.resolution;

import android.content.Context;
import android.graphics.Point;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settingslib.utils.ThreadUtils;

/** Switches between FHD+ and WQHD+ rendering on panels exposing a single WQHD+ resolution. */
public class DisplayResolutionPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "DisplayResolution";

    private static final int FHD_WIDTH = 1080;
    private static final int WQHD_WIDTH = 1440;

    private final IWindowManager mWm = WindowManagerGlobal.getWindowManagerService();

    public DisplayResolutionPreferenceController(Context context, String key) {
        super(context, key);
    }

    @Override
    public int getAvailabilityStatus() {
        final Point size = getInitialSize();
        return size != null && size.x == WQHD_WIDTH ? AVAILABLE : UNSUPPORTED_ON_DEVICE;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final ListPreference preference = screen.findPreference(getPreferenceKey());
        final Point size = getInitialSize();
        if (preference == null || size == null) return;
        preference.setEntries(new CharSequence[] {
                mContext.getString(R.string.display_resolution_fhd,
                        FHD_WIDTH, heightFor(size, FHD_WIDTH)),
                mContext.getString(R.string.display_resolution_wqhd, size.x, size.y)});
        preference.setEntryValues(new CharSequence[] {
                String.valueOf(FHD_WIDTH), String.valueOf(WQHD_WIDTH)});
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        final ListPreference list = (ListPreference) preference;
        final Point current = getCurrentSize();
        if (current == null) return;
        list.setValue(String.valueOf(current.x));
        list.setSummary(list.getEntry());
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final int width = Integer.parseInt((String) newValue);
        ThreadUtils.postOnBackgroundThread(() -> applyWidth(width));
        return true;
    }

    private void applyWidth(int width) {
        final Point initial = getInitialSize();
        final Point current = getCurrentSize();
        if (initial == null || current == null || current.x == width) return;
        try {
            final int userId = UserHandle.myUserId();
            // Scale the current density so that the user's display size choice is kept
            final int density = Math.round(
                    mWm.getBaseDisplayDensity(Display.DEFAULT_DISPLAY) * (float) width / current.x);
            if (width == initial.x) {
                mWm.clearForcedDisplaySize(Display.DEFAULT_DISPLAY);
            } else {
                mWm.setForcedDisplaySize(Display.DEFAULT_DISPLAY, width, heightFor(initial, width));
            }
            if (width == initial.x
                    && density == mWm.getInitialDisplayDensity(Display.DEFAULT_DISPLAY)) {
                mWm.clearForcedDisplayDensityForUser(Display.DEFAULT_DISPLAY, userId);
            } else {
                mWm.setForcedDisplayDensityForUser(Display.DEFAULT_DISPLAY, density, userId);
            }
        } catch (RemoteException | SecurityException e) {
            Log.e(TAG, "Failed to change resolution", e);
        }
    }

    private static int heightFor(Point size, int width) {
        return Math.round(size.y * (float) width / size.x);
    }

    private Point getInitialSize() {
        try {
            final Point size = new Point();
            mWm.getInitialDisplaySize(Display.DEFAULT_DISPLAY, size);
            return size;
        } catch (RemoteException e) {
            return null;
        }
    }

    private Point getCurrentSize() {
        try {
            final Point size = new Point();
            mWm.getBaseDisplaySize(Display.DEFAULT_DISPLAY, size);
            return size;
        } catch (RemoteException e) {
            return null;
        }
    }
}
