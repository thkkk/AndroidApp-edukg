package backend.service;

import backend.config.Config;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecretService {
    private Config config;
    private String idKey = "username";

    @Autowired
    public SecretService(Config config) {
        this.config = config;
    }

    public String string2JWTtoken(String username) { //将openID转为JWTtoken
        Algorithm algorithm = Algorithm.HMAC256(config.getJwtSecret());
        Map<String, Object> header = new HashMap<>(2);
        header.put("Type", "Jwt");
        header.put("alg", "HS256");

        return JWT.create()
                .withHeader(header)
                .withClaim(idKey, username)
                .withExpiresAt(new Date(System.currentTimeMillis() + 10 * 86400 * 1000))
                .sign(algorithm);
    }

    public String string2JWTtoken(String username, long time) { //将openID转为JWTtoken
        Algorithm algorithm = Algorithm.HMAC256(config.getJwtSecret());
        Map<String, Object> header = new HashMap<>(2);
        header.put("Type", "Jwt");
        header.put("alg", "HS256");

        return JWT.create()
                .withHeader(header)
                .withClaim(idKey, username)
                .withExpiresAt(new Date(System.currentTimeMillis() + time * 1000))
                .sign(algorithm);
    }

    public String jwtToken2String(String jwtToken) { //将JWTtoken转为openID
        Algorithm algorithm = Algorithm.HMAC256(config.getJwtSecret());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(jwtToken);
        return jwt.getClaim(idKey).asString();
    }
}
