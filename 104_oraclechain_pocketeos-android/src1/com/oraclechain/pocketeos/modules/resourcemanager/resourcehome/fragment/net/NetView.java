package com.oraclechain.pocketeos.modules.resourcemanager.resourcehome.fragment.net;

import com.oraclechain.pocketeos.base.BaseView;
import com.oraclechain.pocketeos.bean.BlockChainAccountInfoBean;

/**
 * Created by pocketEos on 2017/12/26.
 */
public interface NetView extends BaseView {

    void getBlockchainAccountInfoDataHttp(BlockChainAccountInfoBean.DataBean blockChainAccountInfoBean);

    void getDataHttpFail(String msg);

}
