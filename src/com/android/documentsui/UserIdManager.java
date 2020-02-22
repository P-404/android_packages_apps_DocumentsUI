/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.android.documentsui;

import static androidx.core.util.Preconditions.checkNotNull;

import static com.android.documentsui.base.SharedMinimal.DEBUG;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.core.os.BuildCompat;

import com.android.documentsui.base.UserId;

import java.util.ArrayList;
import java.util.List;


/**
 * Interface to query user ids.
 */
public interface UserIdManager {

    /**
     * Returns the {@UserId} of each profile which should be queried for documents. This
     * will always include {@link UserId#CURRENT_USER}.
     */
    List<UserId> getUserIds();

    /**
     * Creates an implementation of {@link UserIdManager}.
     */
    static UserIdManager create(Context context) {
        return new RuntimeUserIdManager(context);
    }

    /**
     * Implementation of {@link UserIdManager}.
     */
    final class RuntimeUserIdManager implements UserIdManager {

        private static final String TAG = "UserIdManager";

        private static final boolean ENABLE_MULTI_PROFILES = false; // compile-time feature flag

        private final Context mContext;
        private final UserId mCurrentUser;
        private final boolean mIsDeviceSupported;

        private RuntimeUserIdManager(Context context) {
            this(context, UserId.CURRENT_USER,
                    ENABLE_MULTI_PROFILES && isDeviceSupported(context));
        }

        @VisibleForTesting
        RuntimeUserIdManager(Context context, UserId currentUser, boolean isDeviceSupported) {
            mContext = context.getApplicationContext();
            mCurrentUser = checkNotNull(currentUser);
            mIsDeviceSupported = isDeviceSupported;
        }

        @Override
        public List<UserId> getUserIds() {
            final List<UserId> result = new ArrayList<>();
            result.add(mCurrentUser);

            // If the feature is disabled, return a list just containing the current user.
            if (!mIsDeviceSupported) {
                return result;
            }

            UserManager userManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            if (userManager == null) {
                Log.e(TAG, "cannot obtain user manager");
                return result;
            }

            final List<UserHandle> userProfiles = userManager.getUserProfiles();
            if (userProfiles.size() < 2) {
                return result;
            }

            UserId systemUser = null;
            UserId managedUser = null;
            for (UserHandle userHandle : userProfiles) {
                if (userHandle.isSystem()) {
                    systemUser = UserId.of(userHandle);
                    continue;
                }
                if (managedUser == null
                        && userManager.isManagedProfile(userHandle.getIdentifier())) {
                    managedUser = UserId.of(userHandle);
                }
            }

            if (mCurrentUser.isSystem()) {
                // 1. If the current user is system (personal), add the managed user.
                if (managedUser != null) {
                    result.add(managedUser);
                }
            } else if (mCurrentUser.isManagedProfile(userManager)) {
                // 2. If the current user is a managed user, add the personal user.
                // Since we don't have MANAGED_USERS permission to get the parent user, we will
                // treat the system as personal although the system can theoretically in the profile
                // group but not being the parent user(personal) of the managed user.
                if (systemUser != null) {
                    result.add(0, systemUser);
                }
            } else {
                // 3. If we cannot resolve the users properly, we will disable the cross-profile
                // feature by returning just the current user.
                if (DEBUG) {
                    Log.w(TAG, "The current user " + UserId.CURRENT_USER
                            + " is neither system nor managed user. has system user: "
                            + (systemUser != null));
                }
            }
            return result;
        }

        private static boolean isDeviceSupported(Context context) {
            // The feature requires Android R DocumentsContract APIs and INTERACT_ACROSS_USERS
            // permission.
            return (BuildCompat.isAtLeastR()
                    || (Build.VERSION.CODENAME.equals("REL") && Build.VERSION.SDK_INT >= 30))
                    && context.checkSelfPermission(Manifest.permission.INTERACT_ACROSS_USERS)
                    == PackageManager.PERMISSION_GRANTED;
        }
    }
}
