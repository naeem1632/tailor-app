package com.example.tailorapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ReportAuthenticationInterceptor reportAuthenticationInterceptor;

    public WebConfig(ReportAuthenticationInterceptor reportAuthenticationInterceptor) {
        this.reportAuthenticationInterceptor = reportAuthenticationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(reportAuthenticationInterceptor)
                .addPathPatterns("/print/report/**", "/profit-analysis/report/**", "/reports/dashboard", "/reports/payment-report", "/reports/profit-analysis")
                .excludePathPatterns("/reports/login", "/reports/logout");
    }
}