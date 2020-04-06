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

package com.android.documentsui.dirlist;

import static com.google.common.truth.Truth.assertThat;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.android.documentsui.CrossProfileNoPermissionException;
import com.android.documentsui.CrossProfileQuietModeException;
import com.android.documentsui.Model;
import com.android.documentsui.R;
import com.android.documentsui.base.UserId;
import com.android.documentsui.testing.TestActionHandler;
import com.android.documentsui.testing.TestEnv;
import com.android.documentsui.testing.UserManagers;
import com.android.documentsui.util.VersionUtils;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public final class MessageTest {

    private UserId mUserId = UserId.of(100);
    private Message mInflateMessage;
    private Context mContext;
    private Runnable mDefaultCallback = () -> {
    };
    private UserManager mUserManager;

    @Before
    public void setUp() {
        mContext = mock(Context.class);
        mUserManager = UserManagers.create();
        when(mContext.getSystemService(Context.USER_SERVICE)).thenReturn(mUserManager);
        when(mContext.getResources()).thenReturn(
                InstrumentationRegistry.getInstrumentation().getTargetContext().getResources());
        DocumentsAdapter.Environment env =
                new TestEnvironment(mContext, TestEnv.create(), new TestActionHandler());
        mInflateMessage = new Message.InflateMessage(env, mDefaultCallback);
    }

    @Test
    public void testInflateMessage_updateToCrossProfileNoPermission() {
        Model.Update error = new Model.Update(
                new CrossProfileNoPermissionException(),
                /* isRemoteActionsEnabled= */ true);

        mInflateMessage.update(error);
        assertThat(mInflateMessage.getLayout())
                .isEqualTo(InflateMessageDocumentHolder.LAYOUT_CROSS_PROFILE_ERROR);
        assertThat(mInflateMessage.getTitleString())
                .isEqualTo(mContext.getString(R.string.cant_share_across_profile_error_title));
        // The value varies according to the current user. Simply assert not null.
        assertThat(mInflateMessage.getMessageString()).isNotNull();
        // No button for this error
        assertThat(mInflateMessage.getButtonString()).isNull();
    }

    @Test
    public void testInflateMessage_updateToCrossProfileQuietMode() {
        Model.Update error = new Model.Update(
                new CrossProfileQuietModeException(mUserId),
                /* isRemoteActionsEnabled= */ true);
        mInflateMessage.update(error);
        assertThat(mInflateMessage.getLayout())
                .isEqualTo(InflateMessageDocumentHolder.LAYOUT_CROSS_PROFILE_ERROR);
        assertThat(mInflateMessage.getTitleString())
                .isEqualTo(mContext.getString(R.string.quiet_mode_error_title));
        assertThat(mInflateMessage.getMessageString())
                .isEqualTo(mContext.getString(R.string.quiet_mode_error_message));
        if (VersionUtils.isAtLeastR()) {
            // On R or above, we should have permission and can populate a button
            assertThat(mInflateMessage.getButtonString()).isEqualTo(
                    mContext.getString(R.string.quiet_mode_button));
            assertThat(mInflateMessage.mCallback).isNotNull();
            mInflateMessage.mCallback.run();
            verify(mUserManager, timeout(3000))
                    .requestQuietModeEnabled(false, UserHandle.of(mUserId.getIdentifier()));
        } else {
            assertThat(mInflateMessage.getButtonString()).isNull();
        }
    }
}
