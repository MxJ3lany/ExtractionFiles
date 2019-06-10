
// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// This file is autogenerated by
//     java_cpp_enum.py
// From
//     ../../chrome/browser/translate/android/translate_utils.h

package org.chromium.chrome.browser.infobar;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
    TranslateSnackbarType.NONE, TranslateSnackbarType.ALWAYS_TRANSLATE,
    TranslateSnackbarType.NEVER_TRANSLATE, TranslateSnackbarType.NEVER_TRANSLATE_SITE
})
@Retention(RetentionPolicy.SOURCE)
public @interface TranslateSnackbarType {
  int NONE = 0;
  int ALWAYS_TRANSLATE = 1;
  int NEVER_TRANSLATE = 2;
  int NEVER_TRANSLATE_SITE = 3;
}
