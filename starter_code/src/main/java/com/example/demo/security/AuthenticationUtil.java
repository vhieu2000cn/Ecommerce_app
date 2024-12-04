package com.example.demo.security;

import com.auth0.jwt.JWT;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

@Component
public class AuthenticationUtil {

    public static final String SECRET_KEY = "SECRET_KEY";
    public static final long EXPIRATION_TIME = 864_00_000 * 3;
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String HEADER_STRING = "Authorization";
    public static final String SIGN_IN_URL = "/login";
    public static final String SIGN_UP_URL = "/api/user/create";

    public static String generateToken(String username) {
        try {
            return JWT.create()
                    .withSubject(username)
                    .withExpiresAt(new Date(System.currentTimeMillis() + AuthenticationUtil.EXPIRATION_TIME))
                    .sign(HMAC512(AuthenticationUtil.SECRET_KEY.getBytes()));
        }catch (RuntimeException  e){
            return null;
        }
    }


    public static String getUserFromToken(String token) {
        try {
            return JWT.require(HMAC512(AuthenticationUtil.SECRET_KEY.getBytes())).build()
                    .verify(token.replace(AuthenticationUtil.TOKEN_PREFIX, ""))
                    .getSubject();
        }catch (RuntimeException  e){
            return null;
        }
    }
}