package epreuve.kfokam48.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Le front (Next.js, port 3000) appelle le backend (port 8080) depuis le navigateur :
 * l'autorisation d'origine doit être explicite, y compris en conteneurs.
 */
@Configuration
public class WebConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "http://localhost:3000",
                                "http://frontend:3000",
                                System.getenv().getOrDefault("FRONTEND_ORIGIN", "http://localhost:3000"))
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
