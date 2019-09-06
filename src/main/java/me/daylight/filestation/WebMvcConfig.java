package me.daylight.filestation;

import me.daylight.filestation.authority.AuthorityInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Daylight
 * @date 2018/12/30 16:56
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Bean
    public AuthorityInterceptor authorityInterceptor(){
        return new AuthorityInterceptor();
    }

    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authorityInterceptor());
    }
}
