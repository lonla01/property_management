package com.rental.management.security;

import com.rental.management.repository.ProprietaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProprietaireUserDetailsService implements UserDetailsService {

    private final ProprietaireRepository proprietaireRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return proprietaireRepository.findByEmail(email)
                .map(ProprietaireUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Aucun propriétaire pour l'email: " + email));
    }
}
