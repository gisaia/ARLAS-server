# ARLAS RASTER Tile Service

This API is available for generating RASTER tiles for `hits` in ARLAS `collections` matching a filter and available through a tile service (see the collection [configuration](arlas-collection-model.md)). 
It is assumed that for a given image, its PNG tiles are available with a URL pattern such as:

```
http(s?)://hostname:port/any_path/{id}/{z}/{x}/{y}.png
```

where:

- `{id}` place holder is for the identifier of the image. The value extracted from the hit is configurable in the collection description
- `{z}` place holder is for the zoom level
- `{x}` place holder is for the x coordinate of the tile
- `{y}` place holder is for the y coordinate of the tile

See [Tiled Web Maps](https://en.wikipedia.org/wiki/Tiled_web_map) for more details on the coordinates. This pattern must be configured when registering the collection.

Once configured and the third party tile service up and running, then tiles can be requested to the ARLAS tile service available at:
```
GET /explore/{collection}/_tile/{z}/{x}/{y}.png
```

## How it works

The ARLAS tile service looks for the `hits` of the `{collection}` matching the `{z}/{x}/{y}` extends and stack them until the tile is painted enough: once the percentage of painted pixel is greater than `coverage`.
Tiles are stacked by using the `sort` parameter:
- the first tile is painted. If the coverage is reached, it ends here
- otherwise, the next tile is painted on the transparent pixels. If the coverage is reached, it ends here
- otherwise, it goes on until all the hits have been processed. The number of hits used for rendering the tile is the one defined in the `size`parameter.

## Service parameters

The service accepts the query parameters of a search (except for the include/exclude and pretty parameters):

- a filter
- a full text search
- geometric query (intersect, within, etc)
- pagination (size, from, sort, after)

and also two further parameters:
- sampling: step for transparent pixel testing: 1-> test every pixel, 10-> test every 10 pixel in x and every 10 pixel in y
- coverage: the min percentage of the tile that must painted.

!!! note
    In case the gintersect search returns many false positives, then using the `check_geometry` strategy can significantly optimize the rendering process
