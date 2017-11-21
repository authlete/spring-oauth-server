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
package com.authlete.spring.server.api;


import javax.ws.rs.core.Response;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jaxrs.BaseConfigurationEndpoint;


/**
 * Configuration endpoint which supports OpenID Connect Discovery 1&#x2E;0.
 *
 * <p>
 * An OpenID Provider that supports <a href=
 * "http://openid.net/specs/openid-connect-discovery-1_0.html">OpenID Connect
 * Discovery 1.0</a> must provide an endpoint that returns its configuration
 * information in a JSON format. Details about the format are described in
 * "<a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata"
 * >3. OpenID Provider Metadata</a>" in OpenID Connect Discovery 1.0.
 * </p>
 *
 * <p>
 * Note that the URI of an OpenID Provider configuration endpoint is defined in
 * "<a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationRequest"
 * >4.1. OpenID Provider Configuration Request</a>" in OpenID Connect Discovery
 * 1.0. In short, the URI must be:
 * </p>
 *
 * <blockquote>
 * Issuer Identifier + {@code /.well-known/openid-configuration}
 * </blockquote>
 *
 * <p>
 * <i>Issuer Identifier</i> is a URL to identify an OpenID Provider. For example,
 * {@code https://example.com}. For details about Issuer Identifier, See <b>{@code issuer}</b>
 * in "<a href="http://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata"
 * >3. OpenID Provider Metadata</a>" (OpenID Connect Discovery 1.0) and <b>{@code iss}</b> in
 * "<a href="http://openid.net/specs/openid-connect-core-1_0.html#IDToken">2. ID Token</a>"
 * (OpenID Connect Core 1.0).
 * </p>
 *
 * <p>
 * You can change the Issuer Identifier of your service using the management console
 * (<a href="https://www.authlete.com/documents/so_console">Service Owner Console</a>).
 * Note that the default value of Issuer Identifier is not appropriate for commercial
 * use, so you should change it.
 * </p>
 *
 * @see <a href="http://openid.net/specs/openid-connect-discovery-1_0.html"
 *      >OpenID Connect Discovery 1.0</a>
 */
@RestController
public class ConfigurationEndpoint extends BaseConfigurationEndpoint
{
    /**
     * OpenID Provider configuration endpoint.
     */
    @RequestMapping(
            value  = "/.well-known/openid-configuration",
            method = RequestMethod.GET)
    public String get()
    {
        // Handle the configuration request.
        Response response = handle(AuthleteApiFactory.getDefaultApi());

        // Return the configuration information.
        return response.getEntity().toString();
    }
}
