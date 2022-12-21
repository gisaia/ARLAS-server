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

package io.arlas.filter.impl;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.arlas.commons.config.ArlasAuthConfiguration;
import io.arlas.commons.utils.StringUtil;
import io.arlas.filter.core.PolicyEnforcer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.Provider;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

@Provider
@Priority(Priorities.AUTHORIZATION)
/*
  This is the policy enforcer to be used with Auth0 in Arlas Server.
  Set ARLAS_AUTH_POLICY_CLASS=io.arlas.filter.impl.Auth0PolicyEnforcer
 */
public class Auth0PolicyEnforcer extends AbstractPolicyEnforcer {
    private final Logger LOGGER = LoggerFactory.getLogger(Auth0PolicyEnforcer.class);
    private JWTVerifier jwtVerifier;

    public Auth0PolicyEnforcer() {
        this.injectPermissions = false; // permissions are in Auth0
    }

    @Override
    public PolicyEnforcer setAuthConf(ArlasAuthConfiguration conf) throws Exception {
        super.setAuthConf(conf);
        this.jwtVerifier = JWT.require(Algorithm.RSA256(getPemPublicKey(), null)).acceptLeeway(3).build();
        return this;
    }

    @Override
    protected Object getObjectToken(String accessToken, String orgFilter) {
        LOGGER.debug("accessToken=" + decodeToken(accessToken));
        return jwtVerifier.verify(accessToken);
    }

    /**
     * Extract RSA public key from a PEM file containing an X.509 certificate.
     */
    private RSAPublicKey getPemPublicKey() throws Exception {
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        try (InputStream is = getCertificateStream()) {
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            return (RSAPublicKey) cer.getPublicKey();
        }
    }

    private InputStream getCertificateStream() throws Exception {
        if (!StringUtil.isNullOrEmpty(this.authConf.certificateUrl)) {
            return new URL(this.authConf.certificateUrl).openStream();
        } else {
            LOGGER.warn("Configuration 'arlas_auth.certificate_file' is deprecated. Consider using 'arlas_auth.certificate_url'.");
            return new FileInputStream(this.authConf.certificateFile);
        }
    }
}
