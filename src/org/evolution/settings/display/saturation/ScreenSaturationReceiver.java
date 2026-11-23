/*
 * SPDX-FileCopyrightText: 2026 Ahmet033
 * SPDX-License-Identifier: Apache-2.0
 */

package org.evolution.settings.display.saturation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** The framework resets the saturation to 1.0 on boot, so put the stored value back. */
public class ScreenSaturationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(action)
                || Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            ScreenSaturation.restore(context);
        }
    }
}
