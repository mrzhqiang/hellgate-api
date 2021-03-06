package com.github.mrzhqiang.hellgate.config;


import com.github.mrzhqiang.hellgate.website.WebsiteProperties;
import com.github.mrzhqiang.kaptcha.autoconfigure.KaptchaAuthenticationConverter;
import com.github.mrzhqiang.kaptcha.autoconfigure.KaptchaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.boot.autoconfigure.security.SecurityProperties.BASIC_AUTH_ORDER;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration {

    @Configuration
    @Order(BASIC_AUTH_ORDER - 10)
    static class ApiSecurityAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatchers()
                    .antMatchers("/api/**")
                    .and()
                    .authorizeRequests().anyRequest().authenticated()
                    .and().httpBasic()
                    .and().csrf().disable();

            // todo jwt authentication
        }
    }

    @EnableConfigurationProperties(WebsiteProperties.class)
    @Configuration
    static class WebsiteSecurityAdapter extends WebSecurityConfigurerAdapter {

        private final WebsiteProperties websiteProperties;
        private final KaptchaProperties kaptchaProperties;
        private final KaptchaAuthenticationConverter converter;
        private final UserDetailsService userDetailsService;

        public WebsiteSecurityAdapter(WebsiteProperties websiteProperties, KaptchaProperties kaptchaProperties,
                                      KaptchaAuthenticationConverter converter, UserDetailsService userDetailsService) {
            this.websiteProperties = websiteProperties;
            this.kaptchaProperties = kaptchaProperties;
            this.converter = converter;
            this.userDetailsService = userDetailsService;
        }

        @Override
        public void configure(WebSecurity web) {
            web.ignoring().antMatchers(websiteProperties.getIgnorePath());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.addFilterAfter(getAuthenticationFilter(), AnonymousAuthenticationFilter.class)
                    .userDetailsService(userDetailsService)
                    .authorizeRequests()
                    .antMatchers(kaptchaProperties.getPath()).permitAll()
                    .antMatchers(websiteProperties.getPublicPath()).permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .formLogin().loginPage("/login").permitAll()
                    .defaultSuccessUrl(websiteProperties.getDefaultSuccessUrl())
                    .and()
                    .logout().permitAll();
            if (websiteProperties.getRememberMe()) {
                http.rememberMe();
            }
        }

        private AuthenticationFilter getAuthenticationFilter() throws Exception {
            AuthenticationFilter filter = new AuthenticationFilter(authenticationManager(), converter);
            filter.setRequestMatcher(new AntPathRequestMatcher("/register", HttpMethod.POST.name()));
            filter.setFailureHandler(new SimpleUrlAuthenticationFailureHandler("/register?error"));
            return filter;
        }
    }
}
