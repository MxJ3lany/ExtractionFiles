package org.qpython.qsl4a.qsl4a.interpreter;

import org.qpython.qsl4a.qsl4a.StringUtils;

import dalvik.system.DexClassLoader;

import java.util.Collection;

public class ExternalClassLoader {

  public Object load(Collection<String> dexPaths, Collection<String> nativePaths, String className)
      throws Exception {
    String dexOutputDir = "/sdcard/dexoutput";
    String joinedDexPaths = StringUtils.join(dexPaths, ":");
    String joinedNativeLibPaths = nativePaths != null ? StringUtils.join(nativePaths, ":") : null;
    DexClassLoader loader =
        new DexClassLoader(joinedDexPaths, dexOutputDir, joinedNativeLibPaths, this.getClass()
            .getClassLoader());
    Class<?> classToLoad = Class.forName(className, true, loader);
    return classToLoad.newInstance();
  }
}