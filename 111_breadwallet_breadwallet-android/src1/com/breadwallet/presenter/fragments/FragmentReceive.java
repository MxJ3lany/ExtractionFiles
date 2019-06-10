package com.breadwallet.presenter.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.breadwallet.BuildConfig;
import com.breadwallet.R;
import com.breadwallet.presenter.customviews.BRButton;
import com.breadwallet.presenter.customviews.BRKeyboard;
import com.breadwallet.presenter.customviews.BRLinearLayoutWithCaret;
import com.breadwallet.presenter.entities.CryptoRequest;
import com.breadwallet.presenter.fragments.utils.ModalDialogFragment;
import com.breadwallet.tools.animation.SlideDetector;
import com.breadwallet.tools.animation.UiUtils;
import com.breadwallet.tools.manager.BRClipboardManager;
import com.breadwallet.tools.qrcode.QRUtils;
import com.breadwallet.tools.threads.executor.BRExecutor;
import com.breadwallet.tools.util.BRConstants;
import com.breadwallet.tools.util.EventUtils;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.WalletsMaster;
import com.breadwallet.wallet.abstracts.BaseWalletManager;
import com.breadwallet.wallet.abstracts.BalanceUpdateListener;
import com.breadwallet.wallet.util.CryptoUriParser;
import com.platform.HTTPServer;

import java.math.BigDecimal;
import java.util.Map;


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

public class FragmentReceive extends ModalDialogFragment implements BalanceUpdateListener {
    private static final String TAG = FragmentReceive.class.getName();

    public static final String EXTRA_RECEIVE = "com.breadwallet.presenter.fragments.FragmentReceive.EXTRA_RECEIVE";
    public TextView mTitle;
    public TextView mAddress;
    public ImageView mQrImage;
    private String mReceiveAddress;
    private View mSeparatorRequestView;
    private BRButton mShareButton;
    private Button mRequestButton;
    private BRLinearLayoutWithCaret mCopiedLayout;
    private boolean mShowRequestAnAmount;
    private ImageButton mCloseButton;
    private Handler mCopyHandler = new Handler();
    private BRKeyboard mKeyboard;
    private View mSeparatorHeaderView;
    private ViewGroup mBackgroundLayout;
    private ViewGroup mSignalLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.

        ViewGroup rootView = assignRootView((ViewGroup) inflater.inflate(R.layout.fragment_receive, container, false));
        mBackgroundLayout = assignBackgroundLayout((ViewGroup) rootView.findViewById(R.id.background_layout));
        mSignalLayout = assignSignalLayout((ViewGroup) rootView.findViewById(R.id.signal_layout));
        mTitle = rootView.findViewById(R.id.title);
        mAddress = rootView.findViewById(R.id.address_text);
        mQrImage = rootView.findViewById(R.id.qr_image);
        mShareButton = rootView.findViewById(R.id.share_button);
        mCopiedLayout = rootView.findViewById(R.id.copied_layout);
        mRequestButton = rootView.findViewById(R.id.request_button);
        mKeyboard = rootView.findViewById(R.id.keyboard);
        mKeyboard.setBRButtonBackgroundResId(R.drawable.keyboard_white_button);
        mKeyboard.setDeleteImage(R.drawable.ic_delete_gray);
        mKeyboard.setBRKeyboardColor(R.color.white);
        mSeparatorRequestView = rootView.findViewById(R.id.separator);
        mCloseButton = rootView.findViewById(R.id.close_button);
        mSeparatorHeaderView = rootView.findViewById(R.id.separator2);
        mSeparatorHeaderView.setVisibility(View.GONE);
        setListeners();

        ImageButton faq = rootView.findViewById(R.id.faq_button);

        faq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Activity app = getActivity();
                if (app == null) {
                    Log.e(TAG, "onClick: app is null, can't start the webview with url: "
                            + HTTPServer.getPlatformUrl(HTTPServer.URL_SUPPORT));
                    return;
                }

                BaseWalletManager wm = WalletsMaster.getInstance().getCurrentWallet(app);
                UiUtils.showSupportFragment((FragmentActivity) app, BRConstants.FAQ_RECEIVE, wm);
            }
        });

        mSignalLayout.removeView(mCopiedLayout);

        mSignalLayout.setLayoutTransition(UiUtils.getDefaultTransition());

        mSignalLayout.setOnTouchListener(new SlideDetector(getContext(), mSignalLayout));

        return rootView;
    }

    private void setListeners() {
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) {
                    return;
                }
                BaseWalletManager walletManager = WalletsMaster.getInstance().getCurrentWallet(getActivity());
                CryptoRequest cryptoRequest = new CryptoRequest.Builder().setAddress(walletManager.decorateAddress(mReceiveAddress)).setAmount(BigDecimal.ZERO).build();
                Uri cryptoUri = CryptoUriParser.createCryptoUrl(getActivity(), walletManager, cryptoRequest);
                QRUtils.sendShareIntent(getActivity(), cryptoUri.toString(), cryptoRequest.getAddress());
            }
        });
        mAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                copyText();
            }
        });
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                Activity app = getActivity();
                app.getFragmentManager().popBackStack();
                UiUtils.showRequestFragment((FragmentActivity) app);
            }
        });

        mBackgroundLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                getActivity().onBackPressed();
            }
        });
        mQrImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!UiUtils.isClickAllowed()) return;
                copyText();
            }
        });

        mCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeWithAnimation();
            }
        });
    }

    private void showCopiedLayout(boolean b) {
        if (!b) {
            mSignalLayout.removeView(mCopiedLayout);
            mCopyHandler.removeCallbacksAndMessages(null);
        } else {
            if (mSignalLayout.indexOfChild(mCopiedLayout) == -1) {
                mSignalLayout.addView(mCopiedLayout, mSignalLayout.indexOfChild(mShareButton));
                mCopyHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSignalLayout.removeView(mCopiedLayout);
                    }
                }, DateUtils.SECOND_IN_MILLIS * 2);
            } else {
                mCopyHandler.removeCallbacksAndMessages(null);
                mSignalLayout.removeView(mCopiedLayout);
            }
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        BaseWalletManager wm = WalletsMaster.getInstance().getCurrentWallet(getActivity());
        boolean isReceive = getArguments().getBoolean(EXTRA_RECEIVE);
        mShowRequestAnAmount = isReceive && wm.getUiConfiguration().isShowRequestedAmount();
        if (!mShowRequestAnAmount) {
            mSignalLayout.removeView(mSeparatorRequestView);
            mSignalLayout.removeView(mRequestButton);
            mTitle.setText(getString(R.string.UnlockScreen_myAddress));
        }
        updateQr();
    }

    private void updateQr() {
        BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                final BaseWalletManager walletManager = WalletsMaster.getInstance().getCurrentWallet(getContext());
                walletManager.refreshAddress(getContext());
                BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        mReceiveAddress = walletManager.getAddress(getContext());
                        String decoratedReceiveAddress = walletManager.decorateAddress(mReceiveAddress);
                        mAddress.setText(decoratedReceiveAddress);
                        Utils.correctTextSizeIfNeeded(mAddress);
                        Uri uri = CryptoUriParser.createCryptoUrl(getActivity(), walletManager, new CryptoRequest.Builder().setAddress(decoratedReceiveAddress).build());
                        boolean generated = QRUtils.generateQR(getActivity(), uri.toString(), mQrImage);
                        if (!QRUtils.generateQR(getContext(), uri.toString(), mQrImage)) {
                            throw new RuntimeException("failed to generate qr image for address");
                        }
                    }
                });
            }
        });
    }

    private void copyText() {
        Activity app = getActivity();
        BRClipboardManager.putClipboard(app, mAddress.getText().toString());
        EventUtils.pushEvent(EventUtils.EVENT_RECEIVE_COPIED_ADDRESS);
        // The testnet does not work with the BCH address format so copy the legacy address for testing purposes.
        if (BuildConfig.BITCOIN_TESTNET) {
            BRClipboardManager.putClipboard(app, WalletsMaster.getInstance().getCurrentWallet(app).undecorateAddress(mAddress.getText().toString()));
        }

        showCopiedLayout(true);
    }


    @Override
    public void onResume() {
        super.onResume();
        WalletsMaster.getInstance().getCurrentWallet(getActivity()).addBalanceChangedListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        WalletsMaster.getInstance().getCurrentWallet(getActivity()).removeBalanceChangedListener(this);
    }

    @Override
    public void onBalanceChanged(String currencyCode, BigDecimal newBalance) {
        updateQr();
    }

    @Override
    public void onBalancesChanged(Map<String, BigDecimal> balanceMap) {
        updateQr();
    }
}