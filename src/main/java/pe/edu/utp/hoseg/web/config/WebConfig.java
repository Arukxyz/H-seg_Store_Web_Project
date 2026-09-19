package pe.edu.utp.hoseg.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.FixedLocaleResolver;

import java.util.Locale;

/** Ajustes MVC generales. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /** El portal es solo en español de Perú; fija el locale para fechas y números en las vistas. */
    @Bean
    LocaleResolver localeResolver() {
        return new FixedLocaleResolver(Locale.forLanguageTag("es-PE"));
    }
}
