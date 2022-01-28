package io.simforce.bytezard.coordinator.utils;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.simforce.bytezard.common.entity.TokenInfo;
import io.simforce.bytezard.coordinator.CoordinatorConstants;

@Component
public class TokenManager {

    /**
     * 自定义 token 私钥
     */
    @Value("${jwtToken.secret:jksdhfyqjsdkhfakshf}")
    private String tokenSecret;

    /**
     * 默认 token 超时时间
     */
    @Value("${jwtToken.timeout:18000000000}")
    private Long timeout;

    /**
     * 默认 jwt 生成算法
     */
    @Value("${jwtToken.algorithm:HS512}")
    private String algorithm;
    
    private static final int PASSWORD_LEN = 8;

    private static final char[] PASSWORD_SEEDS = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    public static String randomPassword() {
        IntStream intStream = new Random().ints(0, PASSWORD_SEEDS.length);
        return intStream.limit(PASSWORD_LEN).mapToObj(i -> PASSWORD_SEEDS[i]).map(String::valueOf).collect(Collectors.joining());
    }

    public String generateToken(TokenInfo tokenInfo) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CoordinatorConstants.TOKEN_USER_NAME, StringUtils.isEmpty(tokenInfo.getUsername()) ? CoordinatorConstants.EMPTY : tokenInfo.getUsername());
        claims.put(CoordinatorConstants.TOKEN_USER_PASSWORD, StringUtils.isEmpty(tokenInfo.getPassword()) ? CoordinatorConstants.EMPTY : tokenInfo.getPassword());
        claims.put(CoordinatorConstants.TOKEN_CREATE_TIME, System.currentTimeMillis());
        return generate(claims);
    }

    public String refreshToken(String token) {
        Claims claims = getClaims(token);
        claims.put(CoordinatorConstants.TOKEN_CREATE_TIME, System.currentTimeMillis());
        return generate(claims);
    }

    public String generateToken(TokenInfo tokenInfo, Long timeOutMillis) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(CoordinatorConstants.TOKEN_USER_NAME, StringUtils.isEmpty(tokenInfo.getUsername()) ? CoordinatorConstants.EMPTY : tokenInfo.getUsername());
        claims.put(CoordinatorConstants.TOKEN_USER_PASSWORD, StringUtils.isEmpty(tokenInfo.getPassword()) ? CoordinatorConstants.EMPTY : tokenInfo.getPassword());
        claims.put(CoordinatorConstants.TOKEN_CREATE_TIME, System.currentTimeMillis());

        return toTokenString(timeOutMillis, claims);
    }

    public String generateContinuousToken(TokenInfo tokenInfo) {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put(CoordinatorConstants.TOKEN_USER_NAME, StringUtils.isEmpty(tokenInfo.getUsername()) ? CoordinatorConstants.EMPTY : tokenInfo.getUsername());
        claims.put(CoordinatorConstants.TOKEN_USER_PASSWORD, StringUtils.isEmpty(tokenInfo.getPassword()) ? CoordinatorConstants.EMPTY : tokenInfo.getPassword());
        claims.put(CoordinatorConstants.TOKEN_CREATE_TIME, System.currentTimeMillis());
        SignatureAlgorithm.valueOf(algorithm);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.get(CoordinatorConstants.TOKEN_USER_NAME).toString())
                .signWith(SignatureAlgorithm.valueOf(algorithm), tokenSecret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    private String generate(Map<String, Object> claims) {
        return toTokenString(timeout, claims);
    }

    private String toTokenString(Long timeOutMillis, Map<String, Object> claims) {
        long expiration = Long.parseLong(claims.get(CoordinatorConstants.TOKEN_CREATE_TIME) + CoordinatorConstants.EMPTY) + timeOutMillis;

        SignatureAlgorithm.valueOf(algorithm);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(claims.get(CoordinatorConstants.TOKEN_USER_NAME).toString())
                .setExpiration(new Date(expiration))
                .signWith(SignatureAlgorithm.valueOf(algorithm), tokenSecret.getBytes(StandardCharsets.UTF_8))
                .compact();
    }

    public String getUsername(String token) {
        String username = null;
        try {
            final Claims claims = getClaims(token);
            username = claims.get(CoordinatorConstants.TOKEN_USER_NAME).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return username;
    }

    public String getPassword(String token) {
        String password = null;
        try {
            final Claims claims = getClaims(token);
            password = claims.get(CoordinatorConstants.TOKEN_USER_PASSWORD).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return password;
    }

    private Claims getClaims(String token) {
       return Jwts.parser()
                    .setSigningKey(tokenSecret.getBytes(StandardCharsets.UTF_8))
                    .parseClaimsJws(token.startsWith(CoordinatorConstants.TOKEN_PREFIX) ?
                            token.substring(token.indexOf(CoordinatorConstants.TOKEN_PREFIX) + CoordinatorConstants.TOKEN_PREFIX.length()).trim() :
                            token.trim())
                    .getBody();
    }

    public boolean validateToken(String token, TokenInfo tokenInfo) {
        String username = getUsername(token);
        String password = getPassword(token);
        return (username.equals(tokenInfo.getUsername()) && password.equals(tokenInfo.getPassword()) && !(isExpired(token)));
    }


    public boolean validateToken(String token, String username, String password) {
        String tokenUsername = getUsername(token);
        String tokenPassword = getPassword(token);
        return (username.equals(tokenUsername) && password.equals(tokenPassword) && !(isExpired(token)));
    }

    private Date getCreatedDate(String token) {
        Date created = null;
        try {
            final Claims claims = getClaims(token);
            created = new Date((Long) claims.get(CoordinatorConstants.TOKEN_CREATE_TIME));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return created;
    }

    private Date getExpirationDate(String token) {
        Date expiration = null;
        try {
            final Claims claims = getClaims(token);
            expiration = claims.getExpiration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return expiration;
    }

    private Boolean isExpired(String token) {
        final Date expiration = getExpirationDate(token);
        //超时时间为空则永久有效
        return null != expiration && expiration.before(new Date(System.currentTimeMillis()));
    }

}
