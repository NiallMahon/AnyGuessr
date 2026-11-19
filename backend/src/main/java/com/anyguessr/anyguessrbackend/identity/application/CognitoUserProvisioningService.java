package com.anyguessr.anyguessrbackend.identity.application;

import com.anyguessr.anyguessrbackend.identity.domain.ExternalLogin;
import com.anyguessr.anyguessrbackend.identity.domain.ExternalLoginRepository;
import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CognitoUserProvisioningService {

    private final ExternalLoginRepository externalLoginRepository;
    private final UserRepository userRepository;
    

    public CognitoUserProvisioningService(
            ExternalLoginRepository externalLoginRepository,
            UserRepository userRepository
          
    ) {
        this.externalLoginRepository = externalLoginRepository;
        this.userRepository = userRepository;
        
    }

    @Transactional
    public Long findOrCreateUser(OidcUser cognitoUser) {
        String provider = "cognito";
        String providerUserId = cognitoUser.getSubject();

        return externalLoginRepository
                .find(provider, providerUserId)
                .map(ExternalLogin::userId)
                .orElseGet(() -> createOrLinkUser(cognitoUser));
    }

    private Long createOrLinkUser(OidcUser cognitoUser) {
        String email = cognitoUser.getEmail();

        String username = cognitoUser.getClaimAsString("cognito:username");

        User user = userRepository.findByEmail(email).orElseGet(() -> userRepository.save(
            new User(
                    null,
                    username,
                    email,
                    null,
                    ""
            )
            
        ));
     

        externalLoginRepository.save(
                new ExternalLogin(
                        "cognito",
                        cognitoUser.getSubject(),
                        user.id()
                )
        );

        return user.id();
    }

}
