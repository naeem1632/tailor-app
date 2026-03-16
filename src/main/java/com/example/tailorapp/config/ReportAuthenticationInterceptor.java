package com.example.tailorapp.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class ReportAuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        Boolean isAuthenticated = (Boolean) session.getAttribute("reportAuthenticated");

        if (isAuthenticated == null || !isAuthenticated) {
            // Store the originally requested URL to redirect after login
            String requestedUrl = request.getRequestURI();
            if (request.getQueryString() != null) {
                requestedUrl += "?" + request.getQueryString();
            }
            session.setAttribute("requestedReportUrl", requestedUrl);

            // Redirect to login page
            response.sendRedirect("/reports/login");
            return false;
        }

        return true;
    }
}