/*
 * Copyright (C) 2007 Esmertec AG. Copyright (C) 2007 The Android Open Source
 * Project
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

package org.awesomeapp.messenger.model;

// import org.awesomeapp.messenger.plugin.loopback.LoopbackConnection;
import android.content.Context;

import org.awesomeapp.messenger.plugin.ImConfigNames;
import org.awesomeapp.messenger.plugin.xmpp.XmppConnection;

import java.util.Map;

/** The factory used to create an instance of ImConnection. */
public class ConnectionFactory {
    private static ConnectionFactory sInstance;

    private ConnectionFactory() {
    }

    /**
     * Gets the singleton instance of the factory.
     *
     * @return the singleton instance.
     */
    public synchronized static ConnectionFactory getInstance() {
        if (sInstance == null) {
            sInstance = new ConnectionFactory();
        }
        return sInstance;
    }

    /**
     * Creates a new {@link ImConnection}.
     *
     * @return the new {@link ImConnection}.
     * @throws ImException if an error occurs during creating a connection.
     */
    public synchronized ImConnection createConnection(Map<String, String> settings, Context context)
            throws ImException {
        String protocolName = settings.get(ImConfigNames.PROTOCOL_NAME);
        if ("XMPP".equals(protocolName)) {

            try
            {
                return new XmppConnection(context);
            }
            catch (Exception e)
            {
                throw new ImException(e.getMessage());
            }
        }
        if ("LLXMPP".equals(protocolName)) {
            //return new LLXmppConnection(context);
            return null;
        }
        /*else if ("LOOPBACK".equals(protocolName)) {
        	return new SMSConnection();
        } */
        else {
            throw new ImException("Unsupported protocol: " + protocolName);
        }
    }
}
