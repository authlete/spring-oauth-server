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


import static com.authlete.jakarta.util.JaxRsUtils.createMultivaluedMap;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ViewResolver;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jakarta.BaseAuthorizationEndpoint;


/**
 * Authorization endpoint which supports OAuth 2&#x2E;0 and OpenID Connect.
 *
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.1"
 *      >RFC 6749, 3.1. Authorization Endpoint</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#AuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.1.2. Authorization Endpoint (Authorization Code Flow)</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#ImplicitAuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.2.2. Authorization Endpoint (Implicit Flow)</a>
 *
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html#HybridAuthorizationEndpoint"
 *      >OpenID Connect Core 1.0, 3.3.2. Authorization Endpoint (Hybrid Flow)</a>
 */
@Component
@Path("/authorization")
public class AuthorizationEndpoint extends BaseAuthorizationEndpoint
{
    @Autowired
    @Qualifier("freeMarkerViewResolver")
    private ViewResolver mViewResolver;


    /**
     * The authorization endpoint for {@code GET} method.
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-3.1">RFC 6749,
     * 3.1 Authorization Endpoint</a> says that the authorization endpoint
     * MUST support {@code GET} method.
     * </p>
     *
     * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.1"
     *      >RFC 6749, 3.1 Authorization Endpoint</a>
     */
    @GET
    public Response get(@Context HttpServletRequest request)
    {
        // Handle the authorization request.
        return handle(request);
    }


    /**
     * The authorization endpoint for {@code POST} method.
     *
     * <p>
     * <a href="http://tools.ietf.org/html/rfc6749#section-3.1">RFC 6749,
     * 3.1 Authorization Endpoint</a> says that the authorization endpoint
     * MAY support {@code POST} method.
     * </p>
     *
     * <p>
     * In addition, <a href=
     * "http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest"
     * >OpenID Connect Core 1.0, 3.1.2.1. Authentication Request</a> says
     * that the authorization endpoint MUST support {@code POST} method.
     * </p>
     */
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(@Context HttpServletRequest request)
    {
        // Directly mapping the request body to a MultivaluedMap
        // instance fails for some reasons under Spring Framework.
        // Therefore, HttpServletRequest.getParameterMap() is used
        // in handle(HttpServletRequest) method. A problem of
        // getParameterMap() is that a Map instance returned from
        // the method contains both form parameters and query
        // parameters.
        //
        // To comply with the specification strictly, this endpoint
        // should return an error when the request contains query
        // parameters.

        // Handle the authorization request.
        return handle(request);
    }


    private Response handle(HttpServletRequest request)
    {
        // Handle the authorization request.
        return handle(AuthleteApiFactory.getDefaultApi(),
                new AuthorizationRequestHandlerSpiImpl(request, mViewResolver),
                createMultivaluedMap(request.getParameterMap()));
    }
}
