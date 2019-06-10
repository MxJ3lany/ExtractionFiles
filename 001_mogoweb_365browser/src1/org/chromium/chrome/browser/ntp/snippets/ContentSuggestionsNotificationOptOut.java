
// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// This file is autogenerated by
//     java_cpp_enum.py
// From
//     ../../chrome/browser/ntp_snippets/ntp_snippets_metrics.h

package org.chromium.chrome.browser.ntp.snippets;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
    ContentSuggestionsNotificationOptOut.IMPLICIT, ContentSuggestionsNotificationOptOut.EXPLICIT,
    ContentSuggestionsNotificationOptOut.MAX_NOTIFICATION_OPT_OUT
})
@Retention(RetentionPolicy.SOURCE)
public @interface ContentSuggestionsNotificationOptOut {
  int IMPLICIT = 0;
  int EXPLICIT = 1;
  int MAX_NOTIFICATION_OPT_OUT = 2;
}
