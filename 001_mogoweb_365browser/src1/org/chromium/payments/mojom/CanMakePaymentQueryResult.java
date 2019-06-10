
// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// This file is autogenerated by:
//     mojo/public/tools/bindings/mojom_bindings_generator.py
// For:
//     components/payments/mojom/payment_request.mojom
//

package org.chromium.payments.mojom;

import org.chromium.base.annotations.SuppressFBWarnings;
import org.chromium.mojo.bindings.DeserializationException;

public final class CanMakePaymentQueryResult {


    public static final int CAN_MAKE_PAYMENT = 0;

    public static final int CANNOT_MAKE_PAYMENT = CAN_MAKE_PAYMENT + 1;

    public static final int QUERY_QUOTA_EXCEEDED = CANNOT_MAKE_PAYMENT + 1;

    public static final int WARNING_CAN_MAKE_PAYMENT = QUERY_QUOTA_EXCEEDED + 1;

    public static final int WARNING_CANNOT_MAKE_PAYMENT = WARNING_CAN_MAKE_PAYMENT + 1;


    private static final boolean IS_EXTENSIBLE = false;

    public static boolean isKnownValue(int value) {
        switch (value) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
                return true;
        }
        return false;
    }

    public static void validate(int value) {
        if (IS_EXTENSIBLE || isKnownValue(value))
            return;

        throw new DeserializationException("Invalid enum value.");
    }

    private CanMakePaymentQueryResult() {}

}