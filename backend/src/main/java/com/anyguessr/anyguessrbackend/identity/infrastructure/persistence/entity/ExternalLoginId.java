package com.anyguessr.anyguessrbackend.identity.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable 
public class ExternalLoginId implements Serializable{
    @Column(nullable = false, length = 20)
    private String provider;

    @Column(
        name = "provider_user_id",
        nullable = false,
        length = 255
    )
    private String providerUserId;

    protected ExternalLoginId(){

    }

    public ExternalLoginId(
        String provider,
        String providerUserId
    ){
        this.provider = provider;
        this.providerUserId = providerUserId;
    }

    public String getProvider(){
        return provider;
    }

    public String getProviderUserId(){
        return providerUserId;
    }

    @Override
    public boolean equals(Object object){
        if (this == object) {
            return true;
        }

        if (!(object instanceof ExternalLoginId other)){
            return false;
        }

        return Objects.equals(provider, other.provider)
                && Objects.equals(
                        providerUserId,
                        other.providerUserId
                );
    }

    @Override
    public int hashCode() {
        return Objects.hash(provider, providerUserId);
    }
}
