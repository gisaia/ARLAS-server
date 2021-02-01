/*
 * Licensed to Gisaïa under one or more contributor
 * license agreements. See the NOTICE.txt file distributed with
 * this work for additional information regarding copyright
 * ownership. Gisaïa licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.arlas.server.utils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class IOUtils {

    public static void zipDirectory(
            File directory, String prefix, ZipOutputStream zipout, final FilenameFilter filter)
            throws IOException {
        File[] files = directory.listFiles(filter);
        // copy file by reading 4k at a time (faster than buffered reading)
        byte[] buffer = new byte[4 * 1024];
        if (files != null) {
            for (File file : files) {
                if (file.exists()) {
                    if (file.isDirectory()) {
                        // recurse and append
                        String newPrefix = prefix + file.getName() + "/";
                        zipout.putNextEntry(new ZipEntry(newPrefix));
                        zipDirectory(file, newPrefix, zipout, filter);
                    } else {
                        ZipEntry entry = new ZipEntry(prefix + file.getName());
                        zipout.putNextEntry(entry);

                        try (InputStream in = new FileInputStream(file)) {
                            int c;
                            while (-1 != (c = in.read(buffer))) {
                                zipout.write(buffer, 0, c);
                            }
                            zipout.closeEntry();
                        }
                    }
                }
            }
        }
        zipout.flush();
    }

}
