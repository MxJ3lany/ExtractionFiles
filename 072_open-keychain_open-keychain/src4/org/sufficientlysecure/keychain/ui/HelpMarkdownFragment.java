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

package org.sufficientlysecure.keychain.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import org.markdown4j.Markdown4jProcessor;
import org.sufficientlysecure.htmltextview.HtmlResImageGetter;
import org.sufficientlysecure.htmltextview.HtmlTextView;
import timber.log.Timber;

import java.io.IOException;

public class HelpMarkdownFragment extends Fragment {

    public static final String ARG_MARKDOWN_RES = "htmlFile";

    /**
     * Create a new instance of HelpHtmlFragment, providing "htmlFile" as an argument.
     */
    static HelpMarkdownFragment newInstance(int markdownRes) {
        HelpMarkdownFragment f = new HelpMarkdownFragment();

        // Supply html raw file input as an argument.
        Bundle args = new Bundle();
        args.putInt(ARG_MARKDOWN_RES, markdownRes);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int mHtmlFile = getArguments().getInt(ARG_MARKDOWN_RES);

        ScrollView scroller = new ScrollView(getActivity());
        HtmlTextView text = new HtmlTextView(getActivity());

        // padding
        int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getActivity()
                .getResources().getDisplayMetrics());
        text.setPadding(padding, padding, padding, 0);

        scroller.addView(text);

        // load markdown from raw resource
        try {
            String html = new Markdown4jProcessor().process(
                    getActivity().getResources().openRawResource(mHtmlFile));
            text.setHtml(html, new HtmlResImageGetter(text));
        } catch (IOException e) {
            Timber.e(e, "IOException");
        }

        return scroller;
    }
}
