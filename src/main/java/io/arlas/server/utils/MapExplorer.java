package io.arlas.server.utils;

import io.arlas.server.model.response.CollectionReferenceDescriptionProperty;
import org.elasticsearch.common.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by sfalquier on 11/08/2017.
 */
public class MapExplorer {

    public static Object getObjectFromPath(String path, Object source) {
        if(Strings.isNullOrEmpty(path)) {
            return source;
        } else {
            String[] pathLevels = path.split("\\.");
            StringBuffer newPath = new StringBuffer();
            for(int i=1;i<pathLevels.length;i++) {
                newPath.append(pathLevels[i]);
                if(i<pathLevels.length-1) {
                    newPath.append(".");
                }
            }
            if(source instanceof Map && ((Map)source).containsKey(pathLevels[0])) {
                return getObjectFromPath(newPath.toString(), ((Map) source).get(pathLevels[0]));
            } else if(source instanceof CollectionReferenceDescriptionProperty
                    && ((CollectionReferenceDescriptionProperty)source).properties!=null
                    && ((CollectionReferenceDescriptionProperty)source).properties.containsKey(pathLevels[0])) {
                return getObjectFromPath(newPath.toString(), ((CollectionReferenceDescriptionProperty)source).properties.get(pathLevels[0]));
            } else {
                return null;
            }
        }
    }
}
