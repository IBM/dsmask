/*
 * Copyright (c) IBM Corp. 2018, 2021.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *  Maksim Zinal (IBM) - Initial implementation
 */
package ia.custom.ru;

import java.io.File;
import java.util.HashMap;

/**
 * Коллекция загруженных справочников, с целью кеширования
 * на время работы задания Information Analyzer.
 * @author zinal
 */
public class DcsFactory {

    private static final Object GUARD = new Object();
    private static String BASE_PATH = null;

    private static final HashMap<String, DcsDict> DICTS = new HashMap<>();

    /**
     * Получить путь к каталогу хранения файлов со справочниками.
     * @return Путь к каталогу со справочниками.
     */
    public static String getBasePath() {
        synchronized(GUARD) {
            if (BASE_PATH==null || BASE_PATH.length()==0)
                BASE_PATH = computeBasePath();
            return BASE_PATH;
        }
    }

    /**
     * Определить путь к каталогу хранения файлов со справочниками.
     * @return Путь к каталогу со справочниками.
     */
    private static String computeBasePath() {
        String basePath = System.getenv("OPTIM_DCS_DICT");
        if (basePath == null || basePath.length()==0) {
            if ( '\\' == File.separatorChar ) {
                basePath = "C:\\IBM\\Masker\\ia-bundle-ru\\iadict";
            } else {
                basePath = "/opt/IBM/Masker/ia-bundle-ru/iadict";
            }
        }
        return basePath;
    }

    public static void setBasePath(String basePath) {
        synchronized(GUARD) {
            BASE_PATH = basePath;
        }
    }

    public static void setBasePath(File basePath) {
        setBasePath(basePath==null ? (String)null : basePath.getAbsolutePath());
    }

    /**
     * Получить справочник с указанным именем
     * @param dictName Имя справочника
     * @return Объект справочника
     */
    public static DcsDict dictionary(String dictName) {
        dictName = (dictName==null) ? "" :
                dictName.replaceAll("\\s{2,}", " ").trim();
        if (dictName.length()==0)
            dictName = "default";
        DcsDict d;
        synchronized(DICTS) {
            d = DICTS.get(dictName);
            if (d==null) {
                d = new DcsDict(new File(getBasePath(), dictName + ".txt"));
                DICTS.put(dictName, d);
            }
        }
        return d;
    }

}
