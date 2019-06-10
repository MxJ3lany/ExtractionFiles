/*
 * Copyright (C) 2017 Peng fei Pan <sky@panpf.me>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.panpf.tool4a.provider;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;

import java.util.List;

public class PhoneUtils {
    /**
     * 发送短信，需要SEND_SMS权限
     *
     * @param context        上下文
     * @param number         电话号码
     * @param messageContent 短信内容，如果长度过长将会发多条发送
     */
    public static void sendSms(Context context, String number, String messageContent) {
        SmsManager smsManager = SmsManager.getDefault();
        List<String> contentList = smsManager.divideMessage(messageContent);
        for (String content : contentList) {
            smsManager.sendTextMessage(number, null, content, null, null);
        }
    }

    /**
     * 获取所有联系人的姓名和电话号码，需要READ_CONTACTS权限
     *
     * @param context 上下文
     * @return Cursor。姓名：CommonDataKinds.Phone.DISPLAY_NAME；号码：CommonDataKinds.Phone.NUMBER
     */
    public static Cursor getContactsNameAndNumber(Context context) {
        return context.getContentResolver().query(CommonDataKinds.Phone.CONTENT_URI, new String[]{
                CommonDataKinds.Phone.DISPLAY_NAME, CommonDataKinds.Phone.NUMBER}, null, null, CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
    }

    /**
     * 获取手机号码
     *
     * @param context 上下文
     * @return 手机号码，手机号码不一定能获取到
     */
    public static String getMobilePhoneNumber(Context context) {
        return ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number();
    }
}
