package br.com.clinicah.security;

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

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // público
                .antMatchers("/auth/**").permitAll()
                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v2/api-docs").permitAll()

                // médicos: ROOT e MEDICO gerenciam; PACIENTE só lê
                .antMatchers(HttpMethod.GET, "/doctors/**").hasAnyRole("ROOT", "MEDICO", "PACIENTE")
                .antMatchers("/doctors/**").hasAnyRole("ROOT", "MEDICO")

                // prontuários: MEDICO e ROOT criam/editam; PACIENTE só lê o próprio
                .antMatchers(HttpMethod.GET, "/patients/*/records/**").hasAnyRole("ROOT", "MEDICO", "PACIENTE")
                .antMatchers("/patients/*/records/**").hasAnyRole("ROOT", "MEDICO")

                // pacientes: ROOT e PACIENTE gerenciam; MEDICO só lê
                .antMatchers(HttpMethod.GET, "/patients/**").hasAnyRole("ROOT", "MEDICO", "PACIENTE")
                .antMatchers("/patients/**").hasAnyRole("ROOT", "PACIENTE")

                // consultas: todos os roles autenticados
                .antMatchers("/appointments/**").hasAnyRole("ROOT", "MEDICO", "PACIENTE")

                .anyRequest().hasRole("ROOT")
            .and()
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
