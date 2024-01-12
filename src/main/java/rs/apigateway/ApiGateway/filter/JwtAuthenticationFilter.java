package rs.apigateway.ApiGateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import rs.apigateway.ApiGateway.UserRoles;
import rs.apigateway.ApiGateway.model.User;
import rs.apigateway.ApiGateway.model.UserResponse;
import rs.apigateway.ApiGateway.util.JwtUtil;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;


public class JwtAuthenticationFilter implements GatewayFilter {
    ObjectMapper objectMapper;
    final String AUTH_SERVICE_URL;
    public JwtUtil jwtUtil;
    private Map<String, String> roleEndpointMap;


    @Autowired
    public JwtAuthenticationFilter(JwtUtil jwtUtil, @Value("$auth.signin.url") String AUTH_SERVICE_URL) {
        this.AUTH_SERVICE_URL = AUTH_SERVICE_URL;
        this.jwtUtil = jwtUtil;
        roleEndpointMap = Map.of(
                "/api/cars/POST", "ROLE_ADMIN",
                "/api/cars/PUT", "ROLE_ADMIN",
                "/api/cars/DELETE", "ROLE_ADMIN"
        );
    }


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        final List<String> openApiEndpoints = List.of("/auth/signin", "/auth/signup");
        Predicate<ServerHttpRequest> isSecured = r -> openApiEndpoints.stream()
                .noneMatch(uri -> request.getURI().getPath().contains(uri));
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        if (!jwtUtil.validateToken(token)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }


        User userDetails = jwtUtil.extractUserDetails(token);
        try {
            int index = userDetails.getUsernameOrEmail().indexOf("@");
            String username = userDetails.getUsernameOrEmail().substring(0,index);
            String path = exchange.getRequest().getPath().toString();
            String method = exchange.getRequest().getMethod().name();


            int count = 0;
            for (int i = 0; i < path.length(); i++) {
                if (path.charAt(i) == '/') {
                    count++;
                }
            }
            URI fullUri = exchange.getRequest().getURI();
            String queryString = fullUri.getQuery();
            if ((count > 2) || (request.getMethod().matches("GET") &&  path.contains("reservation"))) {
                int lastSlashIndex = path.lastIndexOf('/');
                String rest = path.substring(lastSlashIndex + 1);
                path = path.substring(0, lastSlashIndex);
                if (!username.equals(rest) && (userDetails.getUserRole().equals("ROLE_USER")) && (!queryString.contains("startDate"))) {
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

            }
            String updatedEndPoint = path + "/" + method;
            if (roleEndpointMap.containsKey(updatedEndPoint)) {
                String requiredRoles = roleEndpointMap.get(updatedEndPoint);
                if (!userDetails.getUserRole().equals(requiredRoles)) {
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                } else {
                    return chain.filter(exchange);
                }
            } else {
                return chain.filter(exchange);
            }
        } catch (HttpClientErrorException e) {
            System.out.println("HttpClientErrorException: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            // Or use a logger if available
            token = "";
        } catch (Exception e) {
            e.printStackTrace(); // This will print the stack trace for other exceptions
            token = "";
        }
        return chain.filter(exchange);
    }
}
















        /*
        ServerHttpRequest request = exchange.getRequest();
        final List<String> openApiEndpoints = List.of("/auth/signin", "/auth/signup");
        Predicate<ServerHttpRequest> isSecured = r -> openApiEndpoints.stream()
                        .noneMatch(uri -> request.getURI().getPath().contains(uri));
        String username;
        String password;
        String token = "";
        if (!request.getHeaders().getOrEmpty("username").isEmpty()
                && !request.getHeaders().getOrEmpty("password").isEmpty()) {
            username = request.getHeaders().getOrEmpty("username").get(0);
            password = request.getHeaders().getOrEmpty("password").get(0);
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            User user = new User(username, password);
            String userConvertedJson;
            try {
                userConvertedJson = new ObjectMapper().writeValueAsString(user);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> requirements = new HttpEntity<>(userConvertedJson, headers);
            try {
                String responseString = restTemplate.postForObject(AUTH_SERVICE_URL, requirements, String.class);
                Gson gson = new GsonBuilder().create();
                final UserResponse userResponse = gson.fromJson(responseString, UserResponse.class);
                token = userResponse.getAccessToken();
                UserRoles userRoles = userResponse.getRoleAdmin();
                String path = exchange.getRequest().getPath().toString();
                String method = exchange.getRequest().getMethod().name();

                int count = 0;
                for (int i = 0; i < path.length(); i++) {
                    if (path.charAt(i) == '/') {
                        count++;
                    }
                }
                if (count > 2 || (request.getMethod().matches("GET") &&  path.contains("reservation"))) {
                    int lastSlashIndex = path.lastIndexOf('/');
                    String rest = path.substring(lastSlashIndex + 1);
                    path = path.substring(0, lastSlashIndex);
                    if (!username.equals(rest) && !(userRoles.equals(UserRoles.ROLE_ADMIN))) {
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    }

                }
                String endpointKey = path + "/" + method;
                if (Objects.equals(token, "") && !jwtUtil.validateToken(token)) {
                    ServerHttpResponse nullResponse = (ServerHttpResponse) exchange.getResponse();
                    nullResponse.setStatusCode(HttpStatus.UNAUTHORIZED);
                }
                if (roleEndpointMap.containsKey(endpointKey)) {
                    String requiredRoles = roleEndpointMap.get(endpointKey);
                    if (!userRoles.name().equals(requiredRoles)) {
                        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                        return exchange.getResponse().setComplete();
                    } else {
                        return chain.filter(exchange);
                    }
                } else {
                    return chain.filter(exchange);
                }
            } catch (HttpClientErrorException e) {
                System.out.println("HttpClientErrorException: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
                // Or use a logger if available
                token = "";
            } catch (Exception e) {
                e.printStackTrace(); // This will print the stack trace for other exceptions
                token = "";
            }


        }
        return chain.filter(exchange);

         */
