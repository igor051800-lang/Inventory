package com.inventory.deva_inventory.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfig {

    // Spring 6 no longer matches "/api/roles/" to "/api/roles"; keep the Boot 2 behavior.
    @Bean
    public FilterRegistrationBean<UrlHandlerFilter> trailingSlashFilter() {
        FilterRegistrationBean<UrlHandlerFilter> registration = new FilterRegistrationBean<>(
                UrlHandlerFilter.trailingSlashHandler("/api/**").wrapRequest().build());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
