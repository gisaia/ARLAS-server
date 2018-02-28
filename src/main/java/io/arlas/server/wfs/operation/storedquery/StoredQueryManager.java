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

package io.arlas.server.wfs.operation.storedquery;

import io.arlas.server.wfs.utils.WFSConstant;
import net.opengis.wfs._2.*;
import org.deegree.commons.ows.exception.OWSException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;
import java.util.List;

public class StoredQueryManager {

    private final StoredQuery DEFAULT_QUERY;

    public StoredQueryManager() throws ParserConfigurationException {
        DEFAULT_QUERY = createDefaultStoredQuery();
    }

    public List<StoredQuery> listStoredQueries()  {
        List<StoredQuery> storedQueries = new ArrayList<>();
        storedQueries.add(DEFAULT_QUERY);
        return storedQueries;
    }

    public StoredQuery getStoredQuery(String id) throws OWSException {
        if (DEFAULT_QUERY.getId().equals(id))
            return DEFAULT_QUERY;
        throw new OWSException( "A stored query with identifier '" + id + "' is not offered by this server.", OWSException.INVALID_PARAMETER_VALUE,"request");
    }

    private StoredQuery createDefaultStoredQuery() throws ParserConfigurationException {
        // GetFeatureById query according to the WFS 2.0 spec
        StoredQueryDescriptionType description = new StoredQueryDescriptionType();

        description.setId(WFSConstant.GET_FEATURE_BY_ID_NAME);
        Title queryTitle = new Title();
        queryTitle.setLang("en");
        queryTitle.setValue("GetFeatureById");
        description.getTitle().add(queryTitle);
        Abstract queryAbstract = new Abstract();
        queryAbstract.setLang("en");
        queryAbstract.setValue("Retrieves a feature by its id.");
        description.getAbstract().add(queryAbstract);

        ParameterExpressionType parameter = new ParameterExpressionType();
        parameter.setName("id");
        parameter.setType(QName.valueOf("sring"));
        Title parameterTitle = new Title();
        parameterTitle.setLang("en");
        parameterTitle.setValue("Identifier");
        parameter.getTitle().add(parameterTitle);
        Abstract parameterAbstract = new Abstract();
        parameterAbstract.setLang("en");
        parameterAbstract.setValue("The id of the feature to be retrieved.");
        parameter.getAbstract().add(parameterAbstract);
        description.getParameter().add(parameter);

        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.newDocument();

        Element query = document.createElementNS(WFSConstant.WFS_NAMESPACE_URI, "Query");
        Element filter = document.createElementNS(WFSConstant.FES_NAMESPACE_URI, "Filter");
        Element resourceId = document.createElementNS(WFSConstant.FES_NAMESPACE_URI, "ResourceId");
        resourceId.setAttribute("rid", "${id}");
        filter.appendChild(resourceId);
        query.appendChild(filter);

        QueryExpressionTextType queryExpression = new QueryExpressionTextType();
        queryExpression.getContent().add(query);
        queryExpression.setIsPrivate(false);
        queryExpression.setLanguage("en");
        queryExpression.setLanguage(WFSConstant.DEFAULT_LANGUAGE);
        description.getQueryExpressionText().add(queryExpression);

        return new StoredQuery(description, this);
    }
}
