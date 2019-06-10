package com.breadwallet.tools.security;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.breadwallet.core.BRCoreMasterPubKey;
import com.breadwallet.tools.manager.BRReportsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.util.Bip39Reader;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 10/11/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class SmartValidator {

    private static final String TAG = SmartValidator.class.getName();

    public static boolean isPaperKeyValid(Context ctx, String paperKey) {
        if (!isValid(ctx, paperKey, Locale.getDefault().getLanguage())) {
            //try all languages
            for (Bip39Reader.SupportedLanguage supportedLanguage : Bip39Reader.SupportedLanguage.values()) {
                if (isValid(ctx, paperKey, supportedLanguage.toString())) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    private static boolean isValid(Context ctx, String paperKey, String language) {
        List<String> list = Bip39Reader.getBip39Words(ctx, language);
        String[] words = list.toArray(new String[list.size()]);
        Log.e(TAG, "isValid: " + words.length);
        if (words.length % Bip39Reader.WORD_LIST_SIZE != 0) {
            Log.e(TAG, "isPaperKeyValid: " + "The list size should divide by " + Bip39Reader.WORD_LIST_SIZE);
            BRReportsManager.reportBug(new IllegalArgumentException("words.length is not dividable by " + Bip39Reader.WORD_LIST_SIZE), true);
        }
        return BRCoreMasterPubKey.validateRecoveryPhrase(words, paperKey);
    }

    public static boolean isPaperKeyCorrect(String insertedPhrase, Context activity) {
        String normalizedPhrase = Normalizer.normalize(insertedPhrase.trim(), Normalizer.Form.NFKD);
        if (!SmartValidator.isPaperKeyValid(activity, normalizedPhrase)) {
            return false;
        }
        byte[] rawPhrase = normalizedPhrase.getBytes();
        byte[] pubKey = new BRCoreMasterPubKey(rawPhrase, true).serialize();
        byte[] pubKeyFromKeyStore = new byte[0];
        try {
            pubKeyFromKeyStore = BRKeyStore.getMasterPublicKey(activity);
        } catch (Exception e) {
            e.printStackTrace();
            BRReportsManager.reportBug(e);
        }
        Arrays.fill(rawPhrase, (byte) 0);
        return Arrays.equals(pubKey, pubKeyFromKeyStore);
    }

    public static String cleanPaperKey(Context activity, String phraseToCheck) {
        return Normalizer.normalize(phraseToCheck.replace("　", " ")
                .replace("\n", " ").trim().replaceAll(" +", " "), Normalizer.Form.NFKD);
    }

    public static boolean isWordValid(Context ctx, String word) {
        List<String> wordList = Bip39Reader.getBip39Words(ctx, null);
        String cleanWord = Bip39Reader.cleanWord(word);
        return wordList.contains(cleanWord);

    }
}
