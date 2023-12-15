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


import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import com.authlete.common.dto.AuthorizationResponse;
import com.authlete.common.types.Prompt;
import com.authlete.common.types.User;
import com.authlete.jakarta.AuthorizationPageModel;
import com.authlete.jakarta.spi.AuthorizationRequestHandlerSpiAdapter;


/**
 * Implementation of {@link com.authlete.jakarta.spi.AuthorizationRequestHandlerSpi
 * AuthorizationRequestHandlerSpi} interface which needs to be given
 * to the constructor of {@link com.authlete.jakarta.AuthorizationRequestHandler
 * AuthorizationRequestHandler}.
 *
 * <p>
 * Note: The current implementation implements only {@link
 * #generateAuthorizationPage(AuthorizationResponse) generateAuthorizationPage()}
 * method. Other methods need to be implemented only when you want to support
 * {@code prompt=none} in authorization requests. See <a href=
 * "http://openid.net/specs/openid-connect-core-1_0.html#AuthRequest">3.1.2.1.
 * Authentication Request</a> in <a href=
 * "http://openid.net/specs/openid-connect-core-1_0.html">OpenID Connect Core
 * 1.0</a> for details about {@code prompt=none}.
 * </p>
 */
class AuthorizationRequestHandlerSpiImpl extends AuthorizationRequestHandlerSpiAdapter
{
    /**
     * {@code "text/html;charset=UTF-8"}
     */
    private static final MediaType MEDIA_TYPE_HTML =
            MediaType.TEXT_HTML_TYPE.withCharset("UTF-8");


    /**
     * The page template to ask the resource owner for authorization.
     */
    private static final String VIEW_NAME = "authorization";


    /**
     * Authorization request to the authorization endpoint.
     */
    private final HttpServletRequest mRequest;


    /**
     * View resolver.
     */
    private final ViewResolver mViewResolver;


    /**
     * Constructor with an authorization request to the authorization endpoint.
     */
    public AuthorizationRequestHandlerSpiImpl(HttpServletRequest request, ViewResolver viewResolver)
    {
        mRequest      = request;
        mViewResolver = viewResolver;
    }


    @Override
    public Response generateAuthorizationPage(AuthorizationResponse info)
    {
        // Add some data into the session.
        HttpSession session = setUpSession(info);

        // Prepare a model object to feed data into the view.
        Map<String, Object> model = prepareModel(info, session);

        try
        {
            // Get the 'View' to render the authorization page.
            View view = getView();

            // Build the authorization page.
            String page = render(view, model);

            // Return the authorization page.
            return prepareSuccessResponse(page);
        }
        catch (Exception e)
        {
            // Failed to build the authorization page.
            e.printStackTrace();

            // Return an error response.
            return prepareErrorResponse(e);
        }
    }


    private HttpSession setUpSession(AuthorizationResponse info)
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // Store some variables into the session so that they can be
        // referred to later in AuthorizationDecisionEndpoint.
        session.setAttribute("ticket",       info.getTicket());
        session.setAttribute("claimNames",   info.getClaims());
        session.setAttribute("claimLocales", info.getClaimsLocales());

        // Clear the current user information in the session if necessary.
        clearCurrentUserInfoInSessionIfNecessary(info, session);

        return session;
    }


    private Map<String, Object> prepareModel(
            AuthorizationResponse info, HttpSession session)
    {
        // Get the user from the session if they exist.
        User user = (User)session.getAttribute("user");

        // Prepare a model object which contains information needed to
        // render the authorization page. Feel free to create a subclass
        // of AuthorizationPageModel or define another different class
        // according to what you need in the authorization page.
        AuthorizationPageModel model = new AuthorizationPageModel(info, user);

        // Wrap the model to pass it to View.render() method.
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("model", model);

        return map;
    }


    private View getView() throws Exception
    {
        // Get the 'View' to render the authorization page.
        return mViewResolver.resolveViewName(VIEW_NAME, Locale.US);
    }


    private String render(View view, Map<String, Object> model) throws Exception
    {
        // Prepare a dummy HttpServletResponse instance.
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Render the authorization page.
        view.render(model, mRequest, response);

        // Extract the result (HTML) as a String.
        return response.getContentAsString();
    }


    private Response prepareSuccessResponse(String page)
    {
        // 200 OK / HTML
        return Response
                .ok(page, MEDIA_TYPE_HTML)
                .header("Cache-Control", "no-store")
                .build();
    }


    private Response prepareErrorResponse(Exception e)
    {
        // Error message.
        String message = new StringBuilder("Failed to build the authorization page: ")
                .append(e.getMessage()).toString();

        // 500 Internal Server Error / Plain Text
        return Response
                .status(Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.TEXT_PLAIN_TYPE)
                .entity(message)
                .build();
    }


    @Override
    public boolean isUserAuthenticated()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // Get the user from the session if they exist.
        User user = (User)session.getAttribute("user");

        // If the user information exists in the session, the user is already
        // authenticated; Otherwise, the user is not authenticated.
        return user != null;
    }


    @Override
    public long getUserAuthenticatedAt()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // Get the user from the session if they exist.
        Date authTime = (Date)session.getAttribute("authTime");

        if (authTime == null)
        {
            return 0;
        }

        return authTime.getTime() / 1000L;
    }


    @Override
    public String getUserSubject()
    {
        // Create an HTTP session.
        HttpSession session = mRequest.getSession(true);

        // Get the user from the session if they exist.
        User user = (User)session.getAttribute("user");

        if (user == null)
        {
            return null;
        }

        return user.getSubject();
    }


    private void clearCurrentUserInfoInSessionIfNecessary(AuthorizationResponse info, HttpSession session)
    {
        // Get the user from the session if they exist.
        User user     = (User)session.getAttribute("user");
        Date authTime = (Date)session.getAttribute("authTime");

        if (user == null || authTime == null)
        {
            // The information about the user does not exist in the session.
            return;
        }

        // Check 'prompts'.
        checkPrompts(info, session);

        // Check 'authentication age'.
        checkAuthenticationAge(info, session, authTime);
    }


    private void checkPrompts(AuthorizationResponse info, HttpSession session)
    {
        if (info.getPrompts() == null)
        {
            return;
        }

        List<Prompt> prompts = Arrays.asList(info.getPrompts());

        if (prompts.contains(Prompt.LOGIN))
        {
            // Force a login by clearing out the current user.
            clearCurrentUserInfoInSession(session);
        };
    }


    private void checkAuthenticationAge(AuthorizationResponse info, HttpSession session, Date authTime)
    {
        // TODO: max_age == 0 effectively means "log in the user interactively
        // now" but it's used here as a flag, we should fix this to use Integer
        // instead of int probably.
        if (info.getMaxAge() <= 0)
        {
            return;
        }

        Date now = new Date();

        // Calculate number of seconds that have elapsed since login.
        long authAge = (now.getTime() - authTime.getTime()) / 1000L;

        if (authAge > info.getMaxAge())
        {
            // Session age is too old, clear out the current user.
            clearCurrentUserInfoInSession(session);
        };
    }


    private void clearCurrentUserInfoInSession(HttpSession session)
    {
        session.removeAttribute("user");
        session.removeAttribute("authTime");
    }
}
