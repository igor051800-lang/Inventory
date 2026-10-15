package com.inventory.deva_inventory.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring 6 no longer matches "/api/roles/" to "/api/roles"; keep the Spring 5 behavior for existing clients.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    @SuppressWarnings("deprecation")
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseTrailingSlashMatch(true);
    }
}
