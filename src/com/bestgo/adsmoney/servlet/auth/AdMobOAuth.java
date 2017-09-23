package com.bestgo.adsmoney.servlet.auth;

import com.bestgo.adsmoney.Config;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.extensions.servlet.auth.oauth2.AbstractAuthorizationCodeServlet;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.util.Collections;

@WebServlet(name = "AdMobOAuth", urlPatterns = {"/auth/admob_oauth2"})
public class AdMobOAuth extends AbstractAuthorizationCodeServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendRedirect("/auth/success.html");
    }

    @Override
    protected com.google.api.client.auth.oauth2.AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                new JacksonFactory(),
                Config.ADMOB_CLIENT_ID, Config.ADMOB_CLIENT_SECRET,
                Collections.singleton(Config.ADSENSE_READONLY)).setDataStoreFactory(
                new FileDataStoreFactory(Config.DATA_STORE_DIR))
                .setApprovalPrompt("force")
                .setAccessType("offline")
                .build();
    }

    @Override
    protected String getRedirectUri(HttpServletRequest httpServletRequest) throws ServletException, IOException {
        GenericUrl url = new GenericUrl(httpServletRequest.getRequestURL().toString());
        url.setRawPath("/auth/admob_oauth2_callback");
        return url.build();
    }

    @Override
    protected String getUserId(HttpServletRequest httpServletRequest) throws ServletException, IOException {
        String account = httpServletRequest.getParameter("account");
        HttpSession session = httpServletRequest.getSession();
        session.setAttribute("oauth_account", account);
        return account;
    }
}
