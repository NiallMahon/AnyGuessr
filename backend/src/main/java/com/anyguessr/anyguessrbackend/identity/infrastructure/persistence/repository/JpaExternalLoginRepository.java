package com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.repository;

import com.anyguessr.anyguessrbackend.identity.domain.ExternalLogin;
import com.anyguessr.anyguessrbackend.identity.domain.ExternalLoginRepository;
import com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity.ExternalLoginEntity;
import com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity.ExternalLoginId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository 
@org.springframework.context.annotation.Profile("database")

public class JpaExternalLoginRepository implements ExternalLoginRepository{
    private final SpringDataExternalLoginRepository springDataExternalLoginRepository;

    public JpaExternalLoginRepository(
        SpringDataExternalLoginRepository springDataExternalLoginRepository
    ){
        this.springDataExternalLoginRepository = springDataExternalLoginRepository;
    }

    @Override
    public Optional<ExternalLogin> find(
        String provider,
        String providerUserId
    ){
        ExternalLoginId id = new ExternalLoginId(provider, providerUserId);
        // new ExternalLoginId(provider, providerUserId);

        return springDataExternalLoginRepository.findById(id).map(entity -> new ExternalLogin(
            entity.getId().getProvider(),
            entity.getId().getProviderUserId(),
            entity.getUserId()
        ));
    }
    
    @Override
    public ExternalLogin save(
            ExternalLogin externalLogin
    ) {
        ExternalLoginId id =
                new ExternalLoginId(
                        externalLogin.provider(),
                        externalLogin.providerUserId()
                );

         ExternalLoginEntity entity =
                new ExternalLoginEntity(
                        id,
                        externalLogin.userId()
                );

        ExternalLoginEntity saved =
                springDataExternalLoginRepository.save(entity);

        return new ExternalLogin(
                saved.getId().getProvider(),
                saved.getId().getProviderUserId(),
                saved.getUserId()
        );
    }
        
}
