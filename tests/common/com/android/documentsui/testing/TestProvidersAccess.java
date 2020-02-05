/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.android.documentsui.testing;

import android.provider.DocumentsContract.Root;

import com.android.documentsui.InspectorProvider;
import com.android.documentsui.base.Providers;
import com.android.documentsui.base.RootInfo;
import com.android.documentsui.base.State;
import com.android.documentsui.base.UserId;
import com.android.documentsui.roots.ProvidersAccess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class TestProvidersAccess implements ProvidersAccess {

    public static final RootInfo DOWNLOADS;
    public static final RootInfo HOME;
    public static final RootInfo HAMMY;
    public static final RootInfo PICKLES;
    public static final RootInfo RECENTS;
    public static final RootInfo INSPECTOR;
    public static final RootInfo IMAGE;
    public static final RootInfo AUDIO;
    public static final RootInfo VIDEO;
    public static final RootInfo DOCUMENT;
    public static final RootInfo EXTERNALSTORAGE;
    public static final RootInfo NO_TREE_ROOT;


    static {
        UserId userId = UserId.DEFAULT_USER;

        DOWNLOADS = new RootInfo() {{
            flags = Root.FLAG_SUPPORTS_CREATE;
        }};
        DOWNLOADS.userId = userId;
        DOWNLOADS.authority = Providers.AUTHORITY_DOWNLOADS;
        DOWNLOADS.rootId = Providers.ROOT_ID_DOWNLOADS;
        DOWNLOADS.title = "Downloads";
        DOWNLOADS.derivedType = RootInfo.TYPE_DOWNLOADS;
        DOWNLOADS.flags = Root.FLAG_LOCAL_ONLY
                | Root.FLAG_SUPPORTS_CREATE
                | Root.FLAG_SUPPORTS_RECENTS;

        HOME = new RootInfo();
        HOME.userId = userId;
        HOME.authority = Providers.AUTHORITY_STORAGE;
        HOME.rootId = Providers.ROOT_ID_HOME;
        HOME.title = "Home";
        HOME.derivedType = RootInfo.TYPE_LOCAL;
        HOME.flags = Root.FLAG_LOCAL_ONLY
                | Root.FLAG_SUPPORTS_CREATE
                | Root.FLAG_SUPPORTS_IS_CHILD
                | Root.FLAG_SUPPORTS_RECENTS;

        HAMMY = new RootInfo();
        HAMMY.userId = userId;
        HAMMY.authority = "yummies";
        HAMMY.rootId = "hamsandwich";
        HAMMY.title = "Ham Sandwich";
        HAMMY.derivedType = RootInfo.TYPE_LOCAL;
        HAMMY.flags = Root.FLAG_LOCAL_ONLY;

        PICKLES = new RootInfo();
        PICKLES.userId = userId;
        PICKLES.authority = "yummies";
        PICKLES.rootId = "pickles";
        PICKLES.title = "Pickles";
        PICKLES.summary = "Yummy pickles";

        RECENTS = new RootInfo() {
            {
                // Special root for recents
                derivedType = RootInfo.TYPE_RECENTS;
                flags = Root.FLAG_LOCAL_ONLY;
                availableBytes = -1;
            }
        };
        RECENTS.userId = userId;
        RECENTS.title = "Recents";

        INSPECTOR = new RootInfo();
        INSPECTOR.userId = userId;
        INSPECTOR.authority = InspectorProvider.AUTHORITY;
        INSPECTOR.rootId = InspectorProvider.ROOT_ID;
        INSPECTOR.title = "Inspector";
        INSPECTOR.flags = Root.FLAG_LOCAL_ONLY
            | Root.FLAG_SUPPORTS_CREATE;

        IMAGE = new RootInfo();
        IMAGE.userId = userId;
        IMAGE.authority = Providers.AUTHORITY_MEDIA;
        IMAGE.rootId = Providers.ROOT_ID_IMAGES;
        IMAGE.title = "Images";
        IMAGE.derivedType = RootInfo.TYPE_IMAGES;

        AUDIO = new RootInfo();
        AUDIO.userId = userId;
        AUDIO.authority = Providers.AUTHORITY_MEDIA;
        AUDIO.rootId = Providers.ROOT_ID_AUDIO;
        AUDIO.title = "Audio";
        AUDIO.derivedType = RootInfo.TYPE_AUDIO;

        VIDEO = new RootInfo();
        VIDEO.userId = userId;
        VIDEO.authority = Providers.AUTHORITY_MEDIA;
        VIDEO.rootId = Providers.ROOT_ID_VIDEOS;
        VIDEO.title = "Videos";
        VIDEO.derivedType = RootInfo.TYPE_VIDEO;

        DOCUMENT = new RootInfo();
        DOCUMENT.userId = userId;
        DOCUMENT.authority = Providers.AUTHORITY_MEDIA;
        DOCUMENT.rootId = Providers.ROOT_ID_DOCUMENTS;
        DOCUMENT.title = "Documents";
        DOCUMENT.derivedType = RootInfo.TYPE_DOCUMENTS;

        EXTERNALSTORAGE = new RootInfo();
        EXTERNALSTORAGE.userId = userId;
        EXTERNALSTORAGE.authority = Providers.AUTHORITY_STORAGE;
        EXTERNALSTORAGE.rootId = Providers.ROOT_ID_DEVICE;
        EXTERNALSTORAGE.title = "Device";
        EXTERNALSTORAGE.derivedType = RootInfo.TYPE_LOCAL;
        EXTERNALSTORAGE.flags = Root.FLAG_LOCAL_ONLY
                | Root.FLAG_SUPPORTS_IS_CHILD;

        NO_TREE_ROOT = new RootInfo();
        NO_TREE_ROOT.userId = userId;
        NO_TREE_ROOT.authority = "no.tree.authority";
        NO_TREE_ROOT.rootId = "1";
        NO_TREE_ROOT.title = "No Tree Title";
        NO_TREE_ROOT.derivedType = RootInfo.TYPE_LOCAL;
        NO_TREE_ROOT.flags = Root.FLAG_LOCAL_ONLY;
    }

    public final Map<String, Collection<RootInfo>> roots = new HashMap<>();
    private @Nullable RootInfo nextRoot;

    public TestProvidersAccess() {
        add(DOWNLOADS);
        add(HOME);
        add(HAMMY);
        add(PICKLES);
        add(EXTERNALSTORAGE);
        add(NO_TREE_ROOT);
    }

    private void add(RootInfo root) {
        if (!roots.containsKey(root.authority)) {
            roots.put(root.authority, new ArrayList<>());
        }
        roots.get(root.authority).add(root);
    }

    public void configurePm(TestPackageManager pm) {
        pm.addStubContentProviderForRoot(TestProvidersAccess.DOWNLOADS);
        pm.addStubContentProviderForRoot(TestProvidersAccess.HOME);
        pm.addStubContentProviderForRoot(TestProvidersAccess.HAMMY);
        pm.addStubContentProviderForRoot(TestProvidersAccess.PICKLES);
        pm.addStubContentProviderForRoot(TestProvidersAccess.NO_TREE_ROOT);
    }

    @Override
    public RootInfo getRootOneshot(UserId userId, String authority, String rootId) {
        if (roots.containsKey(authority)) {
            for (RootInfo root : roots.get(authority)) {
                if (rootId.equals(root.rootId) && root.userId.equals(userId)) {
                    return root;
                }
            }
        }
        return null;
    }

    @Override
    public Collection<RootInfo> getMatchingRootsBlocking(State state) {
        List<RootInfo> allRoots = new ArrayList<>();
        for (String authority : roots.keySet()) {
            allRoots.addAll(roots.get(authority));
        }
        return ProvidersAccess.getMatchingRoots(allRoots, state);
    }

    @Override
    public Collection<RootInfo> getRootsForAuthorityBlocking(UserId userId, String authority) {
        return roots.get(authority);
    }

    @Override
    public Collection<RootInfo> getRootsBlocking() {
        List<RootInfo> result = new ArrayList<>();
        for (Collection<RootInfo> vals : roots.values()) {
            result.addAll(vals);
        }
        return result;
    }

    @Override
    public RootInfo getDefaultRootBlocking(State state) {
        return DOWNLOADS;
    }

    @Override
    public RootInfo getRecentsRoot() {
        return RECENTS;
    }

    @Override
    public String getApplicationName(UserId userId, String authority) {
        return "Test Application";
    }

    @Override
    public String getPackageName(UserId userId, String authority) {
        return "com.android.documentsui";
    }
}
