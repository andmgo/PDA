package gescazone.demo.infrastructure.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    // Se inyecta desde application.properties
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    // ─────────────────────────────────────────
    // Genera la clave de firma a partir del secret
    // ─────────────────────────────────────────
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // ─────────────────────────────────────────
    // Genera el token JWT después del login exitoso
    // Recibe el Authentication de Spring Security
    // ─────────────────────────────────────────
    public String generateToken(Authentication authentication) {
        // El "principal" es el usuario autenticado (correo en nuestro caso)
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + jwtExpiration);

        return Jwts.builder()
            .setSubject(userDetails.getUsername()) 
            .claim("roles", userDetails.getAuthorities())
            .setIssuedAt(ahora)
            .setExpiration(expiracion)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    // ─────────────────────────────────────────
    // Extrae el número de documento del token
    // Lo usa JwtAuthenticationFilter para saber quién es el usuario
    // ─────────────────────────────────────────
    public String getNumeroDocumentoFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject(); // retorna el número de documento que pusimos en setSubject()
    }

    // ─────────────────────────────────────────
    // Valida que el token sea legítimo y no esté vencido
    // Lo usa JwtAuthenticationFilter en cada request
    // ─────────────────────────────────────────
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token); // si falla, lanza excepción
            return true;

        } catch (ExpiredJwtException e) {
            System.out.println("Token vencido: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.out.println("Token no soportado: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.out.println("Token malformado: " + e.getMessage());
        } catch (SecurityException e) {
            System.out.println("Firma inválida: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Token vacío o nulo: " + e.getMessage());
        }
        return false;
    }
}