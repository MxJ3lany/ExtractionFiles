
// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

// This file is autogenerated by:
//     mojo/public/tools/bindings/mojom_bindings_generator.py
// For:
//     services/service_manager/public/interfaces/service_factory.mojom
//

package org.chromium.service_manager.mojom;

import org.chromium.base.annotations.SuppressFBWarnings;
import org.chromium.mojo.bindings.DeserializationException;


public interface ServiceFactory extends org.chromium.mojo.bindings.Interface {



    public interface Proxy extends ServiceFactory, org.chromium.mojo.bindings.Interface.Proxy {
    }

    Manager<ServiceFactory, ServiceFactory.Proxy> MANAGER = ServiceFactory_Internal.MANAGER;


    void createService(
org.chromium.mojo.bindings.InterfaceRequest<Service> service, String name);


}
