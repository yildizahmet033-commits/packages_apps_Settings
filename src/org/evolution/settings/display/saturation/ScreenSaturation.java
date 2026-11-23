/*
 * SPDX-FileCopyrightText: 2026 Ahmet033
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.display.saturation;

import android.content.Context;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;

/** Stores the global screen saturation and applies it through SurfaceFlinger. */
public final class ScreenSaturation {

    private static final String TAG = "ScreenSaturation";

    /** Saturation in percent, 100 is neutral. */
    private static final String KEY = "screen_saturation_percent";
    private static final int MIN = 100;
    private static final int MAX = 200;
    private static final int DEFAULT = 200;

    private static final String SURFACE_COMPOSER_INTERFACE = "android.ui.ISurfaceComposer";
    private static final int TRANSACTION_SATURATION = 1022;

    private ScreenSaturation() {}

    public static int get(Context context) {
        return clamp(Settings.Secure.getIntForUser(context.getContentResolver(), KEY, DEFAULT,
                UserHandle.USER_CURRENT));
    }

    public static void set(Context context, int percent) {
        Settings.Secure.putIntForUser(context.getContentResolver(), KEY, clamp(percent),
                UserHandle.USER_CURRENT);
        apply(percent);
    }

    /** Re-applies the stored value. */
    public static void restore(Context context) {
        apply(get(context));
    }

    private static void apply(int percent) {
        final IBinder flinger = ServiceManager.getService("SurfaceFlinger");
        if (flinger == null) return;
        final Parcel data = Parcel.obtain();
        try {
            data.writeInterfaceToken(SURFACE_COMPOSER_INTERFACE);
            data.writeFloat(clamp(percent) / 100f);
            flinger.transact(TRANSACTION_SATURATION, data, null, 0);
        } catch (RemoteException | SecurityException e) {
            Log.e(TAG, "Failed to apply saturation", e);
        } finally {
            data.recycle();
        }
    }

    private static int clamp(int percent) {
        return Math.max(MIN, Math.min(MAX, percent));
    }
}
