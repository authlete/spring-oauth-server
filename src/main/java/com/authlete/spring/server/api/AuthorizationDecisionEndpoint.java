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
import java.util.Date;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.springframework.stereotype.Component;
import com.authlete.common.api.AuthleteApiFactory;
import com.authlete.common.types.User;
import com.authlete.jakarta.BaseAuthorizationDecisionEndpoint;
import com.authlete.spring.server.db.UserDao;


/**
 * The endpoint that receives a request from the form in the authorization page.
 */
@Component
@Path("/authorization/decision")
public class AuthorizationDecisionEndpoint extends BaseAuthorizationDecisionEndpoint
{
    /**
     * Process a request from the form in the authorization page.
     *
     * <p>
     * NOTE:
     * A better implementation would re-display the authorization page
     * when the pair of login ID and password is wrong, but this
     * implementation does not do it for brevity. A much better
     * implementation would check the login credentials by Ajax.
     * </p>
     *
     * @param request
     *         A request from the form in the authorization page.
     *
     * @return
     *         A response to the user agent. Basically, the response
     *         will trigger redirection to the client's redirect
     *         endpoint.
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
        MultivaluedMap<String, String> parameters
                = createMultivaluedMap(request.getParameterMap());

        // Get the existing session.
        HttpSession session = getSession(request);

        // Retrieve some variables from the session. See the implementation
        // of AuthorizationRequestHandlerSpiImpl.getAuthorizationPage().
        String   ticket       = (String)  takeAttribute(session, "ticket");
        String[] claimNames   = (String[])takeAttribute(session, "claimNames");
        String[] claimLocales = (String[])takeAttribute(session, "claimLocales");
        User user             = getUser(session, parameters);
        Date authTime         = (Date)session.getAttribute("authTime");

        // Handle the end-user's decision.
        return handle(AuthleteApiFactory.getDefaultApi(),
                new AuthorizationDecisionHandlerSpiImpl(parameters, user, authTime),
                ticket, claimNames, claimLocales);
    }


    /**
     * Get the existing session.
     */
    private HttpSession getSession(HttpServletRequest request)
    {
        // Get the existing session.
        HttpSession session = request.getSession(false);

        // If there exists a session.
        if (session != null)
        {
            // OK.
            return session;
        }

        // A session does not exist. Make a response of "400 Bad Request".
        String message = "A session does not exist.";

        Response response = Response
                .status(Status.BAD_REQUEST)
                .entity(message)
                .type(MediaType.TEXT_PLAIN)
                .build();

        throw new WebApplicationException(message, response);
    }


    /**
     * Look up an end-user.
     */
    private static User getUser(HttpSession session, MultivaluedMap<String, String> parameters)
    {
        // Look up the user in the session to see if they're already logged in.
        User sessionUser = (User)session.getAttribute("user");

        if (sessionUser != null)
        {
            return sessionUser;
        }

        // Look up an end-user who has the login credentials.
        User loginUser = UserDao.getByCredentials(parameters.getFirst("loginId"),
                parameters.getFirst("password"));

        if (loginUser != null)
        {
            session.setAttribute("user", loginUser);
            session.setAttribute("authTime", new Date());
        }

        return loginUser;
    }


    /**
     * Get the value of an attribute from the given session and
     * remove the attribute from the session after the retrieval.
     */
    protected Object takeAttribute(HttpSession session, String key)
    {
        // Retrieve the value from the session.
        Object value = session.getAttribute(key);

        // Remove the attribute from the session.
        session.removeAttribute(key);

        // Return the value of the attribute.
        return value;
    }
}
