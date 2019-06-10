/*
 * Copyright © 2018 Zhenjie Yan.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.kalle.secure;

import java.security.GeneralSecurityException;

/**
 * Created by Zhenjie Yan on 2018/2/11.
 */
public interface Secret {

    String encrypt(String data) throws GeneralSecurityException;

    byte[] encrypt(byte[] data) throws GeneralSecurityException;

    String decrypt(String data) throws GeneralSecurityException;

    byte[] decrypt(byte[] data) throws GeneralSecurityException;
}