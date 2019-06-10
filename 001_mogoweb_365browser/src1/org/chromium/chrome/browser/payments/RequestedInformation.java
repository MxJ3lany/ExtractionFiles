
// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// This file is autogenerated by
//     java_cpp_enum.py
// From
//     ../../components/payments/core/journey_logger.h

package org.chromium.chrome.browser.payments;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({
    RequestedInformation.NONE, RequestedInformation.EMAIL, RequestedInformation.PHONE,
    RequestedInformation.SHIPPING, RequestedInformation.NAME, RequestedInformation.MAX
})
@Retention(RetentionPolicy.SOURCE)
public @interface RequestedInformation {
  int NONE = 0;
  int EMAIL = 1 << 0;
  int PHONE = 1 << 1;
  int SHIPPING = 1 << 2;
  int NAME = 1 << 3;
  int MAX = 16;
}
