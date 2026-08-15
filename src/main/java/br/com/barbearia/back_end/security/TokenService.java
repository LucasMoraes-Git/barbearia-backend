package br.com.barbearia.back_end.security;

import br.com.barbearia.back_end.usuario.entity.Usuario;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Getter
@Service

public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final String emissor;
    private final long expiracaoSegundos;

    public TokenService(
            JwtEncoder jwtEncoder,

            @Value("${security.jwt.issuer}")
            String emissor,

            @Value("${security.jwt.expiration-seconds}")
            long expiracaoSegundos
    )
    {
        this.jwtEncoder = jwtEncoder;
        this.emissor = emissor;
        this.expiracaoSegundos = expiracaoSegundos;
    }

    public String gerarToken(Usuario usuario)
    {
        Instant agora = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder().issuer(emissor).issuedAt(agora).expiresAt(agora.plusSeconds(expiracaoSegundos)).subject(usuario.getEmail()).claim("usuarioId", usuario.getId().toString()).claim("perfil", usuario.getUsuarioPerfil().toString()).build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        JwtEncoderParameters parametros = JwtEncoderParameters.from(header, claims);

        return jwtEncoder.encode(parametros).getTokenValue();
    }

}
