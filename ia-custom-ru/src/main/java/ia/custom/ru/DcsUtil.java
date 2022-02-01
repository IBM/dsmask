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
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Вспомогательные алгоритмы для работы со справочниками и значениями.
 * @author zinal
 */
public class DcsUtil {

    /**
     * Переводим в строку и удаляем все нечисловые символы
     * @param value Обрабатываемое значение.
     * @return Строка, содержащая только цифры из исходного значения.
     */
    public static String extractDigits(Object value) {
        if (value==null)
            return "";
        if (value instanceof Number) {
            String str = value.toString();
            if (str.endsWith(".0"))
                return str.substring(0, str.length()-2);
            return str.replaceAll("[^\\d]", "");
        }
        return value.toString().trim().replaceAll("[^\\d]", "");
    }

    /**
     * Протоколирование исключения в файл.
     * @param ex Исключение
     */
    public static void logException(Exception ex) {
        File f = new File(System.getProperty("java.io.tmpdir"));
        try {
            final Date d = new Date();
            f = new File(f, "ia-DcsRus");
            f = new File(f, new SimpleDateFormat("yyyy-MM-dd").format(d));
            f.mkdirs();
            f = new File(f,
               "ia-DcsRus_"
                    + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(d)
                    + ".txt");
            PrintWriter out = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(
                                new FileOutputStream(f, true),
                                StandardCharsets.UTF_8)));
            try {
                ex.printStackTrace(out);
                out.println();
                out.println();
            } finally {
                out.close();
            }
        } catch(Exception skip) {}
        System.out.println("EXCEPTION-LOG: " + ex.toString());
        System.out.println("\tdetails in " + f);
        System.out.println();
    }

}
