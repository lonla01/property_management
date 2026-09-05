package com.rental.management.security;

import com.rental.management.domain.entity.Proprietaire;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class ProprietaireUserDetails implements UserDetails {

    private final Proprietaire proprietaire;

    public ProprietaireUserDetails(Proprietaire proprietaire) {
        this.proprietaire = proprietaire;
    }

    public Proprietaire getProprietaire() {
        return proprietaire;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_PROPRIETAIRE"));
    }

    @Override
    public String getPassword() {
        return proprietaire.getMotDePasseHash();
    }

    @Override
    public String getUsername() {
        return proprietaire.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return proprietaire.isActif(); }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return proprietaire.isActif(); }
}
