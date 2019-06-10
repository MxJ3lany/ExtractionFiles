
// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// This file is autogenerated by
//     java_cpp_enum.py
// From
//     ../../components/offline_pages/core/background/save_page_request.h

package org.chromium.components.offlinepages;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
    RequestState.AVAILABLE, RequestState.PAUSED, RequestState.OFFLINING
})
@Retention(RetentionPolicy.SOURCE)
public @interface RequestState {
  int AVAILABLE = 0;
  int PAUSED = 1;
  int OFFLINING = 2;
}
