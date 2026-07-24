package tfs.com.ecommerce.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tfs.com.ecommerce.api.api.security.SecurityFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(req -> {
                    // Rotas Públicas
                    req.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll();
                    req.requestMatchers(HttpMethod.GET, "/api/v1/produtos").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/api/v1/pedidos/checkout").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/api/v1/fretes/calcular").permitAll();
                    req.requestMatchers(HttpMethod.POST, "/api/v1/webhooks/mercadopago").permitAll();
                    req.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll(); // CORS Preflight

                    // Swagger OpenAPI
                    req.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll();

                    // Rotas Privadas (Admin)
                    req.requestMatchers(HttpMethod.POST, "/api/v1/produtos").hasRole("ADMIN");
                    req.requestMatchers(HttpMethod.PUT, "/api/v1/produtos/**").hasRole("ADMIN");
                    req.requestMatchers(HttpMethod.DELETE, "/api/v1/produtos/**").hasRole("ADMIN");

                    req.anyRequest().authenticated();
                })
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
