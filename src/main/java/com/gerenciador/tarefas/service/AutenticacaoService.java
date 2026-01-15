package com.gerenciador.tarefas.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

public class AutenticacaoService {

    private static final String BEARE = "Bearer ";
    private static final String HEADER_AUTHORIZATION = "Authorization";
    private static final String CHAVE_JWT_KEY = "signinKey";
    private static final String AUTHORITIES = "authorities";
    private static final int EXPITATION_TOKEN_ONE_HOUR = 3600000;



    static public void addJWTToken(HttpServletResponse response, Authentication authentication) {

        Map<String, Object> claims = new HashMap<>();

        claims.put(AUTHORITIES, authentication
                .getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));

        String jwtToken = Jwts.builder()
                .setSubject(authentication.getName())
                .setExpiration(new Date(System.currentTimeMillis() + EXPITATION_TOKEN_ONE_HOUR))
                .signWith(SignatureAlgorithm.HS512, CHAVE_JWT_KEY)
                .addClaims(claims)
                .compact();

        response.addHeader(HEADER_AUTHORIZATION, BEARE + jwtToken);
        response.addHeader("Acess-Control-Expose-Headers", HEADER_AUTHORIZATION);
    }

    static public Authentication obterAutenticacao(HttpServletRequest request){

        String token = request.getHeader(HEADER_AUTHORIZATION);

        if (token != null) {

            Claims user = Jwts.parser()
                    .setSigningKey(CHAVE_JWT_KEY)
                    .parseClaimsJws(token.replace(BEARE + " ",""))
                    .getBody();

            if (user != null){

                List<SimpleGrantedAuthority> permissoes = ((ArrayList<String>)user.get(AUTHORITIES))
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

                return new UsernamePasswordAuthenticationToken(user, null,permissoes);
            } else {
                throw new RuntimeException("Autenticação falhou");
            }
        }
        return null;
    }
}
