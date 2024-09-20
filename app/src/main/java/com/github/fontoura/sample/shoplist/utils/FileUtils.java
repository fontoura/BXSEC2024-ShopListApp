package com.github.fontoura.sample.shoplist.utils;

import java.io.File;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    public static boolean anyFilesExist(String[] paths) {
        for (String path : paths) {
            try {
                if (new File(path).exists()) {
                    return true;
                }
            } catch (SecurityException e) {
                // no-op
            }
        }
        return false;
    }
}
