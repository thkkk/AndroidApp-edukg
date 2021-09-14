package backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "config")
public class Config {
    private String defaultPhoneString;
    private String defaultPassword;
    private String jwtSecret;
}
