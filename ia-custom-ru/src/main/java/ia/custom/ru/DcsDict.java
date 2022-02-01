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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Простой справочник значений, загружаемый из текстового файла.
 * @author zinal
 */
public class DcsDict implements Serializable {
    private static final long serialVersionUID = 20210415001L;

    private final HashSet<String> values = new HashSet<>();

    /**
     * Поиск значения в справочнике
     * @param value Значение (до нормализации)
     * @return true, если нормализованный вариант найден в справочнике, иначе false
     */
    public boolean contains(String value) {
        return values.contains(normalize(value));
    }

    /**
     * Поиск значения в справочнике
     * @param value Значение (ПОСЛЕ нормализации)
     * @return true, если нормализованный вариант найден в справочнике, иначе false
     */
    public boolean containsDirect(String value) {
        return values.contains(value);
    }

    /**
     * Доступ к перечню элементов.
     * @return Перечень элементов
     */
    public Collection<String> getEntries() {
        return Collections.unmodifiableCollection(values);
    }

    /**
     * Добавить значение в справочник.
     * @param value Добавляемое значение
     */
    public void add(String value) {
        value = normalize(value);
        if (value.length() > 0)
            values.add(value);
    }

    /**
     * Нормализация значения
     * @param value Значение до нормализации
     * @return Нормализованное значение:
     *           удалены пробелы по краям,
     *           множественные пробелы на одинарный,
     *           в нижнем регистре
     */
    public static String normalize(String value) {
        value = (value==null) ? "" :
                value.trim().replaceAll("\\s{2,}", " ")
                     .toLowerCase()
                     .replace('ё', 'е');
        return value;
    }

    /**
     * Загрузка справочника из файла
     * @param file Файл со справочником
     */
    public DcsDict(File file) {
        final Path f = file.toPath();
        try {
            Files.lines(f, StandardCharsets.UTF_8).forEach(
                s -> {
                    String n = normalize(s);
                    if (n.length() > 0)
                        values.add(n);
                }
            );
        } catch(Exception ex) {
            DcsUtil.logException(ex);
        }
        values.remove("");
    }

    public void save(File file) throws IOException {
        PrintWriter out = new PrintWriter(
            new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(file, false),
                            StandardCharsets.UTF_8)));
        try {
            for (String v : values) {
                out.println(v);
            }
        } finally {
            out.close();
        }
    }

}
