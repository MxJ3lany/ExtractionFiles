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

package com.mrd.bitlib.model;

import java.io.Serializable;

import com.mrd.bitlib.util.ByteWriter;

public class UnspentTransactionOutput implements Serializable {
   private static final long serialVersionUID = 1L;

   public OutPoint outPoint;
   public int height; // -1 means unconfirmed
   public long value;
   public ScriptOutput script;

   public UnspentTransactionOutput(OutPoint outPoint, int height, long value, ScriptOutput script) {
      this.outPoint = outPoint;
      this.height = height;
      this.value = value;
      this.script = script;
   }

   public byte[] toBytes() {
      ByteWriter writer = new ByteWriter(1024);
      toByteWriter(writer);
      return writer.toBytes();
   }

   public void toByteWriter(ByteWriter writer) {
      outPoint.toByteWriter(writer);
      writer.putIntLE(height);
      writer.putLongLE(value);
      byte[] scriptBytes = script.getScriptBytes();
      writer.putCompactInt(scriptBytes.length);
      writer.putBytes(scriptBytes);
   }

   @Override
   public String toString() {
      return "outPoint:" + outPoint + " height:" + height + " value: " + value +
              " script: " + script.dump();
   }

   @Override
   public int hashCode() {
      return outPoint.txid.hashCode() + outPoint.index;
   }

   @Override
   public boolean equals(Object obj) {
      if (obj == this) {
         return true;
      }
      if (!(obj instanceof UnspentTransactionOutput)) {
         return false;
      }
      UnspentTransactionOutput other = (UnspentTransactionOutput) obj;
      return outPoint.equals(other.outPoint);
   }
}
