package rs.apigateway.ApiGateway.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.apigateway.ApiGateway.UserRoles;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String accessToken;
    private String tokenType;

    private UserRoles roleAdmin;


    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public UserRoles getRoleAdmin() {
        return roleAdmin;
    }

    public void setRoleAdmin(UserRoles roleAdmin) {
        this.roleAdmin = roleAdmin;
    }
}
