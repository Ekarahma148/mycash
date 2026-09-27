package com.project.mycash.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // halaman publik
       if (uri.equals("/")
        || uri.startsWith("/login")
        || uri.startsWith("/register")
        || uri.startsWith("/css")
        || uri.startsWith("/js")
        || uri.startsWith("/images")
        || uri.startsWith("/favicon")) {
    return true;
}

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
