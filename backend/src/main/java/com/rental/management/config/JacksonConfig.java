package com.rental.management.config;

import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Les entités JPA sont renvoyées directement par les contrôleurs (pas de DTO de
 * lecture) avec des associations LAZY. Ce module apprend à Jackson à
 * initialiser ces proxies Hibernate au moment de la sérialisation au lieu
 * d'échouer — nécessite open-in-view=true (voir application.yml) pour que la
 * session Hibernate soit encore active à ce moment-là.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Hibernate6Module hibernate6Module() {
        Hibernate6Module module = new Hibernate6Module();
        module.configure(Hibernate6Module.Feature.FORCE_LAZY_LOADING, true);
        return module;
    }
}
