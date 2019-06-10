/*
 *    Calendula - An assistant for personal medication management.
 *    Copyright (C) 2014-2018 CiTIUS - University of Santiago de Compostela
 *
 *    Calendula is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 3 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package es.usc.citius.servando.calendula.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.codetroopers.betterpickers.numberpicker.NumberPickerBuilder;
import com.codetroopers.betterpickers.numberpicker.NumberPickerDialogFragment;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.j256.ormlite.stmt.PreparedQuery;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.view.IconicsImageView;

import org.joda.time.LocalDate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import es.usc.citius.servando.calendula.CalendulaApp;
import es.usc.citius.servando.calendula.R;
import es.usc.citius.servando.calendula.activities.MedicinesActivity;
import es.usc.citius.servando.calendula.activities.ScheduleCreationActivity;
import es.usc.citius.servando.calendula.database.DB;
import es.usc.citius.servando.calendula.drugdb.DBRegistry;
import es.usc.citius.servando.calendula.drugdb.PrescriptionDBMgr;
import es.usc.citius.servando.calendula.drugdb.model.persistence.Prescription;
import es.usc.citius.servando.calendula.modules.ModuleManager;
import es.usc.citius.servando.calendula.modules.modules.StockModule;
import es.usc.citius.servando.calendula.persistence.Medicine;
import es.usc.citius.servando.calendula.persistence.Presentation;
import es.usc.citius.servando.calendula.util.IconUtils;
import es.usc.citius.servando.calendula.util.LogUtil;
import es.usc.citius.servando.calendula.util.PreferenceKeys;
import es.usc.citius.servando.calendula.util.PreferenceUtils;
import es.usc.citius.servando.calendula.util.Snack;
import es.usc.citius.servando.calendula.util.Strings;
import es.usc.citius.servando.calendula.util.stock.MedicineScheduleStockProvider;
import es.usc.citius.servando.calendula.util.stock.StockCalculator;
import es.usc.citius.servando.calendula.util.stock.StockDisplayUtils;

public class MedicineCreateOrEditFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener,
        NumberPickerDialogFragment.NumberPickerDialogHandlerV2 {


    private static final int DIALOG_STOCK_ADD = 1;
    private static final int DIALOG_STOCK_REMOVE = 2;

    private static final String TAG = "MedicineCreateOrEditFr";
    OnMedicineEditListener mMedicineEditCallback;
    Medicine mMedicine;
    Prescription mPrescription;

    Boolean showConfirmButton = true;

    @BindView(R.id.medicine_edit_name)
    TextView mNameTextView;
    @BindView(R.id.textView3)
    TextView mPresentationTv;

    Presentation selectedPresentation;

    @BindView(R.id.med_presentation_scroll)
    HorizontalScrollView presentationScroll;
    @BindView(R.id.scrollView)
    ScrollView verticalScrollView;

    @BindView(R.id.stock_layout)
    RelativeLayout stockLayout;
    @BindView(R.id.stock_units)
    TextView mStockUnits;
    @BindView(R.id.stock_estimated_duration)
    TextView mStockEstimation;
    @BindView(R.id.stock_switch)
    Switch stockSwitch;
    @BindView(R.id.btn_stock_add)
    IconicsImageView addBtn;
    @BindView(R.id.btn_stock_remove)
    IconicsImageView rmBtn;
    @BindView(R.id.btn_stock_reset)
    IconicsImageView resetBtn;

    boolean enableSearch = false;
    long mMedicineId;
    int pColor;
    PrescriptionDBMgr dbMgr;

    float stock = -1;
    String estimatedStockText = "";
    private String mIntentAction;

    private Unbinder unbinder;

    private static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }
        return views;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbMgr = DBRegistry.instance().current();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_create_or_edit_medicine, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        if (ModuleManager.isEnabled(StockModule.ID)) {
            stockLayout.setVisibility(View.VISIBLE);
        }

        pColor = DB.patients().getActive(getActivity()).getColor();
        setupIcons(rootView);

        mNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final MedicinesActivity medicinesActivity = (MedicinesActivity) getActivity();
                CharSequence text = mNameTextView.getText();
                medicinesActivity.showSearchView(text != null ? text.toString() : null);
            }
        });

        mNameTextView.setCompoundDrawables(null, null, new IconicsDrawable(getActivity())
                .icon(CommunityMaterial.Icon.cmd_arrow_top_right)
                .color(pColor)
                .sizeDp(30).paddingDp(5), null);


        String none = getString(R.string.database_none_id);
        String settingUp = getString(R.string.database_setting_up);
        String value = PreferenceUtils.getString(PreferenceKeys.DRUGDB_CURRENT_DB, none);
        enableSearch = !value.equals(none) && !value.equals(settingUp);

        LogUtil.d(TAG, "Arguments:  " + (getArguments() != null) + ", savedState: " + (savedInstanceState != null));
        if (getArguments() != null) {
            mIntentAction = getArguments().getString(CalendulaApp.INTENT_EXTRA_ACTION);
            mMedicineId = getArguments().getLong(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, -1);
        }

        if (mMedicineId == -1 && savedInstanceState != null) {
            mMedicineId = savedInstanceState.getLong(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, -1);
        }

        if (mMedicineId != -1) {
            mMedicine = Medicine.findById(mMedicineId);
        }

        addPresentations(rootView);

        //setupMedPresentationChooser(rootView);

        mNameTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String name = mNameTextView.getText().toString();

                if (mPrescription != null && !dbMgr.shortName(mPrescription).toLowerCase().equals(name.toLowerCase())) {
                    mPrescription = null;
                }

            }
        });

        setupStockViews();
        if (mIntentAction == null) {
            mNameTextView.requestFocus();
        } else if ("add_stock".equals(mIntentAction)) {
            showStockDialog(DIALOG_STOCK_ADD);
        }

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (unbinder != null)
            unbinder.unbind();
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mMedicine != null) {
            setMedicne(mMedicine);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceUtils.instance().preferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceUtils.instance().preferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public boolean validate() {
        if (mNameTextView.getText() != null && mNameTextView.getText().length() > 0) {
            if (selectedPresentation == null) {
                hideKeyboard();
                Snack.show(R.string.medicine_no_presentation_error_message, getActivity());
                return false;
            }
            return true;
        } else {
            mNameTextView.setError(getString(R.string.medicine_no_name_error_message));
            mNameTextView.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                    mNameTextView.setError(null);
                    mNameTextView.removeTextChangedListener(this);
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });
            return false;
        }
    }

    public void scrollToMedPresentation(View view) {
        LogUtil.d(TAG, "Scroll to: " + view.getLeft());

        int amount = view.getLeft();
        if (amount < (0.8 * presentationScroll.getWidth())) {
            amount -= 30;
        }
        presentationScroll.smoothScrollTo(amount, 0);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMedicine != null && mMedicine.getId() != null)
            outState.putLong(CalendulaApp.INTENT_EXTRA_MEDICINE_ID, mMedicine.getId());
    }

    public void setMedicne(Medicine r) {
        LogUtil.d(TAG, "Medicine set: " + r.getName());
        mMedicine = r;
        mNameTextView.setText(mMedicine.getName());
        mPresentationTv.setText(": " + mMedicine.getPresentation().getName(getResources()));
        selectedPresentation = mMedicine.getPresentation();
        selectPresentation(mMedicine.getPresentation());

        if (r.getCn() != null) {
            Prescription p = DB.drugDB().prescriptions().findByCn(r.getCn());
            if (p != null) {
                mPrescription = p;
//                mDescriptionTv.setText(p.getName());
                new ComputeEstimatedStockEndTask().execute();
            }
        }
    }

    public void setPrescription(Prescription p) {
        mNameTextView.setText(Strings.toProperCase(dbMgr.shortName(p)));
        mPrescription = p;
        Presentation pr = DBRegistry.instance().current().expectedPresentation(p);
        if (pr != null) {
            mPresentationTv.setText(": " + pr.getName(getResources()));
            selectedPresentation = pr;
            selectPresentation(pr);
        }
    }

    public void setMedicineName(String medName) {
        mNameTextView.setText(medName);
        mPrescription = null;
    }

    public void clear() {
        mMedicine = null;
        mNameTextView.setText("");
    }

    public void onEdit() {

        String name = mNameTextView.getText().toString();

        if (name != null && name.length() > 0) {

            // if editing
            if (mMedicine != null) {
                mMedicine.setName(name);
                mMedicine.setStock(stockSwitch.isChecked() ? stock : -1);
                if (selectedPresentation != null) {
                    mMedicine.setPresentation(selectedPresentation);
                }
                if (mPrescription != null) {
                    mMedicine.setCn(String.valueOf(mPrescription.getCode()));
                    mMedicine.setDatabase(DBRegistry.instance().current().id());
                } else if (mPrescription == null) {
                    mMedicine.setCn(null);
                }

                if (mMedicineEditCallback != null && !checkIfDuplicate(mMedicine)) {
                    mMedicineEditCallback.onMedicineEdited(mMedicine);
                }
            }
            // if creating
            else {

                if (!validate()) {
                    return;
                }

                Medicine m = new Medicine(name);
                if (mPrescription != null) {
                    m.setCn(String.valueOf(mPrescription.getCode()));
                    m.setDatabase(DBRegistry.instance().current().id());
                }
                m.setStock(stockSwitch.isChecked() ? stock : -1);
                m.setPresentation(selectedPresentation != null ? selectedPresentation : Presentation.UNKNOWN);
                m.setPatient(DB.patients().getActive(getContext()));

                if (mMedicineEditCallback != null && !checkIfDuplicate(m)) {
                    mMedicineEditCallback.onMedicineCreated(m);
                }
            }
        } else {
            Snack.show(R.string.medicine_no_name_error_message, getActivity());
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        LogUtil.d(TAG, "Activity " + activity.getClass().getName() + ", " + (activity instanceof OnMedicineEditListener));
        // If the container activity has implemented
        // the callback interface, set it as listener
        if (activity instanceof OnMedicineEditListener) {
            mMedicineEditCallback = (OnMedicineEditListener) activity;
        }
        if (activity instanceof ScheduleCreationActivity) {
            this.showConfirmButton = false;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (PreferenceKeys.DRUGDB_CURRENT_DB.key().equals(key)) {
            String none = getString(R.string.database_none_id);
            String settingUp = getString(R.string.database_setting_up);
            String value = sharedPreferences.getString(PreferenceKeys.DRUGDB_CURRENT_DB.key(), none);
            enableSearch = !value.equals(none) && !value.equals(settingUp);
            if (enableSearch) {
//                enableSearchButton();
            } else {
//                searchButton.setVisibility(View.GONE);
            }
        }
    }

    public void setupIcons(View root) {
        Context c = getActivity();
        int color = R.color.dark_grey_home;
        int size = 24;
        Drawable ic1 = IconUtils.icon(c, CommunityMaterial.Icon.cmd_pencil, color, size, 4);
        Drawable ic2 = IconUtils.icon(c, CommunityMaterial.Icon.cmd_eye, color, size, 4);
        Drawable ic3 = IconUtils.icon(c, CommunityMaterial.Icon.cmd_basket, color, size, 4);
        ((ImageView) root.findViewById(R.id.ic_med_name)).setImageDrawable(ic1);
        ((ImageView) root.findViewById(R.id.ic_med_presentation)).setImageDrawable(ic2);
        ((ImageView) root.findViewById(R.id.ic_med_stock)).setImageDrawable(ic3);
    }

    @Override
    public void onDialogNumberSet(int reference, BigInteger number, double decimal, boolean isNegative, BigDecimal fullNumber) {
        float amount = fullNumber.floatValue();
        if (reference == DIALOG_STOCK_ADD) {
            stock = (stock == -1) ? amount : (stock + amount);
        } else if (reference == DIALOG_STOCK_REMOVE) {
            if (amount >= stock)
                stock = 0;
            else
                stock -= amount;
        }
        new ComputeEstimatedStockEndTask().execute();
    }

    void updateStockText() {
        String units = selectedPresentation != null ? selectedPresentation.units(getResources(), stock) : Presentation.UNKNOWN.units(getResources(), stock);
        String text = stock == -1 ? getString(R.string.no_stock_info_msg) : (stock + " " + units);
        mStockEstimation.setVisibility(estimatedStockText != null ? View.VISIBLE : View.INVISIBLE);
        mStockEstimation.setText(estimatedStockText != null ? estimatedStockText : "");
        mStockUnits.setText(text);
    }

    void showStockDialog(int ref) {
        NumberPickerBuilder npb =
                new NumberPickerBuilder()
                        .setMinNumber(BigDecimal.ONE)
                        //.setLabelText(ref == DIALOG_STOCK_ADD ? "Increase stock by" : "Decrease stock by")
                        .setDecimalVisibility(NumberPicker.VISIBLE)
                        .setPlusMinusVisibility(NumberPicker.INVISIBLE)
                        .setFragmentManager(getActivity().getSupportFragmentManager())
                        .setTargetFragment(this).setReference(ref)
                        .setStyleResId(R.style.BetterPickersDialogFragment_Calendula);
        npb.show();
    }

    void addPresentations(View rootView) {
        LinearLayout parent = (LinearLayout) rootView.findViewById(R.id.presentation_scroll_content);
        for (final Presentation p : Presentation.available()) {
            View item = getLayoutInflater().inflate(R.layout.presentation_chooser_item, null);
            ImageView imageView = (ImageView) item.findViewById(R.id.presentation_chooser_item_drawable);
            imageView.setImageDrawable(iconFor(p.icon()));
            item.setTag(p);
            parent.addView(item);
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectPresentation(p);
                    updateStockText();
                }
            });
        }
    }

    IconicsDrawable iconFor(IIcon ic) {
        return new IconicsDrawable(getContext())
                .alpha(50)
                .icon(ic)
                .colorRes(R.color.agenda_item_title)
                .paddingDp(5)
                .sizeDp(80);
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNameTextView.getWindowToken(), 0);
    }


    @OnClick(R.id.btn_stock_add)
    protected void addStock() {
        final MedicinesActivity medicinesActivity = (MedicinesActivity) getActivity();
        showStockDialog(DIALOG_STOCK_ADD);
    }

    @OnClick(R.id.btn_stock_remove)
    protected void removeStock() {
        final MedicinesActivity medicinesActivity = (MedicinesActivity) getActivity();
        showStockDialog(DIALOG_STOCK_REMOVE);
    }

    @OnClick(R.id.btn_stock_reset)
    protected void resetStock() {
        final MedicinesActivity medicinesActivity = (MedicinesActivity) getActivity();
        new MaterialStyledDialog.Builder(getContext())
                .setStyle(Style.HEADER_WITH_ICON)
                .setIcon(IconUtils.icon(getContext(), mMedicine.getPresentation().icon(), R.color.white, 100))
                .setHeaderColor(R.color.android_orange_dark)
                .withDialogAnimation(true)
                .setTitle(R.string.title_reset_stock)
                .setDescription(getString(R.string.message_reset_stock, mMedicine.getName()))
                .setCancelable(true)
                .setNegativeText(R.string.cancel)
                .setPositiveText(R.string.reset)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        LogUtil.d(TAG, "onClick: resetting stock...");
                        setDefaultStock();
                        updateStockText();
                    }
                })
                .show();
    }

    private void setupStockViews() {


        IconicsDrawable resetDrawable = new IconicsDrawable(getContext(), CommunityMaterial.Icon.cmd_reload)
                .iconOffsetXDp(2)
                .backgroundColorRes(R.color.agenda_item_title)
                .sizeDp(40)
                .paddingDp(10)
                .color(Color.WHITE)
                .roundedCornersDp(23);

        resetBtn.setImageDrawable(resetDrawable);


        stockSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                updateStockControlsVisibility();
                if (isChecked) {
                    // user is enabling stock first time
                    if (stock == -1)
                        setDefaultStock();

                    verticalScrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            verticalScrollView.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
            }
        });

        stock = mMedicine != null && mMedicine.getStock() != null ? mMedicine.getStock() : -1;

        if (stock > -1) {
            stockSwitch.setChecked(true);
        }

        updateStockControlsVisibility();
        updateStockText();
    }

    private void setDefaultStock() {
        if (mPrescription != null && mPrescription.getPackagingUnits() > 0) {
            stock = mPrescription.getPackagingUnits();
            new ComputeEstimatedStockEndTask().execute();
        }
    }

    private void updateStockControlsVisibility() {
        int visibility = stockSwitch.isChecked() ? View.VISIBLE : View.INVISIBLE;
        mStockUnits.setVisibility(visibility);
        mStockEstimation.setVisibility((stockSwitch.isChecked() && (estimatedStockText != null)) ? View.VISIBLE : View.INVISIBLE);
        addBtn.setVisibility(visibility);
        rmBtn.setVisibility(visibility);
        int resetVisibility = stockSwitch.isChecked() && mMedicine != null && mMedicine.isBoundToPrescription() ? View.VISIBLE : View.GONE;
        resetBtn.setVisibility(resetVisibility);
    }

    private void selectPresentation(Presentation p) {
        selectedPresentation = p;
        for (View v : getViewsByTag((ViewGroup) getView(), getString(R.string.presentation_item_tag))) {
            v.setBackgroundColor(getResources().getColor(R.color.transparent));
        }

        if (p != null) {
            View view = getView().findViewWithTag(p);
            if(view != null) {
                ImageView image = (ImageView) view.findViewById(R.id.presentation_chooser_item_drawable);
                image.setBackgroundResource(R.drawable.presentation_circle_background);
                mPresentationTv.setText(": " + p.getName(getResources()));
                scrollToMedPresentation(view);
            }
        }
    }

    private boolean checkIfDuplicate(final Medicine m) {
        try {
            List<Medicine> others;
            if (m.isBoundToPrescription()) {
                final String cn = m.getCn();
                final PreparedQuery<Medicine> query = DB.medicines().queryBuilder().where()
                        .eq(Medicine.COLUMN_PATIENT, m.getPatient())
                        .and().eq(Medicine.COLUMN_CN, cn).prepare();
                others = DB.medicines().query(query);
            } else {
                final String name = m.getName();
                final Presentation presentation = m.getPresentation();
                final PreparedQuery<Medicine> query = DB.medicines().queryBuilder().where()
                        .eq(Medicine.COLUMN_PATIENT, m.getPatient())
                        .and().eq(Medicine.COLUMN_NAME, name)
                        .and().eq(Medicine.COLUMN_PRESENTATION, presentation).prepare();
                others = DB.medicines().query(query);
            }
            boolean ret = false;
            if (others != null && others.size() > 0) {
                if (others.size() > 1) //should not happen
                    LogUtil.e(TAG, "checkIfDuplicate: multiple duplicates detected for medicine: " + m);
                final Medicine other = others.get(0);
                ret = !other.getId().equals(m.getId());
            }
            if (ret) {
                Snack.show(R.string.error_duplicate_medicine, this.getActivity());
            }
            return ret;
        } catch (SQLException e) {
            LogUtil.e(TAG, "checkIfDuplicate: ", e);
            return false;
        }
    }

    private void showSoftInput() {
        mNameTextView.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(mNameTextView, 0);
            }
        }, 10);

    }


    // Container Activity must implement this interface
    public interface OnMedicineEditListener {
        void onMedicineEdited(Medicine r);

        void onMedicineCreated(Medicine r);

        void onMedicineDeleted(Medicine r);
    }

    public class ComputeEstimatedStockEndTask extends AsyncTask<Void, Void, Boolean> {

        String text;

        @Override
        protected Boolean doInBackground(Void... params) {
            if (stock >= 0 && mMedicine != null) {
                LogUtil.d(TAG, "updateStockText: medicina ok");
                final StockCalculator.StockEnd stockEnd = StockCalculator.calculateStockEnd(LocalDate.now(), new MedicineScheduleStockProvider(mMedicine), stock);
                text = StockDisplayUtils.getReadableStockDuration(stockEnd, getContext());
                return true;
            }
            return false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            estimatedStockText = null;
            updateStockText();


        }

        @Override
        protected void onPostExecute(Boolean res) {
            super.onPostExecute(res);
            if (res) {
                estimatedStockText = text;
                updateStockText();
            }
        }
    }


}