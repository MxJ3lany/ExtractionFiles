/*
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.listeners.AdvanceToNextListener;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.ViewIds;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import java.io.File;
import java.util.ArrayList;

import timber.log.Timber;

/**
 * ListWidget handles select-one fields using radio buttons. The radio buttons are aligned
 * horizontally. They are typically meant to be used in a field list, where multiple questions with
 * the same multiple choice answers can sit on top of each other and make a grid of buttons that is
 * easy to navigate quickly. Optionally, you can turn off the labels. This would be done if a label
 * widget was at the top of your field list to provide the labels. If audio or video are specified
 * in the select answers they are ignored.
 *
 * @author Jeff Beorse (jeff@beorse.net)
 */
@SuppressLint("ViewConstructor")
public class ListWidget extends ItemsWidget implements MultiChoiceWidget, OnCheckedChangeListener {

    @Nullable
    private AdvanceToNextListener listener;

    private final boolean autoAdvance;

    ArrayList<RadioButton> buttons;
    View center;

    public ListWidget(Context context, FormEntryPrompt prompt, boolean displayLabel, boolean autoAdvance) {
        super(context, prompt);

        this.autoAdvance = autoAdvance;
        if (context instanceof AdvanceToNextListener) {
            listener = (AdvanceToNextListener) context;
        }

        buttons = new ArrayList<>();

        // Layout holds the horizontal list of buttons
        LinearLayout buttonLayout = new LinearLayout(context);

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
        }

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                AppCompatRadioButton r = new AppCompatRadioButton(getContext());
                r.setId(ViewIds.generateViewId());
                r.setTag(i);
                r.setEnabled(!prompt.isReadOnly());
                r.setFocusable(!prompt.isReadOnly());

                buttons.add(r);

                if (items.get(i).getValue().equals(s)) {
                    r.setChecked(true);
                }
                r.setOnCheckedChangeListener(this);

                String imageURI;
                if (items.get(i) instanceof ExternalSelectChoice) {
                    imageURI = ((ExternalSelectChoice) items.get(i)).getImage();
                } else {
                    imageURI = prompt.getSpecialFormSelectChoiceText(items.get(i),
                            FormEntryCaption.TEXT_FORM_IMAGE);
                }

                // build image view (if an image is provided)
                ImageView imageView = null;
                TextView missingImage = null;

                final int labelId = ViewIds.generateViewId();

                // Now set up the image view
                String errorMsg = null;
                if (imageURI != null) {
                    try {
                        String imageFilename =
                                ReferenceManager.instance().DeriveReference(imageURI).getLocalURI();
                        final File imageFile = new File(imageFilename);
                        if (imageFile.exists()) {
                            Bitmap b = null;
                            try {
                                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                                int screenWidth = metrics.widthPixels;
                                int screenHeight = metrics.heightPixels;
                                b = FileUtils.getBitmapScaledToDisplay(imageFile, screenHeight, screenWidth);
                            } catch (OutOfMemoryError e) {
                                errorMsg = "ERROR: " + e.getMessage();
                            }

                            if (b != null) {
                                imageView = new ImageView(getContext());
                                imageView.setPadding(2, 2, 2, 2);
                                imageView.setAdjustViewBounds(true);
                                imageView.setImageBitmap(b);
                                imageView.setId(labelId);
                            } else if (errorMsg == null) {
                                // An error hasn't been logged and loading the image failed, so it's
                                // likely
                                // a bad file.
                                errorMsg = getContext().getString(R.string.file_invalid, imageFile);

                            }
                        } else {
                            // An error hasn't been logged. We should have an image, but the file
                            // doesn't
                            // exist.
                            errorMsg = getContext().getString(R.string.file_missing, imageFile);
                        }

                        if (errorMsg != null) {
                            // errorMsg is only set when an error has occured
                            Timber.e(errorMsg);
                            missingImage = new TextView(getContext());
                            missingImage.setText(errorMsg);

                            missingImage.setPadding(2, 2, 2, 2);
                            missingImage.setId(labelId);
                        }

                    } catch (InvalidReferenceException e) {
                        Timber.e(e, "Invalid image reference due to %s ", e.getMessage());
                    }
                }

                // build text label. Don't assign the text to the built in label to he
                // button because it aligns horizontally, and we want the label on top
                TextView label = new TextView(getContext());
                label.setText(prompt.getSelectChoiceText(items.get(i)));
                label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
                label.setGravity(Gravity.CENTER_HORIZONTAL);
                if (!displayLabel) {
                    label.setVisibility(View.GONE);
                }

                // answer layout holds the label text/image on top and the radio button on bottom
                LinearLayout answer = new LinearLayout(getContext());
                answer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams headerParams =
                        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);
                headerParams.gravity = Gravity.CENTER_HORIZONTAL;

                LinearLayout.LayoutParams buttonParams =
                        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);
                buttonParams.gravity = Gravity.CENTER_HORIZONTAL;

                if (imageView != null) {
                    imageView.setScaleType(ScaleType.CENTER);
                    if (!displayLabel) {
                        imageView.setVisibility(View.GONE);
                    }
                    answer.addView(imageView, headerParams);
                } else if (missingImage != null) {
                    answer.addView(missingImage, headerParams);
                } else {
                    if (displayLabel) {
                        label.setId(labelId);
                        answer.addView(label, headerParams);
                    }

                }
                answer.addView(r, buttonParams);
                answer.setPadding(4, 0, 4, 0);

                // Each button gets equal weight
                LinearLayout.LayoutParams answerParams =
                        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                answerParams.weight = 1;

                buttonLayout.addView(answer, answerParams);
            }
        }

        buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.RIGHT_OF, center.getId());
        addView(buttonLayout, params);
    }

    @Override
    public void clearAnswer() {
        for (RadioButton button : this.buttons) {
            if (button.isChecked()) {
                button.setChecked(false);
                widgetValueChanged();
                return;
            }
        }
    }

    @Override
    public IAnswerData getAnswer() {
        int i = getCheckedId();
        if (i == -1) {
            return null;
        } else {
            SelectChoice sc = items.get(i);
            return new SelectOneData(new Selection(sc));
        }
    }

    public int getCheckedId() {
        for (int i = 0; i < buttons.size(); ++i) {
            RadioButton button = buttons.get(i);
            if (button.isChecked()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (!isChecked) {
            // If it got unchecked, we don't care.
            return;
        }

        for (RadioButton button : this.buttons) {
            if (button.isChecked() && buttonView != button) {
                button.setChecked(false);
            }
        }

        if (autoAdvance && listener != null) {
            listener.advance();
        }

        widgetValueChanged();
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        for (RadioButton r : buttons) {
            r.setOnLongClickListener(l);
        }
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        for (RadioButton r : buttons) {
            r.cancelLongPress();
        }
    }

    @Override
    protected void addQuestionMediaLayout(View v) {
        center = new View(getContext());
        RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams(0, 0);
        centerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        center.setId(ViewIds.generateViewId());
        addView(center, centerParams);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.LEFT_OF, center.getId());
        addView(v, params);
    }

    @Override
    public int getChoiceCount() {
        return buttons.size();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        RadioButton button = buttons.get(choiceIndex);
        button.setChecked(true);

        onCheckedChanged(button, true);
    }
}
