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
package com.yanzhenjie.kalle;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.yanzhenjie.kalle.util.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Zhenjie Yan on 2018/2/13.
 */
public class FileBody extends BasicOutData<FileBody> implements RequestBody {

    private final File mFile;

    public FileBody(File file) {
        this.mFile = file;
    }

    @Override
    public long length() {
        return mFile.length();
    }

    @Override
    public String contentType() {
        String fileName = mFile.getName();
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        if (TextUtils.isEmpty(mimeType)) mimeType = Headers.VALUE_APPLICATION_STREAM;
        return mimeType;
    }


    @Override
    protected void onWrite(OutputStream writer) throws IOException {
        InputStream reader = new FileInputStream(mFile);
        IOUtils.write(reader, writer);
        IOUtils.closeQuietly(reader);
    }
}