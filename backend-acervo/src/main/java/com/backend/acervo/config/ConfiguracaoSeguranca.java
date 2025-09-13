package com.backend.acervo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.http.HttpMethod;


import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

@Configuration
@EnableWebMvc
public class ConfiguracaoSeguranca {

    @Bean
    SecurityFilterChain filtroSeguranca(HttpSecurity http) throws Exception {
        http
                .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.disable())
            )
                /*.headers(headers -> headers
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; " +
                            "connect-src 'self' http://localhost:4200 http://localhost:8080; " +
                            "script-src 'self' 'unsafe-inline'; " +
                            "style-src 'self' 'unsafe-inline'; " +
                            "img-src 'self' data:;")
                    )
                    .frameOptions(frameOptions -> frameOptions.deny())
                    .xssProtection(xss -> xss
                        .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK)
                    )
                )*/
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/api/autenticacao/**").permitAll()
                                .requestMatchers("/api/arquivos/download/**").permitAll()
                                .requestMatchers("/api/arquivos/enviar").hasRole("COLABORADOR")
                                .requestMatchers("/api/arquivos/excluir/**").hasRole("COLABORADOR")
                                .anyRequest().authenticated()
                )
                
                .logout(logout -> logout
                                .logoutUrl("/api/autenticacao/logout")
                                .logoutSuccessHandler((request, response, authentication) -> {
                                    response.setContentType("application/json");
                                    response.setCharacterEncoding("UTF-8");
                                    response.setStatus(200);
                                    response.getWriter().write("{\"mensagem\": \"Logout realizado com sucesso\"}");
                                })
                                .permitAll()
                )
                .sessionManagement(sessao -> sessao
                                .maximumSessions(1)
                                .expiredSessionStrategy(event -> {
                                    HttpServletResponse response = event.getResponse();
                                    response.setContentType("application/json");
                                    response.setCharacterEncoding("UTF-8");
                                    response.setStatus(401);
                                    response.getWriter().write("{\"erro\": \"Sessão expirada\"}");
                                })
                );
        
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:4200",
            "http://127.0.0.1:4200", 
            "http://[::1]:4200"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Set-Cookie", "Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    @Bean
    PasswordEncoder codificadorSenha() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}