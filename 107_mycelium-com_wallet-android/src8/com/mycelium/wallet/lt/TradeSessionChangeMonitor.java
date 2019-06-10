/*
 * Copyright 2013, 2014 Megion Research and Development GmbH
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.mycelium.wallet.lt;

import java.util.UUID;

import android.os.Handler;
import android.util.Log;

import com.mycelium.lt.api.LtApi;
import com.mycelium.lt.api.LtApiException;
import com.mycelium.lt.api.model.TradeSession;

public class TradeSessionChangeMonitor {
   private static final String TAG = "TradeSesChangeMon";

   public static abstract class Listener {
      private final Handler _handler;
      private final UUID _tradeSessionId;
      private final long _lastChange;

      protected Listener(UUID tradeSessionId, long lastChange) {
         _tradeSessionId = tradeSessionId;
         _lastChange = lastChange;
         _handler = new Handler();
      }

      public UUID getTradeSessionId() {
         return _tradeSessionId;
      }

      public long getLastChange() {
         return _lastChange;
      }

      public abstract void onTradeSessionChanged(TradeSession s);

   }

   private static class Monitor implements Runnable {

      private static final long RETRY_TIMEOUT_MS = 5000;
      private LocalTraderManager _ltManager;
      private LtApi _api;
      private Thread _thread;
      private UUID _sessionId;
      private Listener _listener;
      private boolean _isCancelled;

      public Monitor(LocalTraderManager ltManager, LtApi api, Listener listener, UUID sessionId) {
         _ltManager = ltManager;
         _api = api;
         _listener = listener;
         _sessionId = sessionId;
         _thread = new Thread(this);
         _thread.setDaemon(true);
         _thread.setName(TAG);
         _thread.start();
      }

      @Override
      public void run() {
         long timestamp = _listener.getLastChange();
         Log.i(TAG, "Monitor Starting with timestamp " + timestamp);
         try {
            while (!_isCancelled) {
               if (!_ltManager.hasLocalTraderAccount()) {
                  // The user might be about to enable local trader, sleep a
                  // while and try again
                  Thread.sleep(10000);
                  continue;
               }
               try {
                  // Wait for session change
                  final TradeSession result = _api.waitForTradeSessionChange(_sessionId,
                        _listener.getTradeSessionId(), timestamp).getResult();

                  if (_isCancelled) {
                     return;
                  }
                  // Update the timestamp we we trigger on the next update
                  timestamp = result.lastChange;
                  // Success, post update to caller
                  _listener._handler.post(new Runnable() {

                     @Override
                     public void run() {
                        if (!_isCancelled) {
                           _listener.onTradeSessionChanged(result);
                        }
                     }
                  });

               } catch (final LtApiException e) {
                  if (e.errorCode == LtApi.ERROR_CODE_WAIT_TIMEOUT) {
                     // Timeout, try again
                     continue;
                  } else {
                     // Something went wrong, sleep on it and try again
                     if (!_isCancelled) {
                        Log.w(TAG, "Monitoring failed, retrying in " + (RETRY_TIMEOUT_MS / 1000) + " seconds");
                        Thread.sleep(RETRY_TIMEOUT_MS);
                     }
                  }
               } // catch
            } // while
            Log.i(TAG, "Monitor stopping with timestamp " + timestamp);
         } catch (Exception e) {
            Log.e(TAG, "Caught exception in monitor, exiting.", e);
            _isCancelled = true;
         }
      }

      public void cancel() {
         if (!_isCancelled) {
            Log.i(TAG, "Cancelling monitor");
            _isCancelled = true;
            // Stop long polling from the server side by making an API call in a
            // separate thread
            Thread t = new Thread(new Runnable() {

               @Override
               public void run() {
                  try {
                     _api.stopWaitingForTradeSessionChange(_sessionId);
                  } catch (Exception e) {
                     // Ignore
                  }
               }
            });
            t.setDaemon(true);
            t.start();
         }
      }

   }

   private LocalTraderManager _ltManager;
   private LtApi _api;
   private Monitor _monitor;

   public TradeSessionChangeMonitor(LocalTraderManager ltManager, LtApi api) {
      _ltManager = ltManager;
      _api = api;
   }

   public void startMonitoring(UUID sessionId, Listener listener) {
      stopMonitoring();
      _monitor = new Monitor(_ltManager, _api, listener, sessionId);

   }

   public void stopMonitoring() {
      if (_monitor != null) {
         _monitor.cancel();
      }
      _monitor = null;
   }
}
