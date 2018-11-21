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

package io.arlas.server.ogc.csw.utils;

import io.arlas.server.app.INSPIREConfiguration;
import io.arlas.server.app.OGCConfiguration;
import io.arlas.server.exceptions.OGC.OGCException;
import io.arlas.server.inspire.common.constants.InspireConstants;
import io.arlas.server.model.CollectionReference;
import io.arlas.server.model.DublinCoreElementName;
import io.arlas.server.model.InspireConformity;
import io.arlas.server.model.Keyword;
import io.arlas.server.model.enumerations.AccessConstraintEnum;
import io.arlas.server.ogc.common.utils.OGCConstant;
import net.opengis.gml._3.CodeType;
import org.isotc211._2005.gco.*;
import org.isotc211._2005.gmd.*;
import org.isotc211._2005.gmd.ObjectFactory;
import org.isotc211._2005.srv.SVServiceIdentificationType;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class MDMetadataBuilder {

    private static final String SCOPE_CODE_URL = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_ScopeCode";
    private static final String DATE_TYPE_CODE_LIST = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_DateTypeCode";
    private static final String ROLE_CODE_LIST = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#CI_RoleCode";
    private static final String RESTRICTION_CODE_LIST = "http://standards.iso.org/ittf/PubliclyAvailableStandards/ISO_19139_Schemas/resources/codelist/ML_gmxCodelists.xml#MD_RestrictionCode";

    private static final String DATE_TYPE = "publication";


    public static final ObjectFactory gmdObjectFactory = new ObjectFactory();
    public static final org.isotc211._2005.gco.ObjectFactory gcoObjectFactory = new org.isotc211._2005.gco.ObjectFactory();


    public static MDMetadataType getBriefMDMetadata(CollectionReference collectionReference) {
        MDMetadataType mdMetadataType = new MDMetadataType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        addFileIdentifier(mdMetadataType, dublinCoreElementName.identifier);
        addHierarchyLevel(mdMetadataType, dublinCoreElementName.type);
        addBriefIdentificationInfo(mdMetadataType, dublinCoreElementName);
        return mdMetadataType;
    }

    public static MDMetadataType getSummaryMDMetadata(CollectionReference collectionReference, OGCConfiguration ogcConfiguration, INSPIREConfiguration inspireConfiguration) throws OGCException {
        MDMetadataType mdMetadataType = new MDMetadataType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        addFileIdentifier(mdMetadataType, dublinCoreElementName.identifier);
        addLanguage(mdMetadataType,dublinCoreElementName.language);
        addHierarchyLevel(mdMetadataType, dublinCoreElementName.type);
        addDateStamp(mdMetadataType, dublinCoreElementName.getDate());
        addMetadataStandardName(mdMetadataType);
        addSummaryIdentificationInfo(mdMetadataType, collectionReference, ogcConfiguration, inspireConfiguration);
        addDistibutionInfo(mdMetadataType, collectionReference, ogcConfiguration, ElementSetName.summary);
        return mdMetadataType;
    }

    public static MDMetadataType getFullMDMetadata(CollectionReference collectionReference, OGCConfiguration ogcConfiguration, INSPIREConfiguration inspireConfiguration) throws OGCException {
        MDMetadataType mdMetadataType = new MDMetadataType();
        DublinCoreElementName dublinCoreElementName = collectionReference.params.dublinCoreElementName;
        addFileIdentifier(mdMetadataType, dublinCoreElementName.identifier);
        addLanguage(mdMetadataType,dublinCoreElementName.language);
        addHierarchyLevel(mdMetadataType, dublinCoreElementName.type);
        addContact(mdMetadataType, ogcConfiguration);
        addDateStamp(mdMetadataType, dublinCoreElementName.getDate());
        addMetadataStandardName(mdMetadataType);
        addSummaryIdentificationInfo(mdMetadataType, collectionReference, ogcConfiguration, inspireConfiguration);
        addDistibutionInfo(mdMetadataType, collectionReference, ogcConfiguration, ElementSetName.full);
        addDataQuality(mdMetadataType, collectionReference, ogcConfiguration);
        return mdMetadataType;
    }

    public static void addDistibutionInfo(MDMetadataType mdMetadataType, CollectionReference collectionReference, OGCConfiguration ogcConfiguration, ElementSetName elementSetName) {
        MDDistributionPropertyType mdDistributionPropertyType = new MDDistributionPropertyType();
        MDDistributionType mdDistributionType = new MDDistributionType();
        String format = Optional.ofNullable(collectionReference.params.dublinCoreElementName.format).filter(f -> !f.equals("")).orElse("unknown");
        addDistributionFormat(mdDistributionType, format);
        addTransferOptions(mdDistributionType, collectionReference, ogcConfiguration, elementSetName);
        mdDistributionPropertyType.setMDDistribution(mdDistributionType);
        mdMetadataType.setDistributionInfo(mdDistributionPropertyType);
    }

    public static void addDataQuality(MDMetadataType mdMetadataType, CollectionReference collectionReference, OGCConfiguration ogcConfiguration) {
        DQDataQualityPropertyType dataQualityPropertyType = new DQDataQualityPropertyType();
        DQDataQualityType dataQualityType = new DQDataQualityType();
        DQScopePropertyType dqScopePropertyType = new DQScopePropertyType();
        DQScopeType dqScopeType = new DQScopeType();
        MDScopeCodePropertyType mdScopeCodePropertyType = createScope(collectionReference.params.dublinCoreElementName.type);
        dqScopeType.setLevel(mdScopeCodePropertyType);
        dqScopePropertyType.setDQScope(dqScopeType);
        dataQualityType.setScope(dqScopePropertyType);
        DQElementPropertyType networkConformity = createDomainConsistency(InspireConformity.INSPIRE_NETWORK_SERVICES_CONFORMITY_TITLE, InspireConformity.INSPIRE_NETWORK_SERVICES_CONFORMITY_DATE, DATE_TYPE);
        DQElementPropertyType metadataConformity = createDomainConsistency(InspireConformity.INSPIRE_METADATA_CONFORMITY_TITLE, InspireConformity.INSPIRE_METADATA_CONFORMITY_DATE, DATE_TYPE);
        dataQualityType.getReport().add(networkConformity);
        dataQualityType.getReport().add(metadataConformity);
        dataQualityPropertyType.setDQDataQuality(dataQualityType);
        mdMetadataType.getDataQualityInfo().add(dataQualityPropertyType);
    }

    public static void addContact(MDMetadataType mdMetadataType, OGCConfiguration ogcConfiguration) {
        mdMetadataType.getContact().add(createResponsableParty(ogcConfiguration));
    }

    public static void addDistributionFormat(MDDistributionType mdDistributionType, String format) {
        MDFormatPropertyType mdFormatPropertyType = new MDFormatPropertyType();
        MDFormatType mdFormatType = new MDFormatType();
        mdFormatType.setName(createCharacterStringPropertyType(format));
        mdFormatPropertyType.setMDFormat(mdFormatType);
        mdDistributionType.getDistributionFormat().add(mdFormatPropertyType);
    }
    public static void addTransferOptions(MDDistributionType mdDistributionType, CollectionReference collectionReference, OGCConfiguration ogcConfiguration, ElementSetName elementSetName) {
        MDDigitalTransferOptionsPropertyType mdDigitalTransferOptionsPropertyType = new MDDigitalTransferOptionsPropertyType();
        MDDigitalTransferOptionsType mdDigitalTransferOptionsType = new MDDigitalTransferOptionsType();
        CIOnlineResourcePropertyType ciOnlineResourcePropertyType = new CIOnlineResourcePropertyType();
        CIOnlineResourceType ciOnlineResourceType = new CIOnlineResourceType();
        URLPropertyType urlPropertyType = new URLPropertyType();
        urlPropertyType.setURL(ogcConfiguration.serverUri + "ogc/wfs/" + collectionReference.collectionName + "/?" + OGCConstant.WFS_GET_CAPABILITIES_PARAMETERS);
        ciOnlineResourceType.setLinkage(urlPropertyType);
        if (elementSetName == ElementSetName.full) {
            ciOnlineResourceType.setProtocol(createCharacterStringPropertyType("OGC:WFS"));
            String description = Optional.ofNullable(collectionReference.params.inspire)
                    .map(inspire -> inspire.inspireURI)
                    .map(inspireURI -> inspireURI.code)
                    .orElse("ARLAS.INSPIRE." + collectionReference.collectionName);
            ciOnlineResourceType.setName(createCharacterStringPropertyType(collectionReference.collectionName));
            ciOnlineResourceType.setDescription(createCharacterStringPropertyType(description));
        }
        ciOnlineResourcePropertyType.setCIOnlineResource(ciOnlineResourceType);
        mdDigitalTransferOptionsType.getOnLine().add(ciOnlineResourcePropertyType);
        mdDigitalTransferOptionsPropertyType.setMDDigitalTransferOptions(mdDigitalTransferOptionsType);
        mdDistributionType.getTransferOptions().add(mdDigitalTransferOptionsPropertyType);
    }


    public static void addAbstract(SVServiceIdentificationType svServiceIdentificationType, String description) {
        svServiceIdentificationType.setAbstract(createCharacterStringPropertyType(description));
    }

    public static void addPointOfContact(SVServiceIdentificationType svServiceIdentificationType, OGCConfiguration ogcConfiguration) {
        CIResponsiblePartyPropertyType ciResponsiblePartyPropertyType = createResponsableParty(ogcConfiguration);
        svServiceIdentificationType.getPointOfContact().add(ciResponsiblePartyPropertyType);
    }

    public static void addKeywords(SVServiceIdentificationType svServiceIdentificationType, CollectionReference collectionReference) {
        List<Keyword> keywords = collectionReference.params.inspire.keywords;
        if (keywords != null) {
            for (Keyword keyword : keywords) {
                MDKeywordsPropertyType mdKeywordsPropertyType = new MDKeywordsPropertyType();
                MDKeywordsType mdKeywordsType = new MDKeywordsType();
                mdKeywordsType.getKeyword().add(createCharacterStringPropertyType(keyword.value));
                if (keyword.vocabulary != null && !keyword.vocabulary.equals("")) {
                    mdKeywordsType.setThesaurusName(createCICitation(keyword.vocabulary, keyword.dateOfPublication, DATE_TYPE));
                }
                mdKeywordsPropertyType.setMDKeywords(mdKeywordsType);
                svServiceIdentificationType.getDescriptiveKeywords().add(mdKeywordsPropertyType);
            }
        }
    }

    public static void addResourceConstraints(SVServiceIdentificationType svServiceIdentificationType, CollectionReference collectionReference, INSPIREConfiguration inspireConfiguration) {
        MDConstraintsPropertyType mdConstraintsPropertyType = new MDConstraintsPropertyType();
        MDConstraintsType mdConstraintsType = new MDConstraintsType();
        mdConstraintsType.getUseLimitation().add(createCharacterStringPropertyType(inspireConfiguration.accessAndUseConditions));
        mdConstraintsPropertyType.setMDConstraints(gmdObjectFactory.createMDConstraints(mdConstraintsType));
        svServiceIdentificationType.getResourceConstraints().add(mdConstraintsPropertyType);

        MDConstraintsPropertyType mdLegalConstraintsPropertyType = new MDConstraintsPropertyType();
        MDLegalConstraintsType mdLegalConstraintsType = new MDLegalConstraintsType();
        MDRestrictionCodePropertyType mdRestrictionCodePropertyType = new MDRestrictionCodePropertyType();
        String legalConstraint = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireLimitationAccess)
                .map(inspireLimitationAccess -> inspireLimitationAccess.accessConstraints).get();

        CodeListValueType restrictionCodeListValueType = new CodeListValueType();
        restrictionCodeListValueType.setCodeList(RESTRICTION_CODE_LIST);
        restrictionCodeListValueType.setCodeListValue(legalConstraint);
        mdRestrictionCodePropertyType.setMDRestrictionCode(restrictionCodeListValueType);
        mdLegalConstraintsType.getAccessConstraints().add(mdRestrictionCodePropertyType);
        if (legalConstraint.equals(AccessConstraintEnum.otherRestrictions.name())) {
            String otherConstraint = Optional.ofNullable(collectionReference.params.inspire).map(inspire -> inspire.inspireLimitationAccess)
                    .map(inspireLimitationAccess -> inspireLimitationAccess.otherConstraints).orElse(InspireConstants.LIMITATION_ON_PUBLIC_ACCESS);
            mdLegalConstraintsType.getOtherConstraints().add(createCharacterStringPropertyType(otherConstraint));
        }
        mdLegalConstraintsPropertyType.setMDConstraints(gmdObjectFactory.createMDConstraints(mdLegalConstraintsType));
        svServiceIdentificationType.getResourceConstraints().add(mdLegalConstraintsPropertyType);
    }


    public static void addDateStamp(MDMetadataType mdMetadataType, String date) {
        DatePropertyType datePropertyType = new DatePropertyType();
        datePropertyType.setDate(date);
        mdMetadataType.setDateStamp(datePropertyType);
    }
    public static void addFileIdentifier(MDMetadataType mdMetadataType, String identifier) {
        mdMetadataType.setFileIdentifier(createCharacterStringPropertyType(identifier));
    }

    public static void addLanguage(MDMetadataType mdMetadataType, String language) {
        mdMetadataType.setLanguage(createCharacterStringPropertyType(language));
    }

    public static void addMetadataStandardName(MDMetadataType mdMetadataType) {
        mdMetadataType.setMetadataStandardName(createCharacterStringPropertyType("ISO19115"));
    }


    public static void addHierarchyLevel(MDMetadataType mdMetadataType, String level) {
        MDScopeCodePropertyType mdScopeCodePropertyType = createScope(level);
        mdMetadataType.getHierarchyLevel().add(mdScopeCodePropertyType);
    }

    private static DQElementPropertyType createDomainConsistency(String specification, String date, String dateType) {
        DQDomainConsistencyType dqDomainConsistencyType = new DQDomainConsistencyType();
        DQConformanceResultType dqConformanceResultType = new DQConformanceResultType();
        CICitationPropertyType ciSpecification = createCICitation(specification, date, dateType);
        dqConformanceResultType.setSpecification(ciSpecification);
        DQResultPropertyType dqResultPropertyType = new DQResultPropertyType();
        dqResultPropertyType.setAbstractDQResult(gmdObjectFactory.createDQConformanceResult(dqConformanceResultType));
        dqDomainConsistencyType.getResult().add(dqResultPropertyType);
        DQElementPropertyType dqElementPropertyType = new DQElementPropertyType();
        dqElementPropertyType.setAbstractDQElement(gmdObjectFactory.createDQDomainConsistency(dqDomainConsistencyType));

        return dqElementPropertyType;
    }

    public static void addBriefIdentificationInfo(MDMetadataType mdMetadataType, DublinCoreElementName dublinCoreElementName) {
        MDIdentificationPropertyType mdIdentificationPropertyType = new MDIdentificationPropertyType();
        SVServiceIdentificationType svServiceIdentificationType = new SVServiceIdentificationType();
        addCICitation(svServiceIdentificationType, dublinCoreElementName.title, null, null);
        addExtent(svServiceIdentificationType, dublinCoreElementName.bbox);
        addServiceType(svServiceIdentificationType);
        org.isotc211._2005.srv.ObjectFactory o = new org.isotc211._2005.srv.ObjectFactory();
        mdIdentificationPropertyType.setAbstractMDIdentification(o.createSVServiceIdentification(svServiceIdentificationType));
        mdMetadataType.getIdentificationInfo().add(mdIdentificationPropertyType);
    }

    public static void addSummaryIdentificationInfo(MDMetadataType mdMetadataType, CollectionReference collectionReference, OGCConfiguration ogcConfiguration, INSPIREConfiguration inspireConfiguration) throws OGCException {
        MDIdentificationPropertyType mdIdentificationPropertyType = new MDIdentificationPropertyType();
        SVServiceIdentificationType svServiceIdentificationType = new SVServiceIdentificationType();
        String formatedDate = formatDublinCoreDate(collectionReference.params.dublinCoreElementName.getDate());
        addCICitation(svServiceIdentificationType, collectionReference.params.dublinCoreElementName.title, formatedDate, DATE_TYPE);
        addAbstract(svServiceIdentificationType, collectionReference.params.dublinCoreElementName.description);
        addPointOfContact(svServiceIdentificationType, ogcConfiguration);
        addKeywords(svServiceIdentificationType, collectionReference);
        addResourceConstraints(svServiceIdentificationType, collectionReference, inspireConfiguration);
        addExtent(svServiceIdentificationType, collectionReference.params.dublinCoreElementName.bbox);
        addServiceType(svServiceIdentificationType);
        org.isotc211._2005.srv.ObjectFactory o = new org.isotc211._2005.srv.ObjectFactory();
        mdIdentificationPropertyType.setAbstractMDIdentification(o.createSVServiceIdentification(svServiceIdentificationType));
        mdMetadataType.getIdentificationInfo().add(mdIdentificationPropertyType);
    }


    public static void addCICitation(SVServiceIdentificationType svServiceIdentificationType, String title, String date, String dateType) {
        CICitationPropertyType ciCitationPropertyType = createCICitation(title, date, dateType);
        svServiceIdentificationType.setCitation(ciCitationPropertyType);
    }

    public static void addServiceType(SVServiceIdentificationType svServiceIdentificationType) {
        GenericNamePropertyType genericNamePropertyType = new GenericNamePropertyType();
        CodeType codeType = new CodeType();
        codeType.setValue("download");
        QName _CodeType_QNAME = new QName("http://www.isotc211.org/2005/gco", "LocalName");
        JAXBElement<CodeType> codeTypeJAXBElement = new JAXBElement<CodeType>(_CodeType_QNAME, CodeType.class, null, codeType);
        genericNamePropertyType.setAbstractGenericName(codeTypeJAXBElement);
        svServiceIdentificationType.setServiceType(genericNamePropertyType);
    }

    public static void addExtent(SVServiceIdentificationType svServiceIdentificationType, DublinCoreElementName.Bbox bbox) {
        EXExtentPropertyType exExtentPropertyType = new EXExtentPropertyType();
        EXExtentType exExtentType = new EXExtentType();
        EXGeographicExtentPropertyType exGeographicExtentPropertyType = new EXGeographicExtentPropertyType();
        EXGeographicBoundingBoxType exGeographicBoundingBoxType = new EXGeographicBoundingBoxType();
        exGeographicBoundingBoxType.setEastBoundLongitude(createDecimalPropertyType(bbox.east));
        exGeographicBoundingBoxType.setWestBoundLongitude(createDecimalPropertyType(bbox.west));
        exGeographicBoundingBoxType.setNorthBoundLatitude(createDecimalPropertyType(bbox.north));
        exGeographicBoundingBoxType.setSouthBoundLatitude(createDecimalPropertyType(bbox.south));
        exGeographicExtentPropertyType.setAbstractEXGeographicExtent(gmdObjectFactory.createAbstractEXGeographicExtent(exGeographicBoundingBoxType));
        exExtentType.getGeographicElement().add(exGeographicExtentPropertyType);
        exExtentPropertyType.setEXExtent(exExtentType);
        svServiceIdentificationType.getExtent().add(exExtentPropertyType);
    }

    private static String formatDublinCoreDate(String date) throws OGCException {
        Date dublincoreDate = CSWParamsParser.getMetadataDate(date);
        DateFormat df = new SimpleDateFormat(InspireConstants.CSW_METADATA_DATE_FORMAT);
        return df.format(dublincoreDate);
    }

    private static CharacterStringPropertyType createCharacterStringPropertyType(String value) {
        CharacterStringPropertyType characterStringPropertyType = new CharacterStringPropertyType();
        characterStringPropertyType.setCharacterString(gcoObjectFactory.createCharacterString(value));
        return characterStringPropertyType;
    }

    private static MDScopeCodePropertyType createScope(String level) {
        MDScopeCodePropertyType mdScopeCodePropertyType = new MDScopeCodePropertyType();
        CodeListValueType codeListValueType =  new CodeListValueType();
        codeListValueType.setCodeList(SCOPE_CODE_URL);
        codeListValueType.setCodeListValue(level);
        mdScopeCodePropertyType.setMDScopeCode(codeListValueType);
        return mdScopeCodePropertyType;
    }

    private static CIResponsiblePartyPropertyType createResponsableParty(OGCConfiguration ogcConfiguration) {
        CIResponsiblePartyPropertyType ciResponsiblePartyPropertyType = new CIResponsiblePartyPropertyType();
        CIResponsiblePartyType ciResponsiblePartyType = new CIResponsiblePartyType();
        ciResponsiblePartyType.setOrganisationName(createCharacterStringPropertyType(ogcConfiguration.serviceContactIndividualName));
        CIContactPropertyType ciContactPropertyType = new CIContactPropertyType();
        CIContactType ciContactType = new CIContactType();
        CIAddressPropertyType ciAddressPropertyType = new CIAddressPropertyType();
        CIAddressType ciAddressType = new CIAddressType();
        ciAddressType.setCity(createCharacterStringPropertyType(ogcConfiguration.serviceContactAdressCity));
        ciAddressType.setCountry(createCharacterStringPropertyType(ogcConfiguration.serviceContactAdressCountry));
        ciAddressType.setPostalCode(createCharacterStringPropertyType(ogcConfiguration.serviceContactAdressPostalCode));
        ciAddressPropertyType.setCIAddress(ciAddressType);
        ciContactType.setAddress(ciAddressPropertyType);
        ciContactPropertyType.setCIContact(ciContactType);
        ciResponsiblePartyType.setContactInfo(ciContactPropertyType);
        CIRoleCodePropertyType ciRoleCodePropertyType = new CIRoleCodePropertyType();
        CodeListValueType codeListValueType = new CodeListValueType();
        codeListValueType.setCodeList(ROLE_CODE_LIST);
        codeListValueType.setCodeListValue(ogcConfiguration.serviceProviderRole);
        ciRoleCodePropertyType.setCIRoleCode(codeListValueType);
        ciResponsiblePartyType.setRole(ciRoleCodePropertyType);
        ciResponsiblePartyPropertyType.setCIResponsibleParty(ciResponsiblePartyType);
        return ciResponsiblePartyPropertyType;
    }

    private static CICitationPropertyType createCICitation(String title, String date, String dateType) {
        CICitationPropertyType ciCitationPropertyType = new CICitationPropertyType();
        CICitationType ciCitationType = new CICitationType();
        CharacterStringPropertyType cs = createCharacterStringPropertyType(title);
        ciCitationType.setTitle(cs);
        if(date != null && !date.equals("")) {
            CIDatePropertyType ciDatePropertyType = new CIDatePropertyType();
            CIDateType ciDateType = new CIDateType();
            DatePropertyType datePropertyType = new DatePropertyType();
            datePropertyType.setDate(date);
            CIDateTypeCodePropertyType ciDateTypeCodePropertyType = new CIDateTypeCodePropertyType();
            CodeListValueType codeListValueType = new CodeListValueType();
            codeListValueType.setCodeList(DATE_TYPE_CODE_LIST);
            codeListValueType.setCodeListValue(dateType);
            ciDateTypeCodePropertyType.setCIDateTypeCode(codeListValueType);
            ciDateType.setDate(datePropertyType);
            ciDateType.setDateType(ciDateTypeCodePropertyType);
            ciDatePropertyType.setCIDate(ciDateType);
            ciCitationType.getDate().add(ciDatePropertyType);
        }
        ciCitationPropertyType.setCICitation(ciCitationType);
        return ciCitationPropertyType;
    }

    private static DecimalPropertyType createDecimalPropertyType(double value) {
        DecimalPropertyType decimalPropertyType = new DecimalPropertyType();
        decimalPropertyType.setDecimal(BigDecimal.valueOf(value));
        return decimalPropertyType;
    }

}
