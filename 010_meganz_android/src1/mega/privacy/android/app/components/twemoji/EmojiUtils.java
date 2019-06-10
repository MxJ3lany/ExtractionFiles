package mega.privacy.android.app.components.twemoji;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mega.privacy.android.app.utils.Util;

public final class EmojiUtils {
  private static final Pattern SPACE_REMOVAL = Pattern.compile("[\\s]");

  //returns true when the string contains only emojis. Note that whitespace will be filtered out.
  public boolean isOnlyEmojis(@Nullable final String text) {

    if (!TextUtils.isEmpty(text)) {

      final String inputWithoutSpaces = SPACE_REMOVAL.matcher(text).replaceAll(Matcher.quoteReplacement(""));

      return EmojiManager.getInstance()
            .getEmojiRepetitivePattern()
            .matcher(inputWithoutSpaces)
            .matches();


//      return EmojiManager.getInstance().getEmojiRepetitivePattern().matcher(text).matches();
    }else{

    }
    return false;
  }

  /** returns the emojis that were found in the given text */
  @NonNull public static List<EmojiRange> emojis(@Nullable final String text) {
    return EmojiManager.getInstance().findAllEmojis(text);
  }

//  returns the number of all emojis that were found in the given text
//  public static int emojisCount(@Nullable final String text) {
//    return emojis(text).size();
//  }

  //returns a class that contains all of the emoji information that was found in the given text
  @NonNull public EmojiInformation emojiInformation(@Nullable final String text) {
    final List<EmojiRange> emojis = EmojiManager.getInstance().findAllEmojis(text);

    final boolean isOnlyEmojis = isOnlyEmojis(text);

    return new EmojiInformation(isOnlyEmojis, emojis);
  }

  private EmojiUtils() {
    throw new AssertionError("No instances.");
  }


  public static void log(String message) {
    Util.log("EmojiManager EmojiUtils", message);
  }
}
