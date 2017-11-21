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


import static com.authlete.jaxrs.util.JaxRsUtils.createMultivaluedMap;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.springframework.stereotype.Component;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.web.BasicCredentials;
import com.authlete.jaxrs.BaseIntrospectionEndpoint;


/**
 * Introspection endpoint which supports RFC 7662.
 *
 * @see <a href="http://tools.ietf.org/html/rfc7662"
 *      >RFC 7662, OAuth 2.0 Token Introspection</a>
 */
@Component
@Path("/introspection")
public class IntrospectionEndpoint extends BaseIntrospectionEndpoint
{
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response post(
            @HeaderParam(HttpHeaders.AUTHORIZATION) String authorization,
            @Context HttpServletRequest request)
    {
        // "2.1. Introspection Request" in RFC 7662 says as follows:
        //
        //   To prevent token scanning attacks, the endpoint MUST also require
        //   some form of authorization to access this endpoint, such as client
        //   authentication as described in OAuth 2.0 [RFC6749] or a separate
        //   OAuth 2.0 access token such as the bearer token described in OAuth
        //   2.0 Bearer Token Usage [RFC6750].  The methods of managing and
        //   validating these authentication credentials are out of scope of this
        //   specification.
        //
        // Therefore, this API must be protected in some way or other.
        // Basic Authentication and Bearer Token are typical means, and
        // both use the value of the 'Authorization' header.
        //
        // Authenticate the API caller.
        boolean authenticated = authenticateApiCaller(authorization);

        // If the API caller does not have necessary privileges to call this API.
        if (authenticated == false)
        {
            // Return "401 Unauthorized".
            return Response.status(Status.UNAUTHORIZED).build();
        }

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

        // Handle the introspection request.
        return handle(
                AuthleteApiFactory.getDefaultApi(),
                createMultivaluedMap(request.getParameterMap()));
    }


    /**
     * Authenticate the API caller.
     *
     * @param authorization
     *         The value of the {@code Authorization} header of the API call.
     *
     * @return
     *         True if the API caller has necessary privileges to access
     *         the introspection endpoint.
     */
    private boolean authenticateApiCaller(String authorization)
    {
        // TODO: This implementation is for demonstration purpose only.

        // If the Authorization header contains "Basic Authentication" and
        // if the user part is "nobody".
        BasicCredentials credentials = BasicCredentials.parse(authorization);
        if (credentials != null && "nobody".equals(credentials.getUserId()))
        {
            // Reject the introspection request by "nobody".
            return false;
        }

        // Accept anybody except "nobody".
        return true;
    }
}
