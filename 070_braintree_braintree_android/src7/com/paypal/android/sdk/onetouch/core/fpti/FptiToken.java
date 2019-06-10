package com.paypal.android.sdk.onetouch.core.fpti;

import java.util.Random;

class FptiToken {

    private static final int FPTI_TOKEN_VALIDITY_IN_HOURS = 30;

    String mToken;

    /**
     * Java Date as a long
     */
    private long mValidUntil;

    /**
     * Creates a token, good for 30 hours
     */
    FptiToken() {
        final long now = System.currentTimeMillis();
        if (mToken == null) {
            mValidUntil = now; // force the below if to be true
        }

        if (((mValidUntil + (FPTI_TOKEN_VALIDITY_IN_HOURS * 60 * 1000)) > now)) {
            mValidUntil = now + (FPTI_TOKEN_VALIDITY_IN_HOURS * 60 * 1000);

            final Random r = new Random(mValidUntil);
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; ++i) {
                sb.append((char) ('0' + (Math.abs(r.nextInt()) % 10)));
            }
            mToken = sb.toString();
        }
    }

    /**
     * @return {@code true} if the token is valid (not expired), otherwise {@code false}.
     */
    boolean isValid() {
        return mValidUntil > System.currentTimeMillis();
    }
}
