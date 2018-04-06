package io.arlas.server.utils;

import io.arlas.server.exceptions.ArlasException;

public class Tile {
    int xTile;
    int yTile;
    int zTile;

    public Tile(int x, int y, int z) throws ArlasException {
        CheckParams.checkXYZTileValidity(x, y, z);
        this.xTile = x;
        this.yTile = y;
        this.zTile = z;
    }

    public int getxTile() {
        return xTile;
    }

    public void setxTile(int xTile) {
        this.xTile = xTile;
    }

    public int getyTile() {
        return yTile;
    }

    public void setyTile(int yTile) {
        this.yTile = yTile;
    }

    public int getzTile() {
        return zTile;
    }

    public void setzTile(int zTile) {
        this.zTile = zTile;
    }
}
