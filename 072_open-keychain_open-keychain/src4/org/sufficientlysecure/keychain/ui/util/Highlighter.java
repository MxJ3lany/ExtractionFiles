/*
 * Copyright (C) 2017 Schürmann & Breitmoser GbR
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.sufficientlysecure.keychain.ui.util;

import android.content.Context;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;

import org.sufficientlysecure.keychain.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Highlighter {
    private Context mContext;
    private String mQuery;

    public Highlighter(Context context) {
        mContext = context;
    }

    public void setQuery(String mQuery) {
        this.mQuery = mQuery;
    }

    public Spannable highlight(CharSequence text) {
        Spannable highlight = Spannable.Factory.getInstance().newSpannable(text);

        if (mQuery == null) {
            return highlight;
        }

        String queryPattern = buildPatternFromQuery(mQuery);
        Pattern pattern = Pattern.compile("(" + queryPattern + ")", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);

        int colorEmphasis = FormattingUtils.getColorFromAttr(mContext, R.attr.colorEmphasis);

        while (matcher.find()) {
            highlight.setSpan(new ForegroundColorSpan(colorEmphasis),
                    matcher.start(), matcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return highlight;
    }

    private static String buildPatternFromQuery(String mQuery) {
        String chunks[] = mQuery.split(" *, *");
        boolean firstChunk = true;
        StringBuilder patternPiece = new StringBuilder();
        for (int i = 0; i < chunks.length; ++i) {
            patternPiece.append(Pattern.quote(chunks[i]));
            if (firstChunk) {
                firstChunk = false;
                continue;
            }
            patternPiece.append('|');
        }
        return patternPiece.toString();
    }
}
