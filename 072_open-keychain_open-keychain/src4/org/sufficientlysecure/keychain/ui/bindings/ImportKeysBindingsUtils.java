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

package org.sufficientlysecure.keychain.ui.bindings;

import android.content.Context;

import org.sufficientlysecure.keychain.R;
import org.sufficientlysecure.keychain.ui.util.FormattingUtils;
import org.sufficientlysecure.keychain.ui.util.Highlighter;
import org.sufficientlysecure.keychain.util.LruCache;

public class ImportKeysBindingsUtils {

    private static LruCache<String, Highlighter> highlighterCache = new LruCache<>(1);

    public static Highlighter getHighlighter(Context context, String query) {
        Highlighter highlighter = highlighterCache.get(query);
        if (highlighter == null) {
            highlighter = new Highlighter(context);
            highlighter.setQuery(query);
            highlighterCache.put(query, highlighter);
        }

        return highlighter;
    }

    public static int getColor(Context context, boolean revokedOrExpired) {
        if (revokedOrExpired) {
            return context.getResources().getColor(R.color.key_flag_gray);
        } else {
            return FormattingUtils.getColorFromAttr(context, R.attr.colorText);
        }
    }

}
