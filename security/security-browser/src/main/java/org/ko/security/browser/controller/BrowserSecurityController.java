package org.ko.security.browser.controller;


import org.apache.commons.lang.StringUtils;
import org.ko.security.browser.support.SimpleResponse;
import org.ko.security.browser.support.SocialUserInfo;
import org.ko.security.core.properties.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.web.ProviderSignInUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

@RestController
public class BrowserSecurityController {

    private static final Logger _LOGGER = LoggerFactory.getLogger(BrowserSecurityController.class);

    /**
     * 请求的缓存
     */
    private RequestCache requestCache = new HttpSessionRequestCache();

    /**
     *
     */
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();


    @Autowired private SecurityProperties securityProperties;

    @Autowired private ProviderSignInUtils providerSignInUtils;


    /**
     * 当需要身份认证时跳转这里
     * @param request
     * @param response
     * @return
     */
    @RequestMapping("/authentication/require")
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public SimpleResponse requireAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {

        /**
         * 拿到引发跳转的请求
         */
        SavedRequest savedRequest = requestCache.getRequest(request, response);

        if (Objects.nonNull(savedRequest)) {
            String targetUrl = savedRequest.getRedirectUrl();
            _LOGGER.info("引发跳转的URL是: {}", targetUrl);

            if (StringUtils.endsWithIgnoreCase(targetUrl, ".html")) {
                //如果发起的请求是一个html
                redirectStrategy.sendRedirect(request, response, securityProperties.getBrowser().getLoginPage());
            }

        }
        return new SimpleResponse("访问的服务需要身份认证, 请引导用户到登陆页!");

    }

    @GetMapping("social/user")
    public SocialUserInfo getSocialUserInfo (HttpServletRequest request) {
        SocialUserInfo userInfo = new SocialUserInfo();
        Connection connection = providerSignInUtils.getConnectionFromSession(new ServletWebRequest(request));
        userInfo.setProviderId(connection.getKey().getProviderId());
        userInfo.setProviderUserId(connection.getKey().getProviderUserId());
        userInfo.setNickname(connection.getDisplayName());
        userInfo.setAvator(connection.getImageUrl());
        return userInfo;
    }

    @GetMapping("session/invalid")
    @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
    public SimpleResponse sessionInvalid () {
        return new SimpleResponse("session失效");
    }

}
