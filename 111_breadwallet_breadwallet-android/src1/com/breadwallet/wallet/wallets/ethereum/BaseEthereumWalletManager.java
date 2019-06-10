package com.breadwallet.wallet.wallets.ethereum;

import android.content.Context;

import com.breadwallet.core.ethereum.BREthereumAmount;
import com.breadwallet.core.ethereum.BREthereumLightNode;
import com.breadwallet.core.ethereum.BREthereumTransaction;
import com.breadwallet.core.ethereum.BREthereumWallet;
import com.breadwallet.presenter.entities.BRSettingsItem;
import com.breadwallet.presenter.entities.TxUiHolder;
import com.breadwallet.tools.manager.BRSharedPrefs;
import com.breadwallet.tools.util.Utils;
import com.breadwallet.wallet.abstracts.BaseWalletManager;
import com.breadwallet.wallet.abstracts.BalanceUpdateListener;
import com.breadwallet.wallet.abstracts.OnTxListModified;
import com.breadwallet.wallet.abstracts.SyncListener;
import com.breadwallet.wallet.wallets.WalletManagerHelper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BaseEthereumWalletManager implements BaseWalletManager {
    private static final String ETHEREUM_ADDRESS_PREFIX = "0x";
    static final int SCALE = 8;

    private WalletManagerHelper mWalletManagerHelper;
    protected String mAddress;

    public BaseEthereumWalletManager() {
        mWalletManagerHelper = new WalletManagerHelper();
    }

    protected WalletManagerHelper getWalletManagerHelper() {
        return mWalletManagerHelper;
    }

    //TODO Not used by ETH, ERC20
    @Override
    public int getForkId() {
        return -1;
    }

    @Override
    public synchronized String getAddress(Context context) { //todo context is not used, refactor
        if (mAddress == null) {
            throw new IllegalArgumentException("Address cannot be null.  Make sure it is set in the constructor.");
        }

        // TODO: Test of we can remove the caching in memory and always call core directly.
        return mAddress;
    }

    public abstract BREthereumWallet getWallet();

    @Override
    public boolean isAddressValid(String address) {
        return !Utils.isNullOrEmpty(address) && address.startsWith(ETHEREUM_ADDRESS_PREFIX) && BREthereumLightNode.addressIsValid(address);
    }

    @Override
    public void addBalanceChangedListener(BalanceUpdateListener listener) {
        mWalletManagerHelper.addBalanceChangedListener(listener);
    }

    @Override
    public void removeBalanceChangedListener(BalanceUpdateListener listener) {
        mWalletManagerHelper.removeBalanceChangedListener(listener);
    }

    @Override
    public void onBalanceChanged(BigDecimal balance) {
        mWalletManagerHelper.onBalanceChanged(getCurrencyCode(), balance);
    }

    // TODO not used by ETH, ERC20
    @Override
    public void addSyncListener(SyncListener listener) {
    }

    // TODO not used by ETH, ERC20
    @Override
    public void removeSyncListener(SyncListener listener) {
    }

    @Override
    public void addTxListModifiedListener(OnTxListModified listener) {
        mWalletManagerHelper.addTxListModifiedListener(listener);
    }

    @Override
    public void removeTxListModifiedListener(OnTxListModified listener) {
        mWalletManagerHelper.removeTxListModifiedListener(listener);
    }

    //TODO Not used by ETH, ERC20
    @Override
    public void refreshAddress(Context app) {
    }

    protected abstract WalletEthManager getEthereumWallet();

    @Override
    public List<TxUiHolder> getTxUiHolders(Context app) {
        BREthereumTransaction[] txs = getWallet().getTransactions();
        int blockHeight = (int) getEthereumWallet().getBlockHeight();
        if (app != null && blockHeight != Integer.MAX_VALUE && blockHeight > 0) {
            BRSharedPrefs.putLastBlockHeight(app, getCurrencyCode(), blockHeight);
        }
        if (txs != null && txs.length > 0) {
            List<TxUiHolder> uiTxs = new ArrayList<>();
            for (int i = txs.length - 1; i >= 0; i--) { //revere order
                BREthereumTransaction tx = txs[i];
                if (tx.isSubmitted()) {
                    BREthereumAmount.Unit feeUnit = getCurrencyCode().equalsIgnoreCase(WalletEthManager.ETH_CURRENCY_CODE)
                            ? BREthereumAmount.Unit.ETHER_WEI : BREthereumAmount.Unit.ETHER_GWEI;
                    uiTxs.add(new TxUiHolder(tx, tx.getTargetAddress().equalsIgnoreCase(getEthereumWallet().getWallet().getAccount().getPrimaryAddress()),
                            tx.getBlockTimestamp(), (int) tx.getBlockNumber(), Utils.isNullOrEmpty(tx.getHash())
                            ? null : tx.getHash().getBytes(), tx.getHash(), new BigDecimal(tx.getFee(feeUnit)),
                            tx.getTargetAddress(), tx.getSourceAddress(), null, 0,
                            new BigDecimal(tx.getAmount(getUnit())), true));
                }
            }
            return uiTxs;
        } else {
            return null;
        }
    }

    @Override
    public boolean checkConfirmations(int conformations) {
        return mWalletManagerHelper.checkConfirmations(conformations);
    }

    public List<BRSettingsItem> getSettingsList(Context context) {
        return Collections.emptyList();
    }
}
