package io.arlas.server.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Response;

/**
 * Created by sfalquier on 18/08/2017.
 */
public class ResponseCacheManager {

    private int defaultMaxAgeCache = 0;

    public ResponseCacheManager(int defaultMaxAgeCache) {
        this.defaultMaxAgeCache = defaultMaxAgeCache;
    }

    public Response cache(Response.ResponseBuilder response, Integer maxagecache) {
        if(defaultMaxAgeCache > 0 || maxagecache != null){
            if(maxagecache == null){
                maxagecache = defaultMaxAgeCache;// defaultMaxAgeCache is defined in ARLAS configuration file
            }

            CacheControl cc = new CacheControl();
            cc.setPrivate(false);
            cc.setNoCache(false);
            cc.setNoTransform(true);
            cc.setMaxAge(maxagecache);

            response.cacheControl(cc);
        }

        return response.build();
    }
}
