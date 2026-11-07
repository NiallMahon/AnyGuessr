package com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.repository;

import com.anyguessr.anyguessrbackend.identity.domain.User;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository;
import com.anyguessr.anyguessrbackend.identity.domain.UserRepository.UsernameOrEmailTakenException;
import com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity.UserEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@org.springframework.context.annotation.Profile("!memory")
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository repository;

    public JpaUserRepository(
            SpringDataUserRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return repository.findById(id).map(UserEntity::toDomain);
    }
   
    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }

        return repository.findByEmailIgnoreCase(email.trim())
                .map(UserEntity::toDomain);
    }

    @Override
    public Optional<User> findByUsername(
            String username
    ) {
        if (username == null) {
            return Optional.empty();
        }

        return repository
                .findByUsernameIgnoreCase(username.trim())
                .map(UserEntity::toDomain);
    }


    @Override
    public User save(User user) {
        UserEntity entity;

        if (user.id() == null) {
            entity = UserEntity.fromDomain(user);
        } else {
            entity = repository.findById(user.id())
                    .orElseThrow(() ->
                            new IllegalArgumentException("User does not exist")
                    );

            entity.setUsername(user.username());
            entity.setEmail(user.email());
            entity.setPasswordHash(user.passwordHash());
            entity.setLocation(user.location());
        }

        

        try {
            return repository.save(entity).toDomain();
        } catch (DataIntegrityViolationException e) {
            throw new UsernameOrEmailTakenException(e);
        }
    }

}
