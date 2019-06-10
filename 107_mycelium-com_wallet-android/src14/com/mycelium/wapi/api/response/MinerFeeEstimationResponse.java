/*
 * Copyright 2013, 2014 Megion Research & Development GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mycelium.wapi.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mycelium.wapi.api.lib.FeeEstimation;

import java.io.Serializable;


public class MinerFeeEstimationResponse implements Serializable {
   private static final long serialVersionUID = 1L;

   @JsonProperty
   public final FeeEstimation feeEstimation;


   public MinerFeeEstimationResponse(@JsonProperty("feeEstimation") FeeEstimation feeEstimation) {
      this.feeEstimation = feeEstimation;
   }
}
