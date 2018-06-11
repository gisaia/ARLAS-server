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
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.utils.ImageUtil;
import io.arlas.server.utils.Tile;

import java.awt.*;
import java.util.Optional;
import java.util.Queue;

public class RasterTileStacker {
    private Percentage upTo=new Percentage(70);
    private Queue<TileProvider<RasterTile>> providers;
    private int sampling = 1;

    public RasterTileStacker stack(Queue<TileProvider<RasterTile>> providers){
        this.providers = providers;
        return this;
    }

    public RasterTileStacker sampling(int sampling) throws ArlasException {
        if(sampling<1)throw new InvalidParameterException("Sampling must be greater than 1 but is "+sampling);
        this.sampling=sampling;
        return this;
    }

    public RasterTileStacker upTo(Percentage percentage) throws ArlasException {
        if(upTo.getValue()<0)throw new InvalidParameterException("Percentage must be greater than 1 but is "+sampling);
        this.upTo=percentage;
        return this;
    }

    public Try<Optional<RasterTile>,ArlasException> on(Tile where) throws ArlasException {
        if(this.providers.size()==0){return Try.success(Optional.empty());}
        return stack(where, Optional.empty(), providers, upTo);
    }

    private Try<Optional<RasterTile>,ArlasException> stack(Tile where, Optional<RasterTile> done, Queue<TileProvider<RasterTile>> providers, Percentage upTo)  {
        return Try.flatten(providers.poll().getTile(where).map(otile ->
                {
                    Optional<RasterTile> merged = merge(done, otile).orElse(Optional.empty());
                    Percentage coverage = upTo.getValue() > 0 ? this.coverage(merged, this.sampling) : new Percentage(0); // no need to compute the percentage if upTo is 0
                    if (providers.size() == 0 || coverage.getValue() >= upTo.getValue()) {
                        return Try.success(merged);
                    } else {
                        return stack(where, merged, providers, upTo);
                    }
                }
        ));
    }

    private  Try<Optional<RasterTile>,ArlasException> merge(Optional<RasterTile> obottom, Optional<RasterTile>  otop)  {
        return otop.map(top -> // if there's a top
                obottom.map(bottom->{ // and a bottom then we merge
                    Graphics g = bottom.getImg().getGraphics();
                    g.drawImage(top.getImg(), 0, 0, null);
                    g.dispose();
                    return Try.withCatch(() -> Optional.of(new RasterTile(top.getxTile(), top.getyTile(), top.getzTile(), bottom.getImg())));
                }).orElse(Try.success(otop)) //no bottom but there's a top
        ).orElse(Try.success(obottom)); // no top but there's maybe a bottom
    }

    private Percentage coverage(Optional<RasterTile> tile, int sampling){
        return tile.map(t-> {
            return new Percentage(ImageUtil.coverage(t.getImg(),sampling));
        }).orElse(new Percentage(0));
    }

    public static class Percentage{
        final private int value;
        public Percentage(int value){this.value=value;}
        public int getValue(){return value;}
    }
}
