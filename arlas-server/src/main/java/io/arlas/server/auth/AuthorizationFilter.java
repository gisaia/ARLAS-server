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
package io.arlas.server.auth;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import io.arlas.server.app.ArlasAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);
    private final JWTVerifier jwtVerifier;
    private final ArlasAuthConfiguration authConf;

    private static final String CLAIM_PERMISSIONS = "http://arlas.io/permissions";

    public AuthorizationFilter(ArlasAuthConfiguration conf) throws Exception {
        this.authConf = conf;
        this.jwtVerifier = JWT.require(Algorithm.RSA256(getPemPublicKey(conf.certificateFile), null)).build();
    }

    @Override
    public void filter(ContainerRequestContext ctx) {
        try {
            // header presence and format already checked before in AuthenticationFilter
            DecodedJWT jwt = jwtVerifier.verify(ctx.getHeaderString(HttpHeaders.AUTHORIZATION).substring(7));
            Claim jwtClaim = jwt.getClaim(CLAIM_PERMISSIONS);
            if (!jwtClaim.isNull()) {
                ArlasClaims arlasClaims = new ArlasClaims(jwtClaim.asList(String.class));
                if (arlasClaims.isAllowed(ctx.getMethod(), ctx.getUriInfo().getPath())) {
                    arlasClaims.injectHeaders(ctx.getHeaders());
                    return;
                }
            }
        } catch (JWTVerificationException e){
            LOGGER.warn("JWT verification failed.", e);
        }

        ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                .build());
    }

    /**
     * Extract RSA public key from a PEM file containing an X.509 certificate.
     * @param filename
     * @return
     * @throws Exception
     */
    private RSAPublicKey getPemPublicKey(String filename) throws Exception {
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        try (FileInputStream is = new FileInputStream (filename)) {
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            return (RSAPublicKey) cer.getPublicKey();
        }
    }
}
