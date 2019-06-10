/*
 * Copyright (C) 2016.  BoBoMEe(wbwjx115@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bobomee.android.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

/**
 * Created on 16/5/26.下午3:14.
 * @author bobomee.
 * wbwjx115@gmail.com
 */
public class FileSizeUtil {

  /**
   * 调用此方法自动计算指定文件或指定文件夹的大小
   *
   * @param filePath 文件路径
   * @return 计算好的带B、KB、MB、GB的字符串
   */
  public static String getAutoFileOrFilesSize(String filePath) {
    File file = new File(filePath);
    long blockSize = 0;
    try {
      if (file.isDirectory()) {
        blockSize = getFileSizes(file);
      } else {
        blockSize = getFileSize(file);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return FormetFileSize(blockSize);
  }

  /**
   * 获取指定文件大小
   *
   * @throws Exception
   */
  private static long getFileSize(File file) throws Exception {
    long size = 0;
    if (file.exists()) {
      FileInputStream fis = null;
      fis = new FileInputStream(file);
      size = fis.available();
    }
    return size;
  }

  /**
   * 获取指定文件夹
   *
   * @throws Exception
   */
  private static long getFileSizes(File f) throws Exception {
    long size = 0;
    File flist[] = f.listFiles();
    for (int i = 0; i < flist.length; i++) {
      if (flist[i].isDirectory()) {
        size = size + getFileSizes(flist[i]);
      } else {
        size = size + getFileSize(flist[i]);
      }
    }
    return size;
  }

  public static String FormetFileSize(long size) {
    if (size <= 0) {
      return "0";
    }
    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
    return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups))
        + " "
        + units[digitGroups];
  }
}
