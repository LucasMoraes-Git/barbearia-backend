package br.com.barbearia.back_end.security;

import jakarta.servlet.DispatcherType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${security.jwt.secret}")
            String chaveBase64
    ) {
        byte[] chaveDecodificada;

        try {
            chaveDecodificada =
                    Base64.getDecoder()
                            .decode(chaveBase64);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "JWT_SECRET precisa estar em Base64.",
                    exception
            );
        }

        if (chaveDecodificada.length < 32) {
            throw new IllegalStateException(
                    "JWT_SECRET deve possuir pelo menos 256 bits."
            );
        }

        return new SecretKeySpec(
                chaveDecodificada,
                "HmacSHA256"
        );
    }

    @Bean
    public JwtEncoder jwtEncoder(
            SecretKey secretKey
    ) {
        return NimbusJwtEncoder
                .withSecretKey(secretKey)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey,
            @Value("${security.jwt.issuer}")
            String emissor
    ) {
        NimbusJwtDecoder decoder =
                NimbusJwtDecoder
                        .withSecretKey(secretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer(
                        emissor
                )
        );

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter()
    {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("perfil");

        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

        return authenticationConverter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter authenticationConverter
    ) throws Exception {

        return http

                // A autenticação é feita por JWT no cabeçalho Authorization,
                // e não por sessão ou cookie.
                .csrf(AbstractHttpConfigurer::disable)

                // Desativa as formas tradicionais de autenticação do Spring.
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)

                // Não existe sessão de logout no JWT.
                // O frontend simplesmente remove o token.
                .logout(AbstractHttpConfigurer::disable)

                // O Spring não armazenará a autenticação em uma sessão.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Permite que o Spring Boot produza corretamente
                        // respostas de erro e encaminhamentos internos.
                        .dispatcherTypeMatchers(
                                DispatcherType.ERROR,
                                DispatcherType.FORWARD
                        ).permitAll()

                        // Rotas públicas de autenticação.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/auth/login",
                                "/usuarios/cadastro"
                        ).permitAll()

                        // Catálogo público.
                        // Somente GET será público.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/servicos/**"
                        ).permitAll()

                        // Tudo que começa com /admin exige ROLE_ADMIN.
                        .requestMatchers(
                                "/admin/**"
                        ).hasRole("ADMIN")

                        // Alterações do próprio usuário exigem um JWT válido.
                        .requestMatchers(
                                "/usuarios/me/**"
                        ).authenticated()

                        // Por enquanto, seus endpoints de agendamento
                        // listam todos os agendamentos.
                        .requestMatchers(
                                "/agendamentos/**"
                        ).authenticated()

                        // Bloqueia qualquer endpoint que não tenha
                        // uma regra declarada acima.
                        .anyRequest().denyAll()
                )

                // Configura a API como Resource Server.
                // O Spring procurará o JWT no cabeçalho Bearer.
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(jwt ->
                                jwt.jwtAuthenticationConverter(
                                        authenticationConverter
                                )
                        )
                )

                .build();
    }
}