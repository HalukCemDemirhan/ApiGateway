package rs.apigateway.ApiGateway.filter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import rs.apigateway.ApiGateway.util.JwtUtil;

@Component
public class JwtAuthenticationFilterFactory  extends AbstractGatewayFilterFactory<JwtAuthenticationFilterFactory.Config> {
    @Autowired
    private JwtUtil jwtUtil;

    @Value("${auth.signin.url}")
    private String AUTH_SERVICE_LOGIN_URL;
    public JwtAuthenticationFilterFactory() {
        super(Config.class);
    }

    @Override
    public JwtAuthenticationFilter apply(Config config) {
        return new JwtAuthenticationFilter(jwtUtil, AUTH_SERVICE_LOGIN_URL);
    }

    public static class Config {
    }
}