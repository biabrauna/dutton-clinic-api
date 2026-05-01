package br.com.clinicah.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    // Vercel — todos os deploys do projeto (preview + produção)
                    "https://dutton-clinic-interface2-gilt.vercel.app",
                    "https://dutton-clinic-interface.vercel.app",
                    "https://*.vercel.app",
                    // Lovable
                    "https://dutton-clinic-ui.lovable.app",
                    // Local dev
                    "http://localhost:5173",
                    "http://localhost:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
