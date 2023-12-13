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
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.springframework.stereotype.Component;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.jakarta.BaseRevocationEndpoint;


/**
 * Revocation endpoint which supports RFC 7009.
 *
 * @see <a href="http://tools.ietf.org/html/rfc7009"
 *      >RFC 7009, OAuth 2.0 Token Revocation</a>
 */
@Component
@Path("/revocation")
public class RevocationEndpoint extends BaseRevocationEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @Context HttpServletRequest request)
    {
        // Directly mapping the request body to a MultivaluedMap
        // instance fails for some reasons under Spring Framework.
        // Therefore, HttpServletRequest.getParameterMap() is used
        // here. A problem of getParameterMap() is that a Map
        // instance returned from the method contains both form
        // parameters and query parameters.
        //
        // To comply with the specification strictly, this endpoint
        // should return an error when the request contains query
        // parameters.

        // Handle the revocation request.
        return handle(
                AuthleteApiFactory.getDefaultApi(),
                createMultivaluedMap(request.getParameterMap()),
                authorization);
    }
}
