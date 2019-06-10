package eu.faircode.email;

/*
    This file is part of FairEmail.

    FairEmail is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    FairEmail is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with FairEmail.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2018-2019 by Marcel Bokhorst (M66B)
*/

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.os.Handler;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Base64;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.core.util.PatternsCompat;
import androidx.preference.PreferenceManager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static androidx.core.text.HtmlCompat.FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM;
import static androidx.core.text.HtmlCompat.TO_HTML_PARAGRAPH_LINES_CONSECUTIVE;

public class HtmlHelper {
    static final int PREVIEW_SIZE = 250;

    private static final int TRACKING_PIXEL_SURFACE = 25;

    private static final List<String> heads = Collections.unmodifiableList(Arrays.asList(
            "h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "table", "br", "hr"));
    private static final List<String> tails = Collections.unmodifiableList(Arrays.asList(
            "h1", "h2", "h3", "h4", "h5", "h6", "p", "ol", "ul", "li"));

    private static final ExecutorService executor = Executors.newSingleThreadExecutor(Helper.backgroundThreadFactory);

    static String sanitize(Context context, String html) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean paranoid = prefs.getBoolean("paranoid", true);

        Document parsed = Jsoup.parse(html);

        // <html xmlns:v="urn:schemas-microsoft-com:vml"
        //   xmlns:o="urn:schemas-microsoft-com:office:office"
        //   xmlns:w="urn:schemas-microsoft-com:office:word"
        //   xmlns:m="http://schemas.microsoft.com/office/2004/12/omml"
        //   xmlns="http://www.w3.org/TR/REC-html40">

        // <o:p>&nbsp;</o:p></span>

        // Default XHTML namespace: http://www.w3.org/1999/xhtml

        String ns = null;
        for (Element h : parsed.select("html"))
            for (Attribute a : h.attributes()) {
                String key = a.getKey();
                String value = a.getValue();
                if (value != null &&
                        key.startsWith("xmlns:") &&
                        value.startsWith("http://www.w3.org/")) {
                    ns = key.split(":")[1];
                    break;
                }
            }
        for (Element e : parsed.select("*")) {
            String tag = e.tagName();
            if (tag.contains(":")) {
                if (ns != null && tag.startsWith(ns)) {
                    e.tagName(tag.split(":")[1]);
                    Log.i("Updated tag=" + tag + " to=" + e.tagName());
                } else {
                    e.remove();
                    Log.i("Removed tag=" + tag);
                }
            }
        }

        Whitelist whitelist = Whitelist.relaxed()
                .addTags("hr", "abbr")
                .removeTags("col", "colgroup", "thead", "tbody")
                .removeAttributes("table", "width")
                .removeAttributes("td", "colspan", "rowspan", "width")
                .removeAttributes("th", "colspan", "rowspan", "width")
                .addProtocols("img", "src", "cid")
                .addProtocols("img", "src", "data");
        final Document document = new Cleaner(whitelist).clean(parsed);

        // Short quotes
        for (Element q : document.select("q")) {
            q.prependText("\"");
            q.appendText("\"");
            q.tagName("em");
        }

        // Pre formatted text
        for (Element code : document.select("pre")) {
            code.html(code.html().replaceAll("\\r?\\n", "<br />"));
            code.tagName("div");
        }

        // Code
        document.select("code").tagName("div");

        // Lines
        for (Element hr : document.select("hr")) {
            hr.tagName("div");
            hr.text("----------------------------------------");
        }

        // Descriptions
        document.select("dl").tagName("div");
        for (Element dt : document.select("dt")) {
            dt.tagName("strong");
            dt.appendElement("br");
        }
        for (Element dd : document.select("dd")) {
            dd.tagName("em");
            dd.appendElement("br").appendElement("br");
        }

        // Abbreviations
        document.select("abbr").tagName("u");

        // Remove link tracking pixels
        if (paranoid)
            for (Element img : document.select("img"))
                if (isTrackingPixel(img)) {
                    String src = img.attr("src");
                    img.removeAttr("src");
                    img.tagName("a");
                    img.attr("href", src);
                    img.appendText(context.getString(R.string.title_hint_tracking_image,
                            img.attr("width"), img.attr("height")));
                }

        // Tables
        for (Element col : document.select("th,td")) {
            // separate columns by a space
            if (col.nextElementSibling() == null) {
                if (col.selectFirst("div") == null)
                    col.appendElement("br");
            } else
                col.append("&nbsp;");

            if ("th".equals(col.tagName()))
                col.tagName("strong");
            else
                col.tagName("span");
        }

        for (Element row : document.select("tr"))
            row.tagName("span");

        document.select("caption").tagName("p");
        document.select("table").tagName("div");

        // Lists
        for (Element li : document.select("li")) {
            li.tagName("span");
            li.prependText("* ");
            li.appendElement("br"); // line break after list item
        }
        document.select("ol").tagName("div");
        document.select("ul").tagName("div");

        // Autolink
        NodeTraversor.traverse(new NodeVisitor() {
            @Override
            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    TextNode tnode = (TextNode) node;

                    Pattern pattern = Pattern.compile(
                            PatternsCompat.AUTOLINK_EMAIL_ADDRESS.pattern() + "|" +
                                    PatternsCompat.AUTOLINK_WEB_URL.pattern());
                    Matcher matcher = pattern.matcher(tnode.text());
                    if (matcher.find()) {
                        Element span = document.createElement("span");

                        int pos = 0;
                        String text = tnode.text();

                        do {
                            boolean linked = false;
                            Node parent = tnode.parent();
                            while (parent != null) {
                                if ("a".equals(parent.nodeName())) {
                                    linked = true;
                                    break;
                                }
                                parent = parent.parent();
                            }

                            boolean email = matcher.group().contains("@") && !matcher.group().contains(":");
                            if (BuildConfig.DEBUG)
                                Log.i("Web url=" + matcher.group() +
                                        " " + matcher.start() + "..." + matcher.end() + "/" + text.length() +
                                        " linked=" + linked + " email=" + email);

                            if (linked)
                                span.appendText(text.substring(pos, matcher.end()));
                            else {
                                span.appendText(text.substring(pos, matcher.start()));

                                Element a = document.createElement("a");
                                a.attr("href", (email ? "mailto:" : "") + matcher.group());
                                a.text(matcher.group());
                                span.appendChild(a);
                            }

                            pos = matcher.end();
                        } while (matcher.find());
                        span.appendText(text.substring(pos));

                        tnode.before(span);
                        tnode.text("");
                    }
                }
            }

            @Override
            public void tail(Node node, int depth) {
            }
        }, document);

        // Remove block elements displaying nothing
        for (Element e : document.select("*"))
            if (e.isBlock() && !e.hasText() && e.select("img").size() == 0)
                e.remove();

        Element body = document.body();
        return (body == null ? "" : body.html());
    }

    static Drawable decodeImage(final String source, final long id, boolean show, final TextView view) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(view.getContext());
        boolean compact = prefs.getBoolean("compact", false);
        int zoom = prefs.getInt("zoom", compact ? 0 : 1);

        final int px = Helper.dp2pixels(view.getContext(), (zoom + 1) * 24);

        final Resources.Theme theme = view.getContext().getTheme();
        final Resources res = view.getContext().getResources();

        if (TextUtils.isEmpty(source)) {
            Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
            d.setBounds(0, 0, px, px);
            return d;
        }

        boolean embedded = source.startsWith("cid:");
        boolean data = source.startsWith("data:");

        if (BuildConfig.DEBUG)
            Log.i("Image show=" + show + " embedded=" + embedded + " data=" + data + " source=" + source);

        if (!show) {
            // Show placeholder icon
            int resid = (embedded || data ? R.drawable.baseline_photo_library_24 : R.drawable.baseline_image_24);
            Drawable d = res.getDrawable(resid, theme);
            d.setBounds(0, 0, px, px);
            return d;
        }

        // Embedded images
        if (embedded) {
            DB db = DB.getInstance(view.getContext());
            String cid = "<" + source.substring(4) + ">";
            EntityAttachment attachment = db.attachment().getAttachment(id, cid);
            if (attachment == null) {
                Log.i("Image not found CID=" + cid);
                Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
                d.setBounds(0, 0, px, px);
                return d;
            } else if (!attachment.available) {
                Log.i("Image not available CID=" + cid);
                Drawable d = res.getDrawable(R.drawable.baseline_photo_library_24, theme);
                d.setBounds(0, 0, px, px);
                return d;
            } else {
                Bitmap bm = Helper.decodeImage(attachment.getFile(view.getContext()),
                        res.getDisplayMetrics().widthPixels);
                if (bm == null) {
                    Log.i("Image not decodable CID=" + cid);
                    Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
                    d.setBounds(0, 0, px, px);
                    return d;
                } else {
                    Drawable d = new BitmapDrawable(res, bm);
                    d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
                    return d;
                }
            }
        }

        // Data URI
        if (data)
            try {
                // "<img src=\"data:image/png;base64,iVBORw0KGgoAAA" +
                // "ANSUhEUgAAAAUAAAAFCAYAAACNbyblAAAAHElEQVQI12P4" +
                // "//8/w38GIAXDIBKE0DHxgljNBAAO9TXL0Y4OHwAAAABJRU" +
                // "5ErkJggg==\" alt=\"Red dot\" />";

                String base64 = source.substring(source.indexOf(',') + 1);
                byte[] bytes = Base64.decode(base64.getBytes(), 0);

                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (bm == null)
                    throw new IllegalArgumentException("decode byte array failed");

                Drawable d = new BitmapDrawable(res, bm);
                d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                return d;
            } catch (IllegalArgumentException ex) {
                Log.w(ex);
                Drawable d = res.getDrawable(R.drawable.baseline_broken_image_24, theme);
                d.setBounds(0, 0, px, px);
                return d;
            }

        // Get cache file name
        File dir = new File(view.getContext().getCacheDir(), "images");
        if (!dir.exists())
            dir.mkdir();
        final File file = new File(dir, id + "_" + Math.abs(source.hashCode()) + ".png");

        Drawable cached = getCachedImage(view.getContext(), file);
        if (cached != null)
            return cached;

        final LevelListDrawable lld = new LevelListDrawable();
        Drawable wait = res.getDrawable(R.drawable.baseline_hourglass_empty_24, theme);
        lld.addLevel(0, 0, wait);
        lld.setBounds(0, 0, px, px);

        final Context context = view.getContext().getApplicationContext();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    Drawable cached = getCachedImage(context, file);
                    if (cached != null) {
                        post(cached, source);
                        return;
                    }

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    Log.i("Probe " + source);
                    try (InputStream probe = new URL(source).openStream()) {
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeStream(probe, null, options);
                    }

                    Log.i("Download " + source);
                    Bitmap bm;
                    try (InputStream is = new URL(source).openStream()) {
                        int scaleTo = res.getDisplayMetrics().widthPixels;
                        int factor = 1;
                        while (options.outWidth / factor > scaleTo)
                            factor *= 2;

                        if (factor > 1) {
                            Log.i("Download image factor=" + factor);
                            options.inJustDecodeBounds = false;
                            options.inSampleSize = factor;
                            bm = BitmapFactory.decodeStream(is, null, options);
                        } else
                            bm = BitmapFactory.decodeStream(is);
                    }

                    if (bm == null)
                        throw new FileNotFoundException("Download image failed source=" + source);

                    Log.i("Downloaded image source=" + source);

                    try (OutputStream os = new BufferedOutputStream(new FileOutputStream(file))) {
                        bm.compress(Bitmap.CompressFormat.PNG, 90, os);
                    }

                    // Create drawable from bitmap
                    Drawable d = new BitmapDrawable(res, bm);
                    d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                    post(d, source);
                } catch (Throwable ex) {
                    // Show warning icon
                    Log.w(ex);
                    int resid = (ex instanceof IOException && !(ex instanceof FileNotFoundException)
                            ? R.drawable.baseline_cloud_off_24
                            : R.drawable.baseline_broken_image_24);
                    Drawable d = res.getDrawable(resid, theme);
                    d.setBounds(0, 0, px, px);
                    post(d, source);
                }
            }

            private void post(final Drawable d, String source) {
                Log.i("Posting image=" + source);
                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        int w = d.getIntrinsicWidth();
                        int h = d.getIntrinsicHeight();

                        float width = view.getWidth();
                        if (w > width) {
                            float scale = width / w;
                            w = Math.round(w * scale);
                            h = Math.round(h * scale);
                            d.setBounds(0, 0, w, h);
                        }

                        lld.addLevel(1, 1, d);
                        lld.setBounds(0, 0, w, h);
                        lld.setLevel(1);

                        view.setText(view.getText());
                    }
                });
            }
        });

        return lld;
    }

    static private Drawable getCachedImage(Context context, File file) {
        if (file.exists()) {
            Log.i("Using cached " + file);
            Bitmap bm = BitmapFactory.decodeFile(file.getAbsolutePath());
            if (bm != null) {
                Drawable d = new BitmapDrawable(context.getResources(), bm);
                d.setBounds(0, 0, bm.getWidth(), bm.getHeight());
                return d;
            }
        }

        return null;
    }

    static String getPreview(String body) {
        String text = (body == null ? null : Jsoup.parse(body).text());
        return (text == null ? null : text.substring(0, Math.min(text.length(), PREVIEW_SIZE)));
    }

    static String getText(String html) {
        final StringBuilder sb = new StringBuilder();

        NodeTraversor.traverse(new NodeVisitor() {
            private int qlevel = 0;
            private int tlevel = 0;

            public void head(Node node, int depth) {
                if (node instanceof TextNode) {
                    append(((TextNode) node).text());
                    append(" ");
                } else {
                    String name = node.nodeName();
                    if ("li".equals(name))
                        append("* ");
                    else if ("blockquote".equals(name))
                        qlevel++;

                    if (heads.contains(name))
                        newline();
                }
            }

            public void tail(Node node, int depth) {
                String name = node.nodeName();
                if ("a".equals(name)) {
                    append("[");
                    append(node.absUrl("href"));
                    append("] ");
                } else if ("img".equals(name)) {
                    append("[");
                    append(node.absUrl("src"));
                    append("] ");
                } else if ("th".equals(name) || "td".equals(name)) {
                    Node next = node.nextSibling();
                    if (next == null || !("th".equals(next.nodeName()) || "td".equals(next.nodeName())))
                        newline();
                } else if ("blockquote".equals(name))
                    qlevel--;

                if (tails.contains(name))
                    newline();
            }

            private void append(String text) {
                if (tlevel != qlevel) {
                    newline();
                    tlevel = qlevel;
                }
                sb.append(text);
            }

            private void newline() {
                trimEnd(sb);
                sb.append("\n");
                for (int i = 0; i < qlevel; i++)
                    sb.append('>');
                if (qlevel > 0)
                    sb.append(' ');
            }
        }, Jsoup.parse(html));

        trimEnd(sb);
        sb.append("\n");

        return sb.toString();
    }

    static String getHtmlEmbedded(Context context, long id, String html) throws IOException {
        DB db = DB.getInstance(context);

        Document doc = Jsoup.parse(html);
        for (Element img : doc.select("img")) {
            String src = img.attr("src");
            if (src.startsWith("cid:")) {
                String cid = '<' + src.substring(4) + '>';
                EntityAttachment attachment = db.attachment().getAttachment(id, cid);
                if (attachment != null && attachment.available) {
                    File file = attachment.getFile(context);
                    try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
                        byte[] bytes = new byte[(int) file.length()];
                        if (is.read(bytes) != bytes.length)
                            throw new IOException("length");

                        StringBuilder sb = new StringBuilder();
                        sb.append("data:");
                        sb.append(attachment.type);
                        sb.append(";base64,");
                        sb.append(Base64.encodeToString(bytes, Base64.DEFAULT));

                        img.attr("src", sb.toString());
                    }
                }
            }
        }

        return doc.html();
    }

    private static boolean isTrackingPixel(Element img) {
        String src = img.attr("src");
        String width = img.attr("width").trim();
        String height = img.attr("height").trim();

        if (TextUtils.isEmpty(src))
            return false;
        if (TextUtils.isEmpty(width) || TextUtils.isEmpty(height))
            return false;

        try {
            return (Integer.parseInt(width) * Integer.parseInt(height) <= TRACKING_PIXEL_SURFACE);
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static void trimEnd(StringBuilder sb) {
        int length = sb.length();
        while (length > 0 && sb.charAt(length - 1) == ' ')
            length--;
        sb.setLength(length);
    }

    static Spanned fromHtml(@NonNull String html) {
        return fromHtml(html, null, null);
    }

    static Spanned fromHtml(@NonNull String html, @Nullable Html.ImageGetter imageGetter, @Nullable Html.TagHandler tagHandler) {
        Spanned spanned = HtmlCompat.fromHtml(html, FROM_HTML_SEPARATOR_LINE_BREAK_LIST_ITEM, imageGetter, tagHandler);

        int i = spanned.length();
        while (i > 1 && spanned.charAt(i - 2) == '\n' && spanned.charAt(i - 1) == '\n')
            i--;
        if (i != spanned.length())
            spanned = (Spanned) spanned.subSequence(0, i);

        return spanned;
    }

    static String toHtml(Spanned spanned) {
        String html = HtmlCompat.toHtml(spanned, TO_HTML_PARAGRAPH_LINES_CONSECUTIVE);

        // @Google: why convert size to and from in a different way?
        Document doc = Jsoup.parse(html);
        for (Element element : doc.select("span")) {
            String style = element.attr("style");
            if (style.startsWith("font-size:")) {
                int colon = style.indexOf(':');
                int semi = style.indexOf("em;", colon);
                if (semi > colon)
                    try {
                        String hsize = style.substring(colon + 1, semi).replace(',', '.');
                        float size = Float.parseFloat(hsize);
                        element.tagName(size < 1.0f ? "small" : "big");
                        element.attributes().remove("style");
                    } catch (NumberFormatException ex) {
                        Log.e(ex);
                    }
            }
        }

        return doc.outerHtml();
    }
}
