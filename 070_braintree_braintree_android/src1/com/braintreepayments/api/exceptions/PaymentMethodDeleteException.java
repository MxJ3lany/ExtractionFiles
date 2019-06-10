package com.braintreepayments.api.exceptions;

import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.PaymentMethod;
import com.braintreepayments.api.models.PaymentMethodNonce;

/**
 * Error class thrown when a {@link PaymentMethod#deletePaymentMethod(BraintreeFragment, PaymentMethodNonce)}
 * fails to delete a payment method.
 */
public class PaymentMethodDeleteException extends Exception {

    private final PaymentMethodNonce mPaymentMethodNonce;

    public PaymentMethodDeleteException(PaymentMethodNonce paymentMethodNonce, Exception exception) {
        super(exception);
        mPaymentMethodNonce = paymentMethodNonce;
    }

    /**
     * @return The {@link PaymentMethodNonce} that failed to be deleted.
     */
    public PaymentMethodNonce getPaymentMethodNonce() {
        return mPaymentMethodNonce;
    }
}
