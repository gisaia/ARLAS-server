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

package io.arlas.server.rest.plugins.eo;

import cyclops.control.Try;
import io.arlas.server.core.exceptions.ArlasException;
import io.arlas.server.core.exceptions.InternalServerErrorException;
import io.arlas.server.core.model.RasterTileURL;
import io.arlas.server.core.utils.Tile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;

public class URLBasedRasterTileProvider implements TileProvider <RasterTile>{
    static {
        ImageIO.scanForPlugins();
    }
    private RasterTileURL template;
    private int width =-1;
    private int height =-1;

    public static final String PATTERN_X="{x}";
    public static final String PATTERN_Y="{y}";
    public static final String PATTERN_Z="{z}";

    private String pattern_x = PATTERN_X;
    private String pattern_y = PATTERN_Y;
    private String pattern_z = PATTERN_Z;

    public URLBasedRasterTileProvider(RasterTileURL template){
        this.template=template;
    }

    public URLBasedRasterTileProvider(RasterTileURL template, int width, int height){
        this.template=template;
        this.height=height;
        this.width=width;
    }

    @Override
    public Try<Optional<RasterTile>,ArlasException> getTile(Tile request) {
        return Try.withCatch(()->{
            RasterTile tile = (request.getzTile() > template.maxZ || request.getzTile() < template.minZ)?null:
                    new RasterTile(request.getxTile(), request.getyTile(), request.getzTile(), getImage(resolveURL(request)));
                return Optional.ofNullable(tile);
        },Error.class).mapFailure(e -> {e.printStackTrace();return new InternalServerErrorException("Can not fetch the tile "+request.getxTile()+"/"+request.getyTile()+"/"+request.getzTile(),e);});
    }

    protected BufferedImage getImage(URL url) throws IOException {
        BufferedImage img = ImageIO.read(url);
        if(width>-1 && height>-1 && (img.getWidth()>width || img.getHeight()>height)){
            return img.getSubimage(0,0,width, height);
        }else{
            return img;
        }
    }

    protected URL resolveURL(Tile request) throws IOException {
        return new URL(template.url
                .replace(getPattern_x(), ""+request.getxTile())
                .replace(getPattern_y(), ""+request.getyTile())
                .replace(getPattern_z(), ""+request.getzTile()));
    }

    public String getPattern_x() {
        return pattern_x;
    }

    public void setPattern_x(String pattern_x) {
        this.pattern_x = pattern_x;
    }

    public String getPattern_y() {
        return pattern_y;
    }

    public void setPattern_y(String pattern_y) {
        this.pattern_y = pattern_y;
    }

    public String getPattern_z() {
        return pattern_z;
    }

    public void setPattern_z(String pattern_z) {
        this.pattern_z = pattern_z;
    }
}
