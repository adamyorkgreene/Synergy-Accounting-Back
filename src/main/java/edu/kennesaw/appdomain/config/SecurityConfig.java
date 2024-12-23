package edu.kennesaw.appdomain.config;

import edu.kennesaw.appdomain.filters.SessionDebugFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository())
                        .ignoringRequestMatchers(
                                "/", "/login", "/register", "/forgot-password", "/api/csrf",
                                "/api/users/login", "/api/users/register", "/api/users/request-password-reset",
                                "/api/users/verify", "/api/users/confirm-user", "/api/users/verify-request",
                                "/api/users/logout", "/logout"
                        )
                )
                .addFilterAfter(new SessionDebugFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers(
                                "/", "/login", "/register", "/verify", "/password-reset", "/forgot-password",
                                "/api/csrf", "/api/users/login", "/api/users/register", "/api/users/password-reset",
                                "/api/users/request-password-reset", "/api/users/verify-request",
                                "/api/users/confirm-user", "/api/users/verify"
                        ).permitAll()
                        // Authenticated `/api/users/` endpoints
                        .requestMatchers("/api/users/validate", "/api/users/dashboard").authenticated()
                        // Admin-specific endpoints
                        .requestMatchers("/api/admin/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                        // Manager-specific endpoints
                        .requestMatchers("/api/manager/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                        // Email-related endpoints
                        .requestMatchers("/api/email/**").hasAnyRole("ADMINISTRATOR", "MANAGER", "ACCOUNTANT")
                        // Dashboard-specific endpoints
                        .requestMatchers("/api/dashboard/**").hasAnyRole("USER", "ACCOUNTANT", "ADMINISTRATOR", "MANAGER")
                        // Chart-of-accounts endpoints
                        .requestMatchers("/api/chart-of-accounts").hasAnyRole("ADMINISTRATOR", "MANAGER", "ACCOUNTANT")
                        .requestMatchers("/api/chart-of-accounts/{accountNumber}").hasAnyRole("ADMINISTRATOR", "MANAGER", "ACCOUNTANT")
                        .requestMatchers("/api/chart-of-accounts/add-account").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/chart-of-accounts/add-transaction").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/chart-of-accounts/add-journal-entry").hasAnyRole("ADMINISTRATOR", "MANAGER")
                        .requestMatchers("/api/chart-of-accounts/request-journal-entry").hasAnyRole("ACCOUNTANT", "ADMINISTRATOR", "MANAGER")
                        .requestMatchers("/dashboard/general-ledger").hasAnyRole("ACCOUNTANT", "ADMINISTRATOR", "MANAGER")
                        .requestMatchers("/api/chart-of-accounts/update-transaction").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/chart-of-accounts/update-account").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/chart-of-accounts/delete-transaction").hasRole("ADMINISTRATOR")
                        .requestMatchers("/api/chart-of-accounts/update-activation").hasRole("ADMINISTRATOR")
                        // Any other requests
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/users/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (request.getSession(false) != null) {
                                request.getSession().invalidate();
                            }
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Logged out successfully\"}");
                            response.getWriter().flush();
                        })
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository csrfTokenRepository = new HttpSessionCsrfTokenRepository();
        csrfTokenRepository.setParameterName("_csrf");
        return csrfTokenRepository;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setSameSite("None");
        serializer.setUseSecureCookie(true);
        serializer.setCookiePath("/");
        serializer.setDomainName("synergyaccounting.app");
        return serializer;
    }
}

/*import edu.kennesaw.appdomain.filters.SessionDebugFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.filter.ForwardedHeaderFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.csrfTokenRepository(csrfTokenRepository())
                        .ignoringRequestMatchers("/", "/login", "/register", "/forgot-password", "/api/csrf",
                        "/api/users/login", "/api/users/request-password-reset", "/api/users/register",
                                "/api/users/verify", "/verify", "/verify-success", "/verify-fail",
                                "/api/users/confirm-user", "/confirm-user", "/confirm-success", "/confirm-fail"))
                .addFilterAfter(new SessionDebugFilter(), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth ->
                        auth.requestMatchers("/api/users/login", "/", "/login", "/register", "/api/users/register", "/verify", "/password-reset", "/confirm-user",
                                        "/api/users/verify", "/api/users/verify-request", "/api/users/password-reset", "/api/users/request-password-reset",
                                        "/api/users/request-confirm-user", "/api/users/confirm-user", "/api/csrf", "/api/accounts/approve-journal-entry", "/approve-journal-entry").permitAll()
                        .requestMatchers("/dashboard", "/api/users/dashboard","/api/users/validate", "/upload-image", "/api/dashboard/**").authenticated()
                                .requestMatchers("/api/admin/**").hasRole("ADMINISTRATOR")
                                .requestMatchers("/api/manager/**").hasAnyRole("ADMINISTRATOR", "MANAGER")
                                .requestMatchers("/api/email/**").hasAnyRole("ADMINISTRATOR", "MANAGER", "ACCOUNTANT")
                                .requestMatchers("/api/dashboard/**").hasAnyRole("USER", "ACCOUNTANT", "ADMINISTRATOR", "MANAGER")
                                .requestMatchers("/api/chart-of-accounts").hasAnyRole("ADMINISTRATOR", "MANAGER", "ACCOUNTANT")
                                .requestMatchers("/api/chart-of-accounts/{accountNumber}").hasAnyRole("ADMINISTRATOR", "MANAGER", "ACCOUNTANT")
                                .requestMatchers("/api/chart-of-accounts/add-account").hasRole("ADMINISTRATOR")
                                .requestMatchers("/api/chart-of-accounts/add-transaction").hasRole("ADMINISTRATOR")
                                .requestMatchers("/api/chart-of-accounts/add-journal-entry").hasAnyRole("ADMINISTRATOR", "MANAGER")
                                .requestMatchers("/api/chart-of-accounts/request-journal-entry").hasAnyRole("ACCOUNTANT", "ADMINISTRATOR", "MANAGER")
                                .requestMatchers("/dashboard/general-ledger").hasAnyRole("ACCOUNTANT", "ADMINISTRATOR", "MANAGER")
                                .requestMatchers("/api/chart-of-accounts/update-transaction").hasRole("ADMINISTRATOR")
                                .requestMatchers("/api/chart-of-accounts/update-account").hasRole("ADMINISTRATOR")
                                .requestMatchers("/api/chart-of-accounts/delete-transaction").hasRole("ADMINISTRATOR")
                                .requestMatchers("/api/chart-of-accounts/update-activation").hasRole("ADMINISTRATOR")
                                .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession())
                .logout(logout -> logout
                        .logoutUrl("/api/users/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            if (request.getSession(false) != null) {
                                request.getSession().invalidate();
                            }
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Logged out successfully\"}");
                            response.getWriter().flush();
                            response.getWriter().close();
                        })
                        .deleteCookies("JSESSIONID")
                        .permitAll());
        return http.build();
    }

    @Bean
    public ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Login successful\"}");
            response.setStatus(HttpServletResponse.SC_OK);
        };
    }

    @Bean
    public AuthenticationFailureHandler failureHandler() {
        return (request, response, exception) -> {
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Login failed\", \"error\":\"" + exception.getMessage() + "\"}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CookieCsrfTokenRepository csrfCookieTokenRepository() {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");
        return csrfTokenRepository;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setSameSite("None");
        serializer.setUseSecureCookie(true);
        serializer.setCookiePath("/");
        serializer.setDomainName("synergyaccounting.app");
        return serializer;
    }

}*/
