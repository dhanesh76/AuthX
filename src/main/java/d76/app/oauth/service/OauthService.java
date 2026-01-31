package d76.app.oauth.service;

import d76.app.auth.dto.RegisterResponse;
import d76.app.auth.model.IdentityProvider;
import d76.app.oauth.dto.SocialRegisterRequest;
import d76.app.security.jwt.JwtService;
import d76.app.security.jwt.model.JwtPurpose;
import d76.app.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OauthService {

    private final UserService userService;
    private final JwtService jwtService;

    public RegisterResponse socialRegister(SocialRegisterRequest request) {

        //validate the token
        jwtService.assertActionTokenValid(request.actionToken(), JwtPurpose.SOCIAL_REGISTER);

        //extract details
        var claims = jwtService.extractClaims(request.actionToken());
        String email = claims.getSubject();
        var provider = IdentityProvider.fromClient(claims.get("identityProvider", String.class));

        var user = userService.createOAuthUser(email, request.userName(), provider);

        return new RegisterResponse(
                user.getEmail(),
                provider.name(),
                user.getCreatedAt()
        );
    }
}
