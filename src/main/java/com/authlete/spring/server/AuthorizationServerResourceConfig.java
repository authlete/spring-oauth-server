/*
 * Copyright (C) 2017 Authlete, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package com.authlete.spring.server;


import jakarta.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;
import com.authlete.spring.server.api.AuthorizationDecisionEndpoint;
import com.authlete.spring.server.api.AuthorizationEndpoint;
import com.authlete.spring.server.api.IntrospectionEndpoint;
import com.authlete.spring.server.api.JwksEndpoint;
import com.authlete.spring.server.api.RevocationEndpoint;
import com.authlete.spring.server.api.TokenEndpoint;


/**
 * Configuration for Jersey.
 *
 * <p>
 * Note that the Jersey servlet is registered and mapped to
 * {@code "/*"} by default unless a different path is specified
 * by &#x40;ApplicationPath annotation.
 * </p>
 *
 * <p>
 * Mapping the Jersey servlet at the root would hide the Spring
 * dispatcher servlet because the dispatcher servlet is mapped
 * at the root by default, too.
 * </p>
 *
 * <p>
 * If the Spring dispatcher servlet were hidden, static contents
 * such as {@code index.html} and CSS files would not be served
 * in the way as described in Spring Boot Reference Guide.
 * </p>
 *
 * <p>
 * Therefore, this configuration class specifies {@code "/api"}
 * as the value of &#x40;ApplicationPath annotation. This means
 * that all the APIs handled by the Jersey servlet are placed
 * under {@code "/api"}.
 * </p>
 *
 * <p>
 * Special note. {@code ConfigurationEndpoint} is not managed
 * by the Jersey servlet because the endpoint must be served at
 * {@code "/.well-known/openid-configuration"}. To be exact,
 * the endpoint must be served at {@code
 * "<i>{Issuer-Identifier}</i>/.well-known/openid-configuration"}.
 * See <a href=
 * "https://openid.net/specs/openid-connect-discovery-1_0.html"
 * >OpenID Connect Discovery 1.0</a> for details about the
 * requirement.
 * </p>
 */
@Component
@ApplicationPath("/api")
public class AuthorizationServerResourceConfig extends ResourceConfig
{
    public AuthorizationServerResourceConfig()
    {
        // Register endpoints. Note that ConfigurationEndpoint
        // is not registered here because the endpoint must be
        // served at "/.well-known/openid-configuration".
        registerClasses(
                AuthorizationEndpoint.class,
                AuthorizationDecisionEndpoint.class,
                IntrospectionEndpoint.class,
                JwksEndpoint.class,
                RevocationEndpoint.class,
                TokenEndpoint.class
        );
    }
}
