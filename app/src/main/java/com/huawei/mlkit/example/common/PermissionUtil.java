/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.mlkit.example.common;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;


/**
 * The class is used to get permissions the app required
 *
 * @author
 * @since 2020-02-29
 */
public class PermissionUtil {
    private static final String TAG = "PermissionUtil";

    private PermissionUtil() {
    }

    /**
     * Check whether all permissions are authorized.
     *
     * @param context Context
     * @param permissions Permission array
     * @return If all permissions are authorized, true is returned. Otherwise, false is returned.
     */
    public static boolean isAllPermissionGranted(Context context, String[] permissions) {
        if (context == null || permissions == null) {
            return false;
        }
        for (String permission : permissions) {
            int per = context.checkSelfPermission(permission);
            if (per != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "required permission not granted . "
                        + "permission = " + permission);
                return false;
            }
        }
        Log.w(TAG, "has all permissions");
        return true;
    }

    /**
     * Request permission.
     *
     * @param activity activity object
     * @param permissions Permissions to be applied for
     * @param requestCode Request code, which is used for callback in onRequestPermissionsResult.
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        if (activity == null) {
            Log.e(TAG, "context is null , can not request permissions");
            return;
        }
        activity.requestPermissions(permissions, requestCode);
    }
}
