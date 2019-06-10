/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.widgets;

import android.content.Context;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.fragments.dialogs.BikramSambatDatePickerDialog;

import static org.odk.collect.android.fragments.dialogs.CustomDatePickerDialog.DATE_PICKER_DIALOG;

public class BikramSambatDateWidget extends AbstractDateWidget {

    public BikramSambatDateWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);
    }

    protected void showDatePickerDialog() {
        BikramSambatDatePickerDialog bikramSambatDatePickerDialog = BikramSambatDatePickerDialog.newInstance(getFormEntryPrompt().getIndex(), date, datePickerDetails);
        bikramSambatDatePickerDialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), DATE_PICKER_DIALOG);
    }
}
