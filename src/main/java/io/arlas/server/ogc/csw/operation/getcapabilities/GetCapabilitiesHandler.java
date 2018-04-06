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
import io.arlas.server.ogc.common.utils.CSWConstant;
import io.arlas.server.ogc.csw.CSWHandler;
import io.arlas.server.ogc.csw.utils.CSWRequestType;
import io.arlas.server.ogc.wfs.utils.WFSConstant;
import net.opengis.cat.csw._3.CapabilitiesType;
import net.opengis.ows._2.*;

import javax.xml.bind.JAXBElement;
import java.util.Arrays;

public class GetCapabilitiesHandler {

    private static final String TRUE ="TRUE";
    private static final String FALSE ="FALSE";
    private static final String[] SECTION_NAMES = {"ServiceIdentification","ServiceProvider","Filter_Capabilities"};
    private static final String SECTION_DOMAIN_NAME = "Sections";
    private static final String ACCEPT_VERSIONS_DOMAIN_NAME ="AcceptVersions";
    private static final String RESOLVE_DOMAIN_NAME ="resolve";
    private static final String LOCAL_VALUE ="local";


    public CSWHandler cswHandler;
    public OGCConfiguration ogcConfiguration;

    public CapabilitiesType getCapabilitiesType = new CapabilitiesType();

    protected ValueType trueValueType = new ValueType();
    protected ValueType falseValueType = new ValueType();



    public GetCapabilitiesHandler(CSWHandler cswHandler) {

        this.cswHandler = cswHandler;
        this.ogcConfiguration = cswHandler.ogcConfiguration;

        getCapabilitiesType.setVersion(WFSConstant.SUPPORTED_WFS_VERSION);

        trueValueType.setValue(TRUE);
        falseValueType.setValue(FALSE);
        setServiceProvider();
        setServiceIdentification();
        setOperations();
    }

    public JAXBElement<CapabilitiesType> getCSWCapabilitiesResponse() {
        return cswHandler.cswFactory.createCapabilities(getCapabilitiesType);
    }

    private void setServiceProvider() {
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

    private void setServiceIdentification() {
        ServiceIdentification serviceIdentification = new ServiceIdentification();
        CodeType codeType = new CodeType();
        codeType.setValue(CSWConstant.CSW);
        serviceIdentification.setServiceType(codeType);
        serviceIdentification.getServiceTypeVersion().add(CSWConstant.SUPPORTED_CSW_VERSION);
        getCapabilitiesType.setServiceIdentification(serviceIdentification);
    }

    public void setOperationsUrl(String url) {
        getCapabilitiesType.getOperationsMetadata().getOperation().forEach(op -> {
            HTTP http = new HTTP();
            RequestMethodType requestMethodType = new RequestMethodType();
            requestMethodType.setHref(url);
            JAXBElement<RequestMethodType> get = cswHandler.owsFactory.createHTTPGet(requestMethodType);
            http.getGetOrPost().add(get);
            op.getDCP().get(0).setHTTP(http);
        });
    }

    private void addSection(String sectionName,DomainType sections){
        ValueType serviceIdentification = new ValueType();
        serviceIdentification.setValue(sectionName);
        sections.getAllowedValues().getValueOrRange().add(serviceIdentification);
    }

    private void addOperation(String operationName,OperationsMetadata operationsMetadata,DomainType... parameters){
        DCP dcp = new DCP();
        HTTP http = new HTTP();
        dcp.setHTTP(http);
        Operation operation = new Operation();
        operation.setName(operationName);
        operation.getDCP().add(dcp);
        Arrays.asList(parameters).forEach(parameter->operation.getParameter().add(parameter));
        operationsMetadata.getOperation().add(operation);
    }

    private void setOperations() {
        OperationsMetadata operationsMetadata = new OperationsMetadata();
        DomainType[] noParameters = {};
        //create AcceptVersions parameter for GetCapabilities operation
        DomainType acceptVersions = new DomainType();
        acceptVersions.setName(ACCEPT_VERSIONS_DOMAIN_NAME);
        acceptVersions.setAllowedValues(new AllowedValues());
        ValueType version = new ValueType();
        version.setValue(CSWConstant.SUPPORTED_CSW_VERSION);
        acceptVersions.getAllowedValues().getValueOrRange().add(version);
        //create sections parameter for GetCapabilities operation
        DomainType sections = new DomainType();
        sections.setName(SECTION_DOMAIN_NAME);
        sections.setAllowedValues(new AllowedValues());
        //add sections
        Arrays.asList(SECTION_NAMES).forEach(sectionName->addSection(sectionName,sections));
        //create resolve parameter for GetFeature and GetPropertyValue operation
        DomainType resolve = new DomainType();
        resolve.setName(RESOLVE_DOMAIN_NAME);
        resolve.setAllowedValues(new AllowedValues());
        ValueType local = new ValueType();
        local.setValue(LOCAL_VALUE);
        resolve.getAllowedValues().getValueOrRange().add(local);
        //add  operations
        DomainType[] getCapabilitiesParameters = {acceptVersions,sections};
        addOperation(CSWRequestType.GetCapabilities.name(),operationsMetadata,getCapabilitiesParameters);
        addOperation(CSWRequestType.GetRecords.name(),operationsMetadata,noParameters);
        addOperation(CSWRequestType.GetRecordById.name(),operationsMetadata,noParameters);

        //add  conformance
        getCapabilitiesType.setOperationsMetadata(operationsMetadata);
    }

}