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

package io.arlas.server.rest.explore.opensearch.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "OpenSearchDescription", namespace ="http://a9.com/-/spec/opensearch/1.1/")
public class OpenSearchDescription {

    @XmlElement(name = "shortName", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String shortName = "";

    @XmlElement(name = "Description", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String description = "";

    @XmlElement(name = "Url", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public List<Url> url = new ArrayList<>();

    @XmlElement(name = "Contact", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String contact = "";

    @XmlElement(name = "Tags", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String tags = "";

    @XmlElement(name = "LongName", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String longName = "";

    @XmlElement(name = "Image", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public Image image;

    @XmlElement(name = "Developer", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String developer = "";

    @XmlElement(name = "Attribution", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String attribution = "";

    @XmlElement(name = "SyndicationRight", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String syndicationRight;

    @XmlElement(name = "AdultContent", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String adultContent = "";

    @XmlElement(name = "Language", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String language = "";

    @XmlElement(name = "InputEncoding", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String inputEncoding = "";

    @XmlElement(name = "OutputEncoding", namespace ="http://a9.com/-/spec/opensearch/1.1/")
    public String outputEncoding = "";
}
