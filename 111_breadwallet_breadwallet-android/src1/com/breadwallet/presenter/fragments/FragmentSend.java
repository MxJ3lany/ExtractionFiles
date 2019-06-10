package com.breadwallet.presenter.fragments;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.breadwallet.BuildConfig;
import com.breadwallet.R;
import com.breadwallet.ui.wallet.WalletActivity;
import com.breadwallet.presenter.customviews.BRButton;
import com.breadwallet.presenter.customviews.BRDialogView;
import com.breadwallet.presenter.customviews.BRKeyboard;
import com.breadwallet.presenter.customviews.BRLinearLayoutWithCaret;
import com.breadwallet.presenter.customviews.BaseTextView;
import com.breadwallet.presenter.entities.CryptoRequest;
import com.breadwallet.presenter.fragments.utils.ModalDialogFragment;
import com.breadwallet.presenter.viewmodels.SendViewModel;
import com.breadwallet.tools.animation.UiUtils;
import com.breadwallet.tools.animation.BRDialog;
import com.breadwallet.tools.animation.SlideDetector;
import com.breadwallet.tools.animation.SpringAnimator;
import com.breadwallet.tools.manager.BRClipboardManager;
import com.breadwallet.tools.manager.BRReportsManager;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.manager.SendManager;
import com.breadwallet.tools.threads.executor.BRExecutor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.CurrencyUtils;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.WalletsMaster;
import com.breadwallet.wallet.util.CryptoUriParser;
import com.breadwallet.wallet.abstracts.BaseWalletManager;
import com.breadwallet.wallet.wallets.ethereum.WalletEthManager;
import com.platform.HTTPServer;

import java.math.BigDecimal;


/**
 * BreadWallet
 * <p>
 * Created by Mihail Gutan <mihail@breadwallet.com> on 6/29/15.
 * Copyright (c) 2016 breadwallet LLC
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

public class FragmentSend extends ModalDialogFragment implements BRKeyboard.OnInsertListener {
    private static final String TAG = FragmentSend.class.getName();

    private BRKeyboard mKeyboard;
    private EditText mAddressEdit;
    private Button mScan;
    private Button mPaste;
    private Button mSend;
    private EditText mCommentEdit;
    private TextView mCurrencyCode;
    private EditText mAmountEdit;
    private TextView mBalanceText;
    private TextView mFeeText;
    private ImageView mEditFeeIcon;
    private String mSelectedCurrencyCode;
    private Button mCurrencyCodeButton;
    private int mKeyboardIndex;
    private LinearLayout mKeyboardLayout;
    private ImageButton mCloseButton;
    private ConstraintLayout mAmountLayout;
    private BRButton mRegularFeeButton;
    private BRButton mEconomyFeeButton;
    private BRLinearLayoutWithCaret mFeeLayout;
    private boolean mIsFeeButtonsShown = false;
    private BaseTextView mFeeDescription;
    private BaseTextView mEconomyFeeWarningText;
    private boolean mIsAmountLabelShown = true;
    private static final int CURRENCY_CODE_TEXT_SIZE = 18;
    private SendViewModel mViewModel;
    private ViewGroup mBackgroundLayout;
    private ViewGroup mSignalLayout;
    private static boolean mIsSendShown;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = assignRootView((ViewGroup) inflater.inflate(R.layout.fragment_send, container, false));
        mBackgroundLayout = assignBackgroundLayout((ViewGroup) rootView.findViewById(R.id.background_layout));
        mSignalLayout = assignSignalLayout((ViewGroup) rootView.findViewById(R.id.signal_layout));
        mKeyboard = rootView.findViewById(R.id.keyboard);
        mKeyboard.setBRButtonBackgroundResId(R.drawable.keyboard_white_button);
        mKeyboard.setBRKeyboardColor(R.color.white);
        mKeyboard.setDeleteImage(R.drawable.ic_delete_gray);
        mCurrencyCode = rootView.findViewById(R.id.iso_text);
        mAddressEdit = rootView.findViewById(R.id.address_edit);
        mScan = rootView.findViewById(R.id.scan);
        mPaste = rootView.findViewById(R.id.paste_button);
        mSend = rootView.findViewById(R.id.send_button);
        mCommentEdit = rootView.findViewById(R.id.comment_edit);
        mAmountEdit = rootView.findViewById(R.id.amount_edit);
        mBalanceText = rootView.findViewById(R.id.balance_text);
        mFeeText = rootView.findViewById(R.id.fee_text);
        mEditFeeIcon = rootView.findViewById(R.id.edit);
        mCurrencyCodeButton = rootView.findViewById(R.id.iso_button);
        mKeyboardLayout = rootView.findViewById(R.id.keyboard_layout);
        mAmountLayout = rootView.findViewById(R.id.amount_layout);
        mFeeLayout = rootView.findViewById(R.id.fee_buttons_layout);
        mFeeDescription = rootView.findViewById(R.id.fee_description);
        mEconomyFeeWarningText = rootView.findViewById(R.id.warning_text);

        mRegularFeeButton = rootView.findViewById(R.id.left_button);
        mEconomyFeeButton = rootView.findViewById(R.id.right_button);
        mCloseButton = rootView.findViewById(R.id.close_button);
        BaseWalletManager wm = WalletsMaster.getInstance().getCurrentWallet(getActivity());
        mSelectedCurrencyCode = BRSharedPrefs.isCryptoPreferred(getActivity()) ? wm.getCurrencyCode() : BRSharedPrefs.getPreferredFiatIso(getContext());

        mViewModel = ViewModelProviders.of(this).get(SendViewModel.class);

        setListeners();
        mCurrencyCode.setText(getString(R.string.Send_amountLabel));
        mCurrencyCode.setTextSize(CURRENCY_CODE_TEXT_SIZE);
        mCurrencyCode.setTextColor(getContext().getColor(R.color.light_gray));
        mCurrencyCode.requestLayout();
        mSignalLayout.setOnTouchListener(new SlideDetector(getContext(), mSignalLayout));

        mSignalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        showFeeSelectionButtons(mIsFeeButtonsShown);

        mEditFeeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsFeeButtonsShown = !mIsFeeButtonsShown;
                showFeeSelectionButtons(mIsFeeButtonsShown);
            }
        });
        mKeyboardIndex = mSignalLayout.indexOfChild(mKeyboardLayout);

        ImageButton faq = rootView.findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                Activity app = getActivity();
                if (app == null) {
                    Log.e(TAG, "onClick: app is null, can't start the webview with url: "
                            + HTTPServer.getPlatformUrl(HTTPServer.URL_SUPPORT));
                    return;
                }
                BaseWalletManager wm = WalletsMaster.getInstance().getCurrentWallet(app);
                UiUtils.showSupportFragment((FragmentActivity) app, BRConstants.FAQ_SEND, wm);
            }
        });

        showKeyboard(false);
        setButton(true);

        mSignalLayout.setLayoutTransition(UiUtils.getDefaultTransition());

        return rootView;
    }

    private void startEditingAmount() {
        if (mIsAmountLabelShown) { //only first time
            mIsAmountLabelShown = false;
            mAmountEdit.setHint("0");
            mAmountEdit.setTextSize(getResources().getDimension(R.dimen.amount_text_size));
            mBalanceText.setVisibility(View.VISIBLE);
            mEditFeeIcon.setVisibility(View.VISIBLE);
            mAmountEdit.setVisibility(View.VISIBLE);
            mFeeText.setVisibility(View.VISIBLE);
            mCurrencyCode.setTextColor(getContext().getColor(R.color.almost_black));
            mCurrencyCode.setText(CurrencyUtils.getSymbolByIso(getActivity(), mSelectedCurrencyCode));
            mCurrencyCode.setTextSize(getResources().getDimension(R.dimen.currency_code_text_size_large));

            ConstraintSet set = new ConstraintSet();
            set.clone(mAmountLayout);

            int px4 = Utils.getPixelsFromDps(getContext(), 4);
            set.connect(mBalanceText.getId(), ConstraintSet.TOP, mCurrencyCode.getId(), ConstraintSet.BOTTOM, px4);
            set.connect(mFeeText.getId(), ConstraintSet.TOP, mBalanceText.getId(), ConstraintSet.BOTTOM, px4);
            set.connect(mFeeText.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, px4);
            set.connect(mCurrencyCode.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, px4);
            set.connect(mCurrencyCode.getId(), ConstraintSet.BOTTOM, -1, ConstraintSet.TOP, -1);
            set.applyTo(mAmountLayout);
        }
    }

    private void setListeners() {
        mAmountEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showKeyboard(true);
                startEditingAmount();
            }
        });

        //needed to fix the overlap bug
        mCommentEdit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    mAmountLayout.requestLayout();
                    return true;
                }
                return false;
            }
        });

        mCommentEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                showKeyboard(!hasFocus);
            }
        });

        mPaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                String theUrl = BRClipboardManager.getClipboard(getActivity());
                if (Utils.isNullOrEmpty(theUrl)) {
                    sayClipboardEmpty();
                    return;
                }
                showKeyboard(false);

                final BaseWalletManager wm = WalletsMaster.getInstance().getCurrentWallet(getActivity());


                if (BuildConfig.DEBUG && BuildConfig.BITCOIN_TESTNET) {
                    theUrl = wm.decorateAddress(theUrl);
                }

                final CryptoRequest obj = CryptoUriParser.parseRequest(getActivity(), theUrl);

                if (obj == null || Utils.isNullOrEmpty(obj.getAddress())) {
                    sayInvalidClipboardData();
                    return;
                }
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Send Address -> " + obj.getAddress());
                    Log.d(TAG, "Send Value -> " + obj.getValue());
                    Log.d(TAG, "Send Amount -> " + obj.getAmount());
                }

                if (obj.getCurrencyCode() != null && !obj.getCurrencyCode().equalsIgnoreCase(wm.getCurrencyCode())) {
                    sayInvalidAddress(); //invalid if the screen is Bitcoin and scanning BitcoinCash for instance
                    return;
                }


                if (wm.isAddressValid(obj.getAddress())) {
                    final Activity app = getActivity();
                    if (app == null) {
                        Log.e(TAG, "mPaste onClick: app is null");
                        return;
                    }
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (wm.containsAddress(obj.getAddress())) {
                                app.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        BRDialog.showCustomDialog(getActivity(), "", getResources().getString(R.string.Send_containsAddress),
                                                getResources().getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                                    @Override
                                                    public void onClick(BRDialogView brDialogView) {
                                                        brDialogView.dismiss();
                                                    }
                                                }, null, null, 0);
                                        BRClipboardManager.putClipboard(getActivity(), "");
                                    }
                                });

                            } else if (wm.addressIsUsed(obj.getAddress())) {
                                app.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String title = String.format("%1$s addresses are intended for single use only.", wm.getName());
                                        BRDialog.showCustomDialog(getActivity(), title, getString(R.string.Send_UsedAddress_secondLIne),
                                                "Ignore", "Cancel", new BRDialogView.BROnClickListener() {
                                                    @Override
                                                    public void onClick(BRDialogView brDialogView) {
                                                        brDialogView.dismiss();
                                                        mAddressEdit.setText(wm.decorateAddress(obj.getAddress()));
                                                    }
                                                }, new BRDialogView.BROnClickListener() {
                                                    @Override
                                                    public void onClick(BRDialogView brDialogView) {
                                                        brDialogView.dismiss();
                                                    }
                                                }, null, 0);
                                    }
                                });

                            } else {
                                app.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAddressEdit.setText(wm.decorateAddress(obj.getAddress()));

                                    }
                                });
                            }
                        }
                    });

                } else {
                    sayInvalidClipboardData();
                }

            }
        });

        mCurrencyCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedCurrencyCode.equalsIgnoreCase(BRSharedPrefs.getPreferredFiatIso(getContext()))) {
                    Activity app = getActivity();
                    mSelectedCurrencyCode = WalletsMaster.getInstance().getCurrentWallet(app).getCurrencyCode();
                } else {
                    mSelectedCurrencyCode = BRSharedPrefs.getPreferredFiatIso(getContext());
                }
                updateText();

            }
        });

        mScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                saveViewModelData();
                closeWithAnimation();
                UiUtils.openScanner(getActivity());

            }
        });
        mSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //not allowed now
                if (!UiUtils.isClickAllowed()) return;
                WalletsMaster master = WalletsMaster.getInstance();
                final BaseWalletManager wm = master.getCurrentWallet(getActivity());
                //get the current wallet used
                if (wm == null) {
                    Log.e(TAG, "onClick: Wallet is null and it can't happen.");
                    BRReportsManager.reportBug(new NullPointerException("Wallet is null and it can't happen."), true);
                    return;
                }
                boolean allFilled = true;
                String rawAddress = mAddressEdit.getText().toString();
                String amountStr = mViewModel.getAmount();
                String comment = mCommentEdit.getText().toString();

                //inserted amount
                BigDecimal rawAmount = new BigDecimal(Utils.isNullOrEmpty(amountStr) || amountStr.equalsIgnoreCase(".") ? "0" : amountStr);
                //is the chosen ISO a crypto (could be a fiat currency)
                boolean isIsoCrypto = master.isIsoCrypto(getActivity(), mSelectedCurrencyCode);

                BigDecimal cryptoAmount = isIsoCrypto ? wm.getSmallestCryptoForCrypto(getActivity(), rawAmount) : wm.getSmallestCryptoForFiat(getActivity(), rawAmount);

                CryptoRequest req = CryptoUriParser.parseRequest(getActivity(), rawAddress);
                if (req == null || Utils.isNullOrEmpty(req.getAddress())) {
                    sayInvalidClipboardData();
                    return;
                }
                final Activity app = getActivity();
                if (!wm.isAddressValid(req.getAddress())) {

                    BRDialog.showCustomDialog(app, app.getString(R.string.Alert_error), app.getString(R.string.Send_noAddress),
                            app.getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                                @Override
                                public void onClick(BRDialogView brDialogView) {
                                    brDialogView.dismissWithAnimation();
                                }
                            }, null, null, 0);
                    return;
                }
                if (cryptoAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    allFilled = false;
                    SpringAnimator.failShakeAnimation(getActivity(), mAmountEdit);
                }

                if (cryptoAmount.compareTo(wm.getBalance()) > 0) {
                    allFilled = false;
                    SpringAnimator.failShakeAnimation(getActivity(), mBalanceText);
                    SpringAnimator.failShakeAnimation(getActivity(), mFeeText);
                }

                if (WalletsMaster.getInstance().isCurrencyCodeErc20(getActivity(), wm.getCurrencyCode())) {

                    BigDecimal rawFee = wm.getEstimatedFee(cryptoAmount, mAddressEdit.getText().toString());
                    BaseWalletManager ethWm = WalletEthManager.getInstance(app);
                    BigDecimal balance = ethWm.getBalance();
                    if (rawFee.compareTo(balance) > 0) {
                        if (allFilled) {
                            BigDecimal ethVal = ethWm.getCryptoForSmallestCrypto(app, rawFee);
                            sayInsufficientEthereumForFee(ethVal.setScale(ethWm.getMaxDecimalPlaces(app), BRConstants.ROUNDING_MODE).toPlainString());
                            allFilled = false;
                        }
                    }
                }

                if (allFilled) {
                    final CryptoRequest item = new CryptoRequest.Builder().setAddress(req.getAddress()).setAmount(cryptoAmount).setMessage(comment).build();
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            SendManager.sendTransaction(getActivity(), item, wm, null);

                        }
                    });

                    closeWithAnimation();
                }
            }
        });

        mBackgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                getActivity().onBackPressed();
            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.clear();
                closeWithAnimation();
            }
        });

        mAddressEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE) || (actionId == EditorInfo.IME_ACTION_NEXT)) {
                    Utils.hideKeyboard(getActivity());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showKeyboard(true);
                        }
                    }, 500);

                }
                return false;
            }
        });

        mAddressEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                showKeyboard(!hasFocus);
            }
        });

        mKeyboard.setOnInsertListener(this);

        mRegularFeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(true);
            }
        });
        mEconomyFeeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setButton(false);
            }
        });

    }

    private void showKeyboard(boolean b) {
        if (!b) {
            mSignalLayout.removeView(mKeyboardLayout);

        } else {
            Utils.hideKeyboard(getActivity());
            if (mSignalLayout.indexOfChild(mKeyboardLayout) == -1) {
                mSignalLayout.addView(mKeyboardLayout, mKeyboardIndex);
            } else {
                mSignalLayout.removeView(mKeyboardLayout);
            }

        }
    }

    private void sayClipboardEmpty() {
        BRDialog.showCustomDialog(getActivity(), "", getResources().getString(R.string.Send_emptyPasteboard),
                getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
    }

    private void sayInvalidClipboardData() {
        BRDialog.showCustomDialog(getActivity(), "", getResources().getString(R.string.Send_invalidAddressTitle),
                getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
    }

    private void saySomethingWentWrong() {
        BRDialog.showCustomDialog(getActivity(), "", "Something went wrong.",
                getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {

                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
    }

    private void sayInvalidAddress() {
        BRDialog.showCustomDialog(getActivity(), "", String.format(getResources().getString(R.string.Send_invalidAddressMessage), BRSharedPrefs.getCurrentWalletCurrencyCode(getActivity())),
                getString(R.string.AccessibilityLabels_close), null, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismiss();
                    }
                }, null, null, 0);
    }

    private void sayInsufficientEthereumForFee(String ethNeeded) {
        String message = String.format(getActivity().getString(R.string.Send_insufficientGasMessage), ethNeeded);
        BRDialog.showCustomDialog(getActivity(), getActivity().getString(R.string.Send_insufficientGasTitle), message, getActivity().getString(R.string.Button_continueAction),
                getActivity().getString(R.string.Button_cancel), new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                        getActivity().getFragmentManager().popBackStack();
                    }
                }, new BRDialogView.BROnClickListener() {
                    @Override
                    public void onClick(BRDialogView brDialogView) {
                        brDialogView.dismissWithAnimation();
                    }
                }, null, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
        mIsAmountLabelShown = true;
        loadParameters();
        loadViewModelData();
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity());
        setIsSendShown(false);
    }

    private void loadParameters() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            CryptoRequest request = (CryptoRequest) arguments.getSerializable(WalletActivity.EXTRA_CRYPTO_REQUEST);
            if (request != null) {
                saveViewModelData(getActivity(), request);
            }

        }
    }

    private void handleClick(String key) {
        if (key == null) {
            Log.e(TAG, "handleClick: key is null! ");
            return;
        }

        if (key.isEmpty()) {
            handleDeleteClick();
        } else if (Character.isDigit(key.charAt(0))) {
            handleDigitClick(Integer.parseInt(key.substring(0, 1)));
        } else if (key.charAt(0) == '.') {
            handleSeparatorClick();
        }
    }

    private void handleDigitClick(Integer digit) {
        String currAmount = mViewModel.getAmount();
        BaseWalletManager wm = WalletsMaster.getInstance().getCurrentWallet(getActivity());
        if (new BigDecimal(currAmount.concat(String.valueOf(digit))).compareTo(wm.getMaxAmount(getActivity())) <= 0) {
            //do not insert 0 if the balance is 0 now
            if (currAmount.equalsIgnoreCase("0")) {
                mViewModel.setAmount("");
            }
            boolean isDigitLimitReached = (currAmount.length() - currAmount.indexOf(".") > CurrencyUtils.getMaxDecimalPlaces(getActivity(), mSelectedCurrencyCode));
            if ((currAmount.contains(".") && isDigitLimitReached)) {
                return;
            }
            mViewModel.setAmount(mViewModel.getAmount() + digit);
            updateText();
        }
    }

    private void handleSeparatorClick() {
        String currAmount = mViewModel.getAmount();
        if (currAmount.contains(".") || CurrencyUtils.getMaxDecimalPlaces(getActivity(), mSelectedCurrencyCode) == 0)
            return;
        mViewModel.setAmount(mViewModel.getAmount() + ".");
        updateText();
    }

    private void handleDeleteClick() {
        String currAmount = mViewModel.getAmount();
        if (currAmount.length() > 0) {
            currAmount = currAmount.substring(0, currAmount.length() - 1);
            mViewModel.setAmount(currAmount);
            updateText();
        }

    }

    private void updateText() {
        Context context = getContext();
        if (context == null) return;

        String stringAmount = mViewModel.getAmount();
        setAmount();
        BaseWalletManager wm = WalletsMaster.getInstance().getCurrentWallet(context);
        String balanceString;
        if (mSelectedCurrencyCode == null)
            mSelectedCurrencyCode = wm.getCurrencyCode();
        BigDecimal mCurrentBalance = wm.getBalance();
        if (!mIsAmountLabelShown) {
            mCurrencyCode.setText(CurrencyUtils.getSymbolByIso(context, mSelectedCurrencyCode));
        }
        mCurrencyCodeButton.setText(mSelectedCurrencyCode);

        //is the chosen ISO a crypto (could be also a fiat currency)
        boolean isIsoCrypto = WalletsMaster.getInstance().isIsoCrypto(context, mSelectedCurrencyCode);
        boolean isWalletErc20 = WalletsMaster.getInstance().isCurrencyCodeErc20(context, wm.getCurrencyCode());
        BigDecimal inputAmount = new BigDecimal(Utils.isNullOrEmpty(stringAmount) || stringAmount.equalsIgnoreCase(".") ? "0" : stringAmount);

        //smallest crypto e.g. satoshis
        BigDecimal cryptoAmount = isIsoCrypto ? wm.getSmallestCryptoForCrypto(context, inputAmount) : wm.getSmallestCryptoForFiat(context, inputAmount);

        //wallet's balance for the selected ISO
        BigDecimal isoBalance = isIsoCrypto ? wm.getCryptoForSmallestCrypto(context, mCurrentBalance) : wm.getFiatForSmallestCrypto(context, mCurrentBalance, null);
        if (isoBalance == null) isoBalance = BigDecimal.ZERO;

        BigDecimal rawFee = wm.getEstimatedFee(cryptoAmount, mAddressEdit.getText().toString());

        //get the fee for iso (dollars, bits, BTC..)
        BigDecimal isoFee = isIsoCrypto ? rawFee : wm.getFiatForSmallestCrypto(context, rawFee, null);

        //format the fee to the selected ISO
        String formattedFee = CurrencyUtils.getFormattedAmount(context, mSelectedCurrencyCode, isoFee);

        if (isWalletErc20) {
            BaseWalletManager ethWm = WalletEthManager.getInstance(context);
            isoFee = isIsoCrypto ? rawFee : ethWm.getFiatForSmallestCrypto(context, rawFee, null);
            formattedFee = CurrencyUtils.getFormattedAmount(context, isIsoCrypto ? ethWm.getCurrencyCode() : mSelectedCurrencyCode, isoFee);
        }

        boolean isOverTheBalance = inputAmount.compareTo(isoBalance) > 0;

        if (isOverTheBalance) {
            mBalanceText.setTextColor(getContext().getColor(R.color.warning_color));
            mFeeText.setTextColor(getContext().getColor(R.color.warning_color));
            mAmountEdit.setTextColor(getContext().getColor(R.color.warning_color));
            if (!mIsAmountLabelShown)
                mCurrencyCode.setTextColor(getContext().getColor(R.color.warning_color));
        } else {
            mBalanceText.setTextColor(getContext().getColor(R.color.light_gray));
            mFeeText.setTextColor(getContext().getColor(R.color.light_gray));
            mAmountEdit.setTextColor(getContext().getColor(R.color.almost_black));
            if (!mIsAmountLabelShown)
                mCurrencyCode.setTextColor(getContext().getColor(R.color.almost_black));
        }
        //formattedBalance
        String formattedBalance = CurrencyUtils.getFormattedAmount(context, mSelectedCurrencyCode,
                isIsoCrypto ? wm.getSmallestCryptoForCrypto(context, isoBalance) : isoBalance);
        balanceString = String.format(getString(R.string.Send_balance), formattedBalance);
        mBalanceText.setText(balanceString);
        mFeeText.setText(String.format(getString(R.string.Send_fee), formattedFee));
        mAmountLayout.requestLayout();
    }

    private void showFeeSelectionButtons(boolean b) {
        if (!b) {
            mSignalLayout.removeView(mFeeLayout);
        } else {
            mSignalLayout.addView(mFeeLayout, mSignalLayout.indexOfChild(mAmountLayout) + 1);

        }
    }

    private void setAmount() {
        String cryptoAmount = mViewModel.getAmount();

        int divider = cryptoAmount.length();
        if (cryptoAmount.contains(".")) {
            divider = cryptoAmount.indexOf(".");
        }
        StringBuilder newAmount = new StringBuilder();
        for (int i = 0; i < cryptoAmount.length(); i++) {
            newAmount.append(cryptoAmount.charAt(i));
            if (divider > 3 && divider - 1 != i && divider > i && ((divider - i - 1) % 3 == 0)) {
                newAmount.append(",");
            }
        }
        if (Utils.isNumber(cryptoAmount) && new BigDecimal(cryptoAmount).compareTo(BigDecimal.ZERO) > 0) {
            startEditingAmount();
        }
        mAmountEdit.setText(newAmount.toString());
    }

    private void setButton(boolean isRegular) {
        BaseWalletManager wallet = WalletsMaster.getInstance().getCurrentWallet(getActivity());
        String iso = wallet.getCurrencyCode();
        if (isRegular) {
            BRSharedPrefs.putFavorStandardFee(getActivity(), iso, true);
            mRegularFeeButton.setTextColor(getContext().getColor(R.color.white));
            mRegularFeeButton.setBackground(getContext().getDrawable(R.drawable.b_half_left_blue));
            mEconomyFeeButton.setTextColor(getContext().getColor(R.color.dark_blue));
            mEconomyFeeButton.setBackground(getContext().getDrawable(R.drawable.b_half_right_blue_stroke));
            mFeeDescription.setText(String.format(getString(R.string.FeeSelector_estimatedDeliver), getString(R.string.FeeSelector_regularTime)));
            mEconomyFeeWarningText.getLayoutParams().height = 0;
        } else {
            BRSharedPrefs.putFavorStandardFee(getActivity(), iso, false);
            mRegularFeeButton.setTextColor(getContext().getColor(R.color.dark_blue));
            mRegularFeeButton.setBackground(getContext().getDrawable(R.drawable.b_half_left_blue_stroke));
            mEconomyFeeButton.setTextColor(getContext().getColor(R.color.white));
            mEconomyFeeButton.setBackground(getContext().getDrawable(R.drawable.b_half_right_blue));
            mFeeDescription.setText(String.format(getString(R.string.FeeSelector_estimatedDeliver), getString(R.string.FeeSelector_economyTime)));
            mEconomyFeeWarningText.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
        mEconomyFeeWarningText.requestLayout();
        updateText();
    }

    // from the link above
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks whether a hardware mKeyboard is available
        if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            Log.e(TAG, "onConfigurationChanged: hidden");
            showKeyboard(true);
        } else if (newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES) {
            Log.e(TAG, "onConfigurationChanged: shown");
            showKeyboard(false);
        }
    }

    public void loadViewModelData() {
        if (mAddressEdit != null) {
            if (!Utils.isNullOrEmpty(mViewModel.getAddress())) {
                BaseWalletManager walletManager = WalletsMaster.getInstance().getCurrentWallet(getActivity());
                mAddressEdit.setText(walletManager.decorateAddress(mViewModel.getAddress()));
            }
            if (!Utils.isNullOrEmpty(mViewModel.getMemo())) {
                mCommentEdit.setText(mViewModel.getMemo());
            }
            if (!Utils.isNullOrEmpty(mViewModel.getChosenCode())) {
                mSelectedCurrencyCode = mViewModel.getChosenCode().toUpperCase();
            }
            updateText();
        }
    }

    public void saveViewModelData(Context context, CryptoRequest request) {
        updateViewModel();
        String address = null;
        String code = null;
        String amount = null;
        String memo = null;
        if (request == null) {
            if (mCommentEdit != null) {
                memo = mCommentEdit.getText().toString();
                address = mAddressEdit.getText().toString();
                code = mSelectedCurrencyCode;
            }
        } else {
            address = request.getAddress();
            memo = request.getMessage();
            code = request.getCurrencyCode();
            BaseWalletManager walletManager = WalletsMaster.getInstance().getCurrentWallet(context);
            if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0 && !request.getCurrencyCode().equalsIgnoreCase(WalletEthManager.ETH_CURRENCY_CODE)) {
                // Crypto request amount param is named `amount` and it is in bitcoin and other currencies.
                amount = walletManager.getCryptoForSmallestCrypto(context, new BigDecimal(request.getAmount().toPlainString())).toPlainString();
            } else if (request.getValue() != null && request.getValue().compareTo(BigDecimal.ZERO) > 0 && request.getCurrencyCode().equalsIgnoreCase(WalletEthManager.ETH_CURRENCY_CODE)) {
                // ETH request amount param is named `value` and it is in ether.
                amount = walletManager.getCryptoForSmallestCrypto(context, new BigDecimal(request.getValue().toPlainString())).toPlainString();
            }

        }
        if (!Utils.isNullOrEmpty(address)) {
            mViewModel.setAddress(address);
        }
        if (!Utils.isNullOrEmpty(memo)) {
            mViewModel.setMemo(memo);
        }
        if (!Utils.isNullOrEmpty(code)) {
            mViewModel.setChosenCode(code);
        }
        if (!Utils.isNullOrEmpty(amount)) {
            mViewModel.setAmount(amount);
        }
        loadViewModelData();
    }

    private void updateViewModel() {
        if (mViewModel == null) {
            mViewModel = ViewModelProviders.of(this).get(SendViewModel.class);
        }
    }

    public void saveViewModelData() {
        saveViewModelData(getActivity(), null);
    }

    @Override
    public void onKeyInsert(String key) {
        handleClick(key);
    }

    public static boolean isIsSendShown() {
        return mIsSendShown;
    }

    public static void setIsSendShown(boolean isSendShown) {
        mIsSendShown = isSendShown;
    }
}
