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

import eu.europa.ec.inspire.schemas.common._1.*;
import eu.europa.ec.inspire.schemas.inspire_dls._1.ExtendedCapabilitiesType;
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.exceptions.ArlasException;
import io.arlas.server.exceptions.INSPIRE.INSPIREException;
import io.arlas.server.exceptions.INSPIRE.INSPIREExceptionCode;
import io.arlas.server.exceptions.InternalServerErrorException;
import io.arlas.server.exceptions.InvalidParameterException;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.inspire.common.constants.INSPIREConstants;
import io.arlas.server.inspire.common.enums.AdditionalQueryables;
import io.arlas.server.inspire.common.enums.SupportedISOQueryables;
import io.arlas.server.inspire.common.utils.INSPIRECheckParam;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.INSPIREConformity;
import io.arlas.server.ns.GML;
import io.arlas.server.ogc.common.model.Service;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.utils.CSWConstant;
import io.arlas.server.ogc.csw.utils.CSWRequestType;
import net.opengis.cat.csw._3.CapabilitiesType;
import net.opengis.fes._2.*;
import net.opengis.ows._2.*;
import org.elasticsearch.common.Strings;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GetCapabilitiesHandler {

    private static final String TRUE = "TRUE";
    private static final String FALSE = "FALSE";
    private static final String SECTION_DOMAIN_NAME = "Sections";
    private static final String ACCEPT_VERSIONS_DOMAIN_NAME = "AcceptVersions";
    private static final String OUTPUT_SCHEMA_DOMAIN_NAME = "OutputSchema";
    private static final String OUTPUT_FORMAT_DOMAIN_NAME = "OutputFormat";
    private static final String ACCEPT_FORMATS_DOMAIN_NAME = "AcceptFormats";
    private static final String OPENSEARCH_DOMAIN_NAME = "OpenSearchDescriptionDocument";
    private static final String RESOLVE_DOMAIN_NAME = "resolve";
    private static final String LOCAL_VALUE = "local";
    private static final String BOX = "BBOX";
    private static final String INTERSECTS = "Intersects";
    private static final String ENVELOPE = "Envelope";
    private static final String POLYGON = "Polygon";

    public CSWHandler cswHandler;
    public OGCConfiguration ogcConfiguration;
    public CapabilitiesType capabilitiesType;
    public ExtendedCapabilitiesType inspireExtendedCapabilitiesType = new ExtendedCapabilitiesType();
    private List<String> sections;




    protected ValueType trueValueType = new ValueType();
    protected ValueType falseValueType = new ValueType();


    public GetCapabilitiesHandler(CSWHandler cswHandler) {
        this.cswHandler = cswHandler;
        this.ogcConfiguration = cswHandler.ogcConfiguration;
        trueValueType.setValue(TRUE);
        falseValueType.setValue(FALSE);
    }
    public void setCapabilitiesType(List<String> sections, String url, String urlOpenSearch) {
        capabilitiesType = new CapabilitiesType();
        capabilitiesType.setVersion(CSWConstant.SUPPORTED_CSW_VERSION);
        this.sections = sections;

        if(sections.contains("ServiceIdentification") || sections.contains("All")){
            setServiceIdentification(capabilitiesType);
        }
        if(sections.contains("ServiceProvider") || sections.contains("All")){
            setServiceProvider(capabilitiesType);
        }
        if(sections.contains("OperationsMetadata") || sections.contains("All")){
            setOperations(capabilitiesType,urlOpenSearch);
            setOperationsUrl(capabilitiesType,url);
        }
        if(sections.contains("Filter_Capabilities") || sections.contains("All")){
            setFilterCapabilities(capabilitiesType);
        }
        if(sections.contains("Languages") || sections.contains("All")){
            CapabilitiesBaseType.Languages languages = new CapabilitiesBaseType.Languages();
            languages.getLanguage().add("FILTER");
            capabilitiesType.setLanguages(languages);
        }
    }
    public JAXBElement<CapabilitiesType> getCSWCapabilitiesResponse() {
        return cswHandler.cswFactory.createCapabilities(capabilitiesType);
    }

    public void addINSPIRECompliantElements(List<CollectionReference> collections, List<String> sections, String serviceUrl, String language) throws ArlasException {
        if(sections.contains("OperationsMetadata") || sections.contains("All")){
            addExtendedCapabilities(collections, serviceUrl);
        }
        if(sections.contains("ServiceIdentification") || sections.contains("All")){
            setInspireServiceIdentification(language);
        }
       // setInspireFeatureTypeBoundingBox(collectionReference);
    }

    private void addECConformity() {
        Conformity interoperabilityConformity = new Conformity();
        CitationConformity citationConformity = new CitationConformity();
        citationConformity.setTitle(INSPIREConformity.INSPIRE_INTEROPERABILITY_CONFORMITY_TITLE);
        citationConformity.setDateOfCreation(INSPIREConformity.INSPIRE_INTEROPERABILITY_CONFORMITY_DATE);
        interoperabilityConformity.setSpecification(citationConformity);
        interoperabilityConformity.setDegree(DegreeOfConformity.NOT_EVALUATED);
        inspireExtendedCapabilitiesType.getConformity().clear();
        inspireExtendedCapabilitiesType.getConformity().add(interoperabilityConformity);
    }

    private void addECTemporalReference() {
        TemporalReference temporalReference = new TemporalReference();
        temporalReference.setDateOfCreation(cswHandler.inspireConfiguration.servicesDateOfCreation);
        inspireExtendedCapabilitiesType.getTemporalReference().clear();
        inspireExtendedCapabilitiesType.getTemporalReference().add(temporalReference);
    }

    private void addECResourceLocator(String serviceUrl) {
        ResourceLocatorType resourceLocatorType = new ResourceLocatorType();
        resourceLocatorType.setURL(serviceUrl + CSWConstant.CSW_GET_CAPABILITIES_PARAMETERS);
        inspireExtendedCapabilitiesType.getResourceLocator().clear();
        inspireExtendedCapabilitiesType.getResourceLocator().add(resourceLocatorType);
    }

    private void addECKeywords(List<CollectionReference> collections){
        // Add INSPIRE Madatory Keyword
        inspireExtendedCapabilitiesType.getMandatoryKeyword().clear();
        ClassificationOfSpatialDataService classificationOfSpatialDataService = new ClassificationOfSpatialDataService();
        classificationOfSpatialDataService.setKeywordValue(INSPIREConstants.CSW_MANDATORY_KEYWORD);
        inspireExtendedCapabilitiesType.getMandatoryKeyword().add(classificationOfSpatialDataService);
        List<io.arlas.server.model.Keyword> keywords = new ArrayList<>();
        if (collections != null) {
            collections.forEach(collectionReference -> {
                keywords.addAll(Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.keywords).orElse(new ArrayList<>()));
            });
        }
        inspireExtendedCapabilitiesType.getKeyword().clear();
        if(sections.contains("ServiceIdentification") || sections.contains("All")){
            capabilitiesType.getServiceIdentification().getKeywords().clear();
        }
        keywords.forEach(keyword -> {
            /* If keyword is in CLASSIFICATION_SPATIAL_DATA_SERVICES, set the vocabulary */
            try {
                KeywordValueEnum.valueOf(keyword.value);
                keyword.vocabulary = INSPIREConstants.CLASSIFICATION_SPATIAL_DATA_SERVICES;
                keyword.dateOfPublication = INSPIREConstants.DATE_CLASSIFICATION_SPATIAL_DATA_SERVICES;
            } catch (IllegalArgumentException e) {}
            /* Check if other keywords have a Controled vocabulary*/
            eu.europa.ec.inspire.schemas.common._1.Keyword inspireKeyword = new eu.europa.ec.inspire.schemas.common._1.Keyword();
            inspireKeyword.setKeywordValue(keyword.value);
            OriginatingControlledVocabulary vocabulary = new OriginatingControlledVocabulary();
            Optional.ofNullable(keyword.vocabulary).map(k -> {vocabulary.setTitle(keyword.vocabulary); return k;});
            Optional.ofNullable(keyword.dateOfPublication).map(k -> {vocabulary.setDateOfCreation(keyword.dateOfPublication); return k;});
            if (!Strings.isNullOrEmpty(keyword.vocabulary)) {
                inspireKeyword.setOriginatingControlledVocabulary(vocabulary);
            }
            inspireExtendedCapabilitiesType.getKeyword().add(inspireKeyword);
            if(sections.contains("ServiceIdentification") || sections.contains("All")){
                KeywordsType ke = new KeywordsType();
                LanguageStringType languageStringType = new LanguageStringType();
                languageStringType.setValue(keyword.value);
                ke.getKeyword().add(languageStringType);
                capabilitiesType.getServiceIdentification().getKeywords().add(ke);
            }
        });
    }

    private void addECMetadataPointOfContact() {
        MetadataPointOfContact metadataPointOfContact = new MetadataPointOfContact();
        String email = cswHandler.ogcConfiguration.serviceContactMail;
        String name = cswHandler.ogcConfiguration.serviceContactIndividualName;
        metadataPointOfContact.setEmailAddress(email);
        metadataPointOfContact.setOrganisationName(name);
        inspireExtendedCapabilitiesType.getMetadataPointOfContact().clear();
        inspireExtendedCapabilitiesType.getMetadataPointOfContact().add(metadataPointOfContact);
    }

    private void addECMetadataDate(List<CollectionReference> collections) throws INSPIREException {
        /* The latest date of all the collections is returned*/
        Date metadataDate = null;
        if (collections != null) {
            for(CollectionReference collectionReference : collections) {
                Date collectionDate = getMetadataDate(collectionReference.params.dublinCoreElementName.getDate());
                if (metadataDate == null || metadataDate.compareTo(collectionDate) < 0) {
                    metadataDate = collectionDate;
                }
            }
        }
        if (metadataDate == null) {
            throw new INSPIREException(INSPIREExceptionCode.MISSING_INSPIRE_METADATA, "Metadata date is missing", Service.CSW);
        } else {
            DateFormat df = new SimpleDateFormat(INSPIREConstants.CSW_METADATA_DATE_FORMAT);
            inspireExtendedCapabilitiesType.setMetadataDate(df.format(metadataDate));
        }
    }

    private void addECMetadataLanguage() throws INSPIREException{
        SupportedLanguagesType supportedLanguagesType = new SupportedLanguagesType();
        String defaultLanguage = cswHandler.cswConfiguration.serviceIdentificationLanguage;
        INSPIRECheckParam.checkLanguageInspireCompliance(defaultLanguage, Service.CSW);
        LanguageElementISO6392B defaultLanguageIso = new LanguageElementISO6392B();
        defaultLanguageIso.setLanguage(defaultLanguage);
        supportedLanguagesType.setDefaultLanguage(defaultLanguageIso);
        inspireExtendedCapabilitiesType.setSupportedLanguages(supportedLanguagesType);
        inspireExtendedCapabilitiesType.setResponseLanguage(defaultLanguageIso);
    }

    private void addExtendedCapabilities(List<CollectionReference> collections, String serviceUrl) throws INSPIREException {
        // Add INSPIRE Resource type
        inspireExtendedCapabilitiesType.setResourceType(ResourceType.SERVICE);
        // Add Resource locator
        addECResourceLocator(serviceUrl);
        // Add INSPIRE Spatial data service type
        inspireExtendedCapabilitiesType.setSpatialDataServiceType(SpatialDataServiceType.DISCOVERY);
        // Add INSPIRE keywords
        addECKeywords(collections);
        // Add INSPIRE Temporal Reference
        addECTemporalReference();
        // Add INSPIRE Conformity
        addECConformity();
        // Add INSPIRE Metadata Point Of Contact
        addECMetadataPointOfContact();
        // Add INSPIRE Metadata date
        addECMetadataDate(collections);
        // Add INSPIRE Metadata language
        addECMetadataLanguage();
        ExtendedCapabilities extendedCapabilities  = new ExtendedCapabilities();
        extendedCapabilities.setExtendedCapabilities(inspireExtendedCapabilitiesType);
        capabilitiesType.getOperationsMetadata().setExtendedCapabilities(extendedCapabilities);
    }

    private void setInspireServiceIdentification(String language) throws INSPIREException {
        // FOR NOW we just check if the language is correct but we always return the only language declared in the inspire configuration
        if (language != null) {
            INSPIRECheckParam.checkLanguageInspireCompliance(language, Service.WFS);
        }
        ServiceIdentification serviceIdentification = capabilitiesType.getServiceIdentification();

        // Add INSPIRE 'Resource Title'
        serviceIdentification.getTitle().clear();
        LanguageStringType title = new LanguageStringType();
        title.setValue(cswHandler.cswConfiguration.serviceIdentificationTitle);
        title.setLang(cswHandler.cswConfiguration.serviceIdentificationLanguage);
        serviceIdentification.getTitle().add(title);

        // Add INSPIRE 'Resource Abstract'
        serviceIdentification.getAbstract().clear();
        LanguageStringType abstractTitle = new LanguageStringType();
        abstractTitle.setValue(cswHandler.cswConfiguration.serviceIdentificationAbstract);
        abstractTitle.setLang(cswHandler.cswConfiguration.serviceIdentificationLanguage);

        // Add INSPIRE 'Conditions for Access and Use'
        String conditionsForAccessAndUse = Optional.ofNullable(cswHandler.inspireConfiguration).map(c -> c.accessAndUseConditions).map(String::toString).orElse(INSPIREConstants.NO_CONDITIONS_FOR_ACCESS_AND_USE);
        serviceIdentification.setFees(conditionsForAccessAndUse);

        // Add INSPIRE 'Limitations on Public Access'
        serviceIdentification.getAccessConstraints().clear();
        String limitationsOnPublicAccess = Optional.ofNullable(cswHandler.inspireConfiguration).map(c -> c.publicAccessLimitations).map(String::toString).orElse(INSPIREConstants.LIMITATION_ON_PUBLIC_ACCESS);
        serviceIdentification.getAccessConstraints().add(limitationsOnPublicAccess);

        capabilitiesType.setServiceIdentification(serviceIdentification);
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
        addressType.getElectronicMailAddress().clear();
        addressType.getElectronicMailAddress().add(ogcConfiguration.serviceContactMail);
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

    private void addOperation(String operationName, OperationsMetadata operationsMetadata ,DomainType[] parameters, DomainType[] constraints) {
        DCP dcp = new DCP();
        HTTP http = new HTTP();
        dcp.setHTTP(http);
        Operation operation = new Operation();
        operation.setName(operationName);
        operation.getDCP().add(dcp);
        Arrays.asList(parameters).forEach(parameter -> operation.getParameter().add(parameter));
        Arrays.asList(constraints).forEach(constraint -> operation.getConstraint().add(constraint));

        operationsMetadata.getOperation().add(operation);
    }



    private void setOperations(CapabilitiesType getCapabilitiesType, String url) {
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
        DomainType outputSchema = createDomain(OUTPUT_SCHEMA_DOMAIN_NAME,CSWConstant.SUPPORTED_CSW_OUTPUT_SCHEMA);
        //create outputFormat parameter
        DomainType outputFormat = createDomain(OUTPUT_FORMAT_DOMAIN_NAME,CSWConstant.SUPPORTED_CSW_OUTPUT_FORMAT);
        DomainType acceptFormats = createDomain(ACCEPT_FORMATS_DOMAIN_NAME,CSWConstant.SUPPORTED_CSW_ACCEPT_FORMATS);

        DomainType opensearch = createDomain(OPENSEARCH_DOMAIN_NAME, new String[]{url});

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
        String[] additionalQueryablesStringArray = Arrays.stream(AdditionalQueryables.values()).map(aq -> aq.value).toArray(String[]::new);
        String[] supportedIsoQueryablesStringArray = Arrays.stream(SupportedISOQueryables.values()).map(siq -> siq.value).toArray(String[]::new);
        DomainType additionalQueryables = createDomain("AdditionalQueryables", additionalQueryablesStringArray);
        DomainType supportedIsoQueryables = createDomain("SupportedISOQueryables", supportedIsoQueryablesStringArray);

        DomainType[] getRecordsConstraint = {opensearch, additionalQueryables, supportedIsoQueryables};

        addOperation(CSWRequestType.GetCapabilities.name() ,operationsMetadata, getCapabilitiesParameters,noParameters);
        addOperation(CSWRequestType.GetRecords.name(), operationsMetadata,noParameters,getRecordsConstraint);
        addOperation(CSWRequestType.GetRecordById.name(), operationsMetadata,noParameters,noParameters);
        //add  conformance
        addDefaultConformance(operationsMetadata);
        operationsMetadata.getConstraint().add(opensearch);
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
        QName envQname = new QName(GML.XML_NS, ENVELOPE, GML.XML_PREFIX);
        QName polyQname = new QName(GML.XML_NS, POLYGON, GML.XML_PREFIX);
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

    private DomainType createDomain(String domainName, String[] domainValues){

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
        addConformanceType(operationsMetadata, "OpenSearch", trueValueType);
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

    public Date getMetadataDate(String value) throws INSPIREException {
        try {
            return new SimpleDateFormat(INSPIREConstants.DUBLIN_CORE_DATE_FORMAT).parse(value);
        } catch (ParseException e) {
            throw new INSPIREException(INSPIREExceptionCode.INTERNAL_SERVER_ERROR, "Metadata date of collection is not well formatted", Service.CSW);
        }
    }
}
