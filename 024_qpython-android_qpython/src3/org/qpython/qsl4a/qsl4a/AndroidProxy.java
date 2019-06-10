/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.qpython.qsl4a.qsl4a;

import android.app.Service;
import android.content.Intent;

import org.qpython.qsl4a.qsl4a.facade.FacadeConfiguration;
import org.qpython.qsl4a.qsl4a.facade.FacadeManagerFactory;
import org.qpython.qsl4a.qsl4a.jsonrpc.JsonRpcServer;
import org.qpython.qsl4a.qsl4a.jsonrpc.RpcReceiverManagerFactory;

import java.net.InetSocketAddress;
import java.util.UUID;

public class AndroidProxy {

  private InetSocketAddress mAddress;
  private final JsonRpcServer mJsonRpcServer;
  private final UUID mSecret;
  private final RpcReceiverManagerFactory mFacadeManagerFactory;

  /**
   * 
   * @param service
   *          Android service (required to build facades).
   * @param intent
   *          the intent that launched the proxy/script.
   * @param requiresHandshake
   *          indicates whether RPC security protocol should be enabled.
   */
  public AndroidProxy(Service service, Intent intent, boolean requiresHandshake) {
    if (requiresHandshake) {
      mSecret = UUID.randomUUID();
    } else {
      mSecret = null;
    }
    mFacadeManagerFactory =
        new FacadeManagerFactory(FacadeConfiguration.getSdkLevel(), service, intent,
            FacadeConfiguration.getFacadeClasses());
    mJsonRpcServer = new JsonRpcServer(mFacadeManagerFactory, getSecret());
  }

  public InetSocketAddress getAddress() {
    return mAddress;
  }

  public InetSocketAddress startLocal() {
    return startLocal(0);
  }

  public InetSocketAddress startLocal(int port) {
    mAddress = mJsonRpcServer.startLocal(port);
    return mAddress;
  }
  
  public InetSocketAddress startPublic() {
    return startPublic(0);
  }

  public InetSocketAddress startPublic(int port) {
    mAddress = mJsonRpcServer.startPublic(port);
    return mAddress;
  }

  public void shutdown() {
	  if (mJsonRpcServer!=null) {
		  mJsonRpcServer.shutdown();
	  }
  }

  public String getSecret() {
    if (mSecret == null) {
      return null;
    }
    return mSecret.toString();
  }

  public RpcReceiverManagerFactory getRpcReceiverManagerFactory() {
    return mFacadeManagerFactory;
  }
}
