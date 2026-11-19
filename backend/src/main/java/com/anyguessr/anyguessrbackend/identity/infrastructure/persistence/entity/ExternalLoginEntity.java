package com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "external_login")
public class ExternalLoginEntity {
    
    @EmbeddedId
    private ExternalLoginId id;

    @Column(name = "user_Id", nullable = false)
    private Long userId;

    protected ExternalLoginEntity(){

    }

    public ExternalLoginEntity(
        ExternalLoginId id,
        Long userId
    ){
        this.id = id;
        this.userId = userId;
    }

    public ExternalLoginId getId(){
        return id;
    }

    public Long getUserId(){
        return userId;
    }
}
