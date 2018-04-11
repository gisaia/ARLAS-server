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

package io.arlas.server.ogc.csw.operation.getcapabilities;

import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.ogc.csw.utils.CSWConstant;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.utils.CSWRequestType;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import net.opengis.cat.csw._3.CapabilitiesType;
import net.opengis.fes._2.*;
import net.opengis.ows._2.*;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.List;

public class GetCapabilitiesHandler {

    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";
    private static final String SECTION_DOMAIN_NAME = "Sections";
    private static final String ACCEPT_VERSIONS_DOMAIN_NAME = "AcceptVersions";
    private static final String OUTPUT_SCHEMA_DOMAIN_NAME = "OutputSchema";
    private static final String OUTPUT_FORMAT_DOMAIN_NAME = "OutputFormat";
    private static final String ACCEPT_FORMATS_DOMAIN_NAME = "AcceptFormats";
    private static final String RESOLVE_DOMAIN_NAME = "resolve";
    private static final String LOCAL_VALUE = "local";
    private static final String BOX = "BBOX";
    private static final String INTERSECTS = "Intersects";
    private static final String ENVELOPE = "Envelope";
    private static final String POLYGON = "Polygon";

    public CSWHandler cswHandler;
    public OGCConfiguration ogcConfiguration;



    protected ValueType trueValueType = new ValueType();
    protected ValueType falseValueType = new ValueType();


    public GetCapabilitiesHandler(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;
        this.ogcConfiguration = cswHandler.ogcConfiguration;
        trueValueType.setValue(TRUE);
        falseValueType.setValue(FALSE);
    }
    public JAXBElement<CapabilitiesType> getCSWCapabilitiesResponse(List<String> sections,String url) {
        CapabilitiesType getCapabilitiesType = new CapabilitiesType();
        getCapabilitiesType.setVersion(CSWConstant.SUPPORTED_CSW_VERSION);

        if(sections.contains("ServiceIdentification") || sections.contains("All")){
            setServiceIdentification(getCapabilitiesType);
        }
        if(sections.contains("ServiceProvider") || sections.contains("All")){
            setServiceProvider(getCapabilitiesType);
        }
        if(sections.contains("OperationsMetadata") || sections.contains("All")){
            setOperations(getCapabilitiesType);
            setOperationsUrl(getCapabilitiesType,url);
        }
        if(sections.contains("Filter_Capabilities") || sections.contains("All")){
            setFilterCapabilities(getCapabilitiesType);
        }
        if(sections.contains("Languages") || sections.contains("All")){
            CapabilitiesBaseType.Languages languages = new CapabilitiesBaseType.Languages();
            languages.getLanguage().add("FILTER");
            getCapabilitiesType.setLanguages(languages);
        }
        return cswHandler.cswFactory.createCapabilities(getCapabilitiesType);
    }

    private void setServiceProvider(CapabilitiesType getCapabilitiesType) {
        ServiceProvider serviceProvider = new ServiceProvider();
        serviceProvider.setProviderName(ogcConfiguration.serviceProviderName);
        OnlineResourceType onlineResourceType = new OnlineResourceType();
        onlineResourceType.setHref(ogcConfiguration.serviceProviderSite);
        onlineResourceType.setRole(ogcConfiguration.serviceProviderRole);
        serviceProvider.setProviderSite(onlineResourceType);
        ResponsiblePartySubsetType responsiblePartySubsetType = new ResponsiblePartySubsetType();
        responsiblePartySubsetType.setIndividualName(ogcConfiguration.serviceContactIndividualName);
        ContactType contactType = new ContactType();
        AddressType addressType = new AddressType();
        addressType.setCity(ogcConfiguration.serviceContactAdressCity);
        addressType.setCountry(ogcConfiguration.serviceContactAdressCountry);
        addressType.setPostalCode(ogcConfiguration.serviceContactAdressPostalCode);
        contactType.setAddress(addressType);
        responsiblePartySubsetType.setContactInfo(contactType);
        serviceProvider.setServiceContact(responsiblePartySubsetType);
        getCapabilitiesType.setServiceProvider(serviceProvider);
    }

    private void setServiceIdentification(CapabilitiesType getCapabilitiesType) {
        ServiceIdentification serviceIdentification = new ServiceIdentification();
        CodeType codeType = new CodeType();
        codeType.setValue(CSWConstant.CSW);
        serviceIdentification.setServiceType(codeType);
        serviceIdentification.getServiceTypeVersion().add(CSWConstant.SUPPORTED_CSW_VERSION);
        getCapabilitiesType.setServiceIdentification(serviceIdentification);
    }

    public void setOperationsUrl(CapabilitiesType getCapabilitiesType,String url) {
        getCapabilitiesType.getOperationsMetadata().getOperation().forEach(op -> {
            HTTP http = new HTTP();
            RequestMethodType requestMethodType = new RequestMethodType();
            requestMethodType.setHref(url);
            JAXBElement<RequestMethodType> get = cswHandler.owsFactory.createHTTPGet(requestMethodType);
            http.getGetOrPost().add(get);
            op.getDCP().get(0).setHTTP(http);
        });
    }

    private void addSection(String sectionName, DomainType sections) {
        ValueType serviceIdentification = new ValueType();
        serviceIdentification.setValue(sectionName);
        sections.getAllowedValues().getValueOrRange().add(serviceIdentification);
    }

    private void addOperation(String operationName, OperationsMetadata operationsMetadata, DomainType... parameters) {
        DCP dcp = new DCP();
        HTTP http = new HTTP();
        dcp.setHTTP(http);
        Operation operation = new Operation();
        operation.setName(operationName);
        operation.getDCP().add(dcp);
        Arrays.asList(parameters).forEach(parameter -> operation.getParameter().add(parameter));
        operationsMetadata.getOperation().add(operation);
    }

    private void setOperations(CapabilitiesType getCapabilitiesType) {
        OperationsMetadata operationsMetadata = new OperationsMetadata();
        DomainType[] noParameters = {};
        //create AcceptVersions parameter for GetCapabilities operation
        DomainType acceptVersions = new DomainType();
        acceptVersions.setName(ACCEPT_VERSIONS_DOMAIN_NAME);
        acceptVersions.setAllowedValues(new AllowedValues());
        ValueType version = new ValueType();
        version.setValue(CSWConstant.SUPPORTED_CSW_VERSION);
        acceptVersions.getAllowedValues().getValueOrRange().add(version);
        //create outputSchema parameter
        DomainType outputSchema = addDomain(OUTPUT_SCHEMA_DOMAIN_NAME,CSWConstant.SUPPORTED_CSW_OUTPUT_SCHEMA);
        //create outputFormat parameter
        DomainType outputFormat = addDomain(OUTPUT_FORMAT_DOMAIN_NAME,CSWConstant.SUPPORTED_CSW_OUTPUT_FORMAT);
        DomainType acceptFormats = addDomain(ACCEPT_FORMATS_DOMAIN_NAME,CSWConstant.SUPPORTED_CSW_ACCEPT_FORMATS);
        //create sections parameter for GetCapabilities operation
        DomainType sections = new DomainType();
        sections.setName(SECTION_DOMAIN_NAME);
        sections.setAllowedValues(new AllowedValues());
        //add sections
        Arrays.asList(CSWConstant.SECTION_NAMES).forEach(sectionName -> addSection(sectionName, sections));
        //create resolve parameter for GetFeature and GetPropertyValue operation
        DomainType resolve = new DomainType();
        resolve.setName(RESOLVE_DOMAIN_NAME);
        resolve.setAllowedValues(new AllowedValues());
        ValueType local = new ValueType();
        local.setValue(LOCAL_VALUE);
        resolve.getAllowedValues().getValueOrRange().add(local);
        //add  operations
        DomainType[] getCapabilitiesParameters = {acceptVersions, sections,outputSchema,outputFormat,acceptFormats};
        addOperation(CSWRequestType.GetCapabilities.name(), operationsMetadata, getCapabilitiesParameters);
        addOperation(CSWRequestType.GetRecords.name(), operationsMetadata, noParameters);
        addOperation(CSWRequestType.GetRecordById.name(), operationsMetadata, noParameters);
        //add  conformance
        addDefaultConformance(operationsMetadata);
        getCapabilitiesType.setOperationsMetadata(operationsMetadata);
    }

    public void setFilterCapabilities(CapabilitiesType getCapabilitiesType) {
        FilterCapabilities filterCapabilities = cswHandler.fesFactory.createFilterCapabilities();

        ConformanceType fesConformanceType = new ConformanceType();
        addDefaultConformance(fesConformanceType);

        SpatialCapabilitiesType spatialCapabilities = cswHandler.fesFactory.createSpatialCapabilitiesType();
        SpatialOperatorsType spatialOperatorsType = new SpatialOperatorsType();
        GeometryOperandsType geometryOperandsType = new GeometryOperandsType();
        SpatialOperatorType spatialOperatorBBOXType = new SpatialOperatorType();
        SpatialOperatorType spatialOperatorIntersectsType = new SpatialOperatorType();
        GeometryOperandsType.GeometryOperand envGeometryOperand = new GeometryOperandsType.GeometryOperand();
        GeometryOperandsType.GeometryOperand polyGeometryOperand = new GeometryOperandsType.GeometryOperand();
        QName envQname = new QName(WFSConstant.GML_NAMESPACE_URI, ENVELOPE, WFSConstant.GML_PREFIX);
        QName polyQname = new QName(WFSConstant.GML_NAMESPACE_URI, POLYGON, WFSConstant.GML_PREFIX);
        envGeometryOperand.setName(envQname);
        polyGeometryOperand.setName(polyQname);
        geometryOperandsType.getGeometryOperand().add(envGeometryOperand);
        geometryOperandsType.getGeometryOperand().add(polyGeometryOperand);
        spatialCapabilities.setGeometryOperands(geometryOperandsType);
        spatialOperatorBBOXType.setGeometryOperands(geometryOperandsType);
        spatialOperatorIntersectsType.setGeometryOperands(geometryOperandsType);
        spatialOperatorBBOXType.setName(BOX);
        spatialOperatorIntersectsType.setName(INTERSECTS);
        spatialOperatorsType.getSpatialOperator().add(spatialOperatorBBOXType);
        spatialOperatorsType.getSpatialOperator().add(spatialOperatorIntersectsType);
        spatialCapabilities.setSpatialOperators(spatialOperatorsType);

        filterCapabilities.setSpatialCapabilities(spatialCapabilities);
        filterCapabilities.setConformance(fesConformanceType);
        getCapabilitiesType.setFilterCapabilities(filterCapabilities);


    }

    private DomainType addDomain(String domainName, String[] domainValues){

        DomainType domain = new DomainType();
        domain.setName(domainName);
        domain.setAllowedValues(new AllowedValues());
        Arrays.asList(domainValues).forEach(domainValue -> {
            ValueType value = new ValueType();
            value.setValue(domainValue);
            domain.getAllowedValues().getValueOrRange().add(value);
        });
        return domain;
    }

    private void addConformanceType(OperationsMetadata operationsMetadata, String name, ValueType value) {
        DomainType domainType = new DomainType();
        domainType.setName(name);
        domainType.setDefaultValue(value);
        NoValues noValues = new NoValues();
        domainType.setNoValues(noValues);
        operationsMetadata.getConstraint().add(domainType);
    }

    private void addConformanceType(ConformanceType conformanceType, String name, net.opengis.ows._1.ValueType value) {
        net.opengis.ows._1.DomainType domainType = new net.opengis.ows._1.DomainType();
        domainType.setName(name);
        domainType.setDefaultValue(value);
        net.opengis.ows._1.NoValues noValues = new net.opengis.ows._1.NoValues();
        domainType.setNoValues(noValues);
        conformanceType.getConstraint().add(domainType);
    }

    private void addDefaultConformance(OperationsMetadata operationsMetadata) {
        addConformanceType(operationsMetadata, "OpenSearch", falseValueType);
        addConformanceType(operationsMetadata, "GetCapabilities-XML", falseValueType);
        addConformanceType(operationsMetadata, "GetRecordById-XML", falseValueType);
        addConformanceType(operationsMetadata, "GetRecords-Basic-XML", falseValueType);
        addConformanceType(operationsMetadata, "GetRecords-Distributed-XML", falseValueType);
        addConformanceType(operationsMetadata, "GetRecords-Distributed-KVP", falseValueType);
        addConformanceType(operationsMetadata, "GetRecords-Async-XML", falseValueType);
        addConformanceType(operationsMetadata, "GetRecords-Async-KVP", falseValueType);
        addConformanceType(operationsMetadata, "GetDomain-XML", falseValueType);
        addConformanceType(operationsMetadata, "GetDomain-KVP", falseValueType);
        addConformanceType(operationsMetadata, "Transaction", falseValueType);
        addConformanceType(operationsMetadata, "Harvest-Basic-KVP", falseValueType);
        addConformanceType(operationsMetadata, "Harvest-Basic-XML", falseValueType);
        addConformanceType(operationsMetadata, "Harvest-Async-XML", falseValueType);
        addConformanceType(operationsMetadata, "Harvest-Async-KVP", falseValueType);
        addConformanceType(operationsMetadata, "Harvest-Periodic-XML", falseValueType);
        addConformanceType(operationsMetadata, "Harvest-Periodic-KVP", falseValueType);
        addConformanceType(operationsMetadata, "Filter-CQL", falseValueType);
        addConformanceType(operationsMetadata, "Filter-FES-KVP-Advanced", falseValueType);
        addConformanceType(operationsMetadata, "Filter-FES-XML", falseValueType);
        addConformanceType(operationsMetadata, "Filter-FES-KVP", trueValueType);
    }

    private void addDefaultConformance(ConformanceType conformanceType) {
        net.opengis.ows._1.ValueType trueValueType = new  net.opengis.ows._1.ValueType();
        net.opengis.ows._1.ValueType falseValueType = new  net.opengis.ows._1.ValueType();
        trueValueType.setValue(TRUE);
        falseValueType.setValue(FALSE);
        addConformanceType(conformanceType, "ImplementsQuery", trueValueType);
        addConformanceType(conformanceType, "ImplementsSpatialFilter", trueValueType);
        addConformanceType(conformanceType, "ImplementsMinSpatialFilter", trueValueType);
    }
}
