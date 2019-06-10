package org.web3j.protocol.geth;

import io.reactivex.Flowable;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.BooleanResponse;
import org.web3j.protocol.admin.methods.response.PersonalSign;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.MinerStartResponse;
import org.web3j.protocol.geth.response.PersonalEcRecover;
import org.web3j.protocol.geth.response.PersonalImportRawKey;
import org.web3j.protocol.websocket.events.PendingTransactionNotification;
import org.web3j.protocol.websocket.events.SyncingNotfication;

/**
 * JSON-RPC Request object building factory for Geth. 
 */
public interface Geth extends Admin {
    static Geth build(Web3jService web3jService) {
        return new JsonRpc2_0Geth(web3jService);
    }
        
    Request<?, PersonalImportRawKey> personalImportRawKey(String keydata, String password);

    Request<?, BooleanResponse> personalLockAccount(String accountId);
    
    Request<?, PersonalSign> personalSign(String message, String accountId, String password);
    
    Request<?, PersonalEcRecover> personalEcRecover(String message, String signiture);

    Request<?, MinerStartResponse> minerStart(int threadCount);

    Request<?, BooleanResponse> minerStop();

    /**
     * Creates an {@link Flowable} instance that emits a notification when a new transaction is
     * added to the pending state and is signed with a key that is available in the node.
     *
     * @return a {@link Flowable} instance that emits a notification when a new transaction is
     *         added to the pending state
     */
    Flowable<PendingTransactionNotification> newPendingTransactionsNotifications();

    /**
     * Creates an {@link Flowable} instance that emits a notification when a node starts or stops
     * syncing.
     * @return a {@link Flowable} instance that emits changes to syncing status
     */
    Flowable<SyncingNotfication> syncingStatusNotifications();

}
