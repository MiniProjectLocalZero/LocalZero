package se.mau.localzero.auth.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import se.mau.localzero.auth.handler.CustomLoginSuccessHandler;
import se.mau.localzero.auth.handler.CustomLogoutSuccessHandler;

/**
 * Configuration class
 * Decides how login works and uses encryption algorithms for passwords
 */

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain for handling HTTP security concerns such as authentication,
     * authorization, login, and logout procedures in a Spring Security context.
     *
     * @param http the {@code HttpSecurity} instance used to configure security settings for HTTP requests,
     *             including authorization rules and login/logout behavior.
     * @return the configured {@code SecurityFilterChain}, defining how security is applied to HTTP requests.
     * @throws Exception if an error occurs while configuring the {@code SecurityFilterChain}.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomLoginSuccessHandler customLoginSuccessHandler, CustomLogoutSuccessHandler customLogoutSuccessHandler) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                    //Decide which pages everyone can see
                .requestMatchers("/auth/login", "/auth/register", "/styles.css").permitAll()
                    //Every other page requires the user to be logged in
                    .anyRequest().authenticated()
            )
            .formLogin((form) -> form
                .loginPage("/auth/login") //The page we want to show
                    .loginProcessingUrl("/auth/login") //The page where the POST gets sent
                    .successHandler(customLoginSuccessHandler)
                .permitAll()
            )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(customLogoutSuccessHandler)
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
