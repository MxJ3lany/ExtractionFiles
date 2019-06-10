package com.braintreepayments.api.models;

import android.content.Context;
import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.braintreepayments.api.R;
import com.braintreepayments.api.exceptions.BraintreeException;
import com.braintreepayments.api.internal.GraphQLConstants.Keys;
import com.braintreepayments.api.internal.GraphQLQueryHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import static com.braintreepayments.api.models.PostalAddressParser.COUNTRY_CODE_KEY;

/**
 * Builder used to construct a card tokenization request.
 */
public class CardBuilder extends BaseCardBuilder<CardBuilder> implements Parcelable {

    protected void buildGraphQL(Context context, JSONObject base, JSONObject input) throws BraintreeException,
            JSONException {
        try {
            base.put(Keys.QUERY, GraphQLQueryHelper.getQuery(context, R.raw.tokenize_credit_card_mutation));
        } catch (Resources.NotFoundException | IOException e) {
            throw new BraintreeException("Unable to read GraphQL query", e);
        }

        base.put(OPERATION_NAME_KEY, "TokenizeCreditCard");

        JSONObject creditCard = new JSONObject()
                .put(NUMBER_KEY, mCardnumber)
                .put(EXPIRATION_MONTH_KEY, mExpirationMonth)
                .put(EXPIRATION_YEAR_KEY, mExpirationYear)
                .put(CVV_KEY, mCvv)
                .put(CARDHOLDER_NAME_KEY, mCardholderName);

        JSONObject billingAddress = new JSONObject()
                .put(FIRST_NAME_KEY, mFirstName)
                .put(LAST_NAME_KEY, mLastName)
                .put(COMPANY_KEY, mCompany)
                .put(COUNTRY_CODE_KEY, mCountryCode)
                .put(LOCALITY_KEY, mLocality)
                .put(POSTAL_CODE_KEY, mPostalCode)
                .put(REGION_KEY, mRegion)
                .put(STREET_ADDRESS_KEY, mStreetAddress)
                .put(EXTENDED_ADDRESS_KEY, mExtendedAddress);

        if (billingAddress.length() > 0) {
            creditCard.put(BILLING_ADDRESS_KEY, billingAddress);
        }

        input.put(CREDIT_CARD_KEY, creditCard);
    }

    public CardBuilder() {}

    protected CardBuilder(Parcel in) {
        super(in);
    }

    public static final Creator<CardBuilder> CREATOR = new Creator<CardBuilder>() {
        @Override
        public CardBuilder createFromParcel(Parcel in) {
            return new CardBuilder(in);
        }

        @Override
        public CardBuilder[] newArray(int size) {
            return new CardBuilder[size];
        }
    };
}