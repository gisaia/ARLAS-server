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

import java.awt.image.BufferedImage;

public class ImageUtil {


    public static int coverage(BufferedImage img, int sampling){
        if(img==null){return 0;}
        int count = 0;
        for (int y = 0; y < img.getHeight(); y += sampling) {
            for (int x = 0; x < img.getWidth(); x += sampling) {
                if ((img.getRGB(x, y) >> 24) != 0x00) {
                    count++;
                }
            }
        }
        return (100 * count) / ((img.getHeight() / sampling) * (img.getWidth() / sampling));
    }
}
