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
import io.arlas.server.utils.StringUtil;
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
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.stream.Collectors;

@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    private final Logger LOGGER = LoggerFactory.getLogger(AuthorizationFilter.class);
    private final JWTVerifier jwtVerifier;
    private final ArlasAuthConfiguration authConf;

    public AuthorizationFilter(ArlasAuthConfiguration conf) throws Exception {
        this.authConf = conf;
        this.jwtVerifier = JWT.require(Algorithm.RSA256(getPemPublicKey(conf), null)).build();
    }

    @Override
    public void filter(ContainerRequestContext ctx) {

        String header = ctx.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.toLowerCase().startsWith("bearer ")) {
            // Check if endpoint is public and if the verb is authorize
            if (ctx.getUriInfo().getPath().concat(":").concat(ctx.getMethod()).matches(authConf.getPublicRegex()) || ctx.getMethod() == "OPTIONS") {
                return;
            }
        } else {
            // if a token is provided
            // if method !== options
            // verify token and pass it
            try {
                // header presence and format already checked before in AuthenticationFilter
                DecodedJWT jwt = jwtVerifier.verify(header.substring(7));

                ctx.getHeaders().remove(authConf.headerUser); // remove it in case it's been set manually
                String userId = jwt.getSubject();
                if (!StringUtil.isNullOrEmpty(userId)) {
                    ctx.getHeaders().putSingle(authConf.headerUser, userId);
                }

                ctx.getHeaders().remove(authConf.headerGroup); // remove it in case it's been set manually
                Claim jwtClaimRoles = jwt.getClaim(authConf.claimRoles);
                if (!jwtClaimRoles.isNull()) {
                    ctx.getHeaders().put(authConf.headerGroup,
                            jwtClaimRoles.asList(String.class)
                                    .stream()
                                    .filter(r -> r.toLowerCase().startsWith("group"))
                                    .collect(Collectors.toList())
                    );
                }
                Claim jwtClaimPermissions = jwt.getClaim(authConf.claimPermissions);
                if (!jwtClaimPermissions.isNull()) {
                    ArlasClaims arlasClaims = new ArlasClaims(jwtClaimPermissions.asList(String.class));
                    if (arlasClaims.isAllowed(ctx.getMethod(), ctx.getUriInfo().getPath())) {
                        arlasClaims.injectHeaders(ctx.getHeaders());
                        return;
                    }
                }
            } catch (JWTVerificationException e) {
                LOGGER.warn("JWT verification failed.", e);
            }
        }
        ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                .build());
    }

    /**
     * Extract RSA public key from a PEM file containing an X.509 certificate.
     *
     * @param conf
     * @return
     * @throws Exception
     */
    private RSAPublicKey getPemPublicKey(ArlasAuthConfiguration conf) throws Exception {
        CertificateFactory fact = CertificateFactory.getInstance("X.509");
        try (InputStream is = getCertificateStream(conf)) {
            X509Certificate cer = (X509Certificate) fact.generateCertificate(is);
            return (RSAPublicKey) cer.getPublicKey();
        }
    }

    private InputStream getCertificateStream(ArlasAuthConfiguration conf) throws Exception {
        if (!StringUtil.isNullOrEmpty(conf.certificateUrl)) {
            return new URL(conf.certificateUrl).openStream();
        } else {
            LOGGER.warn("Configuration 'arlas_auth.certificate_file' is deprecated. Consider using 'arlas_auth.certificate_url'.");
            return new FileInputStream(conf.certificateFile);
        }
    }
}
