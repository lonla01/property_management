package com.rental.management.service;

import com.rental.management.domain.entity.Proprietaire;
import com.rental.management.dto.request.LoginRequest;
import com.rental.management.dto.request.RegisterRequest;
import com.rental.management.dto.response.AuthResponse;
import com.rental.management.exception.EmailDejaUtiliseException;
import com.rental.management.repository.ProprietaireRepository;
import com.rental.management.security.JwtService;
import com.rental.management.security.ProprietaireUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final ProprietaireRepository proprietaireRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest request) {
        if (proprietaireRepository.existsByEmail(request.getEmail())) {
            throw new EmailDejaUtiliseException("Un compte existe déjà avec cet email");
        }
        Proprietaire proprietaire = Proprietaire.builder()
                .nom(request.getNom())
                .email(request.getEmail())
                .telephone(request.getTelephone())
                .motDePasseHash(passwordEncoder.encode(request.getMotDePasse()))
                .build();
        proprietaireRepository.save(proprietaire);

        String token = jwtService.generateToken(new ProprietaireUserDetails(proprietaire));
        return new AuthResponse(token, proprietaire.getNom(), proprietaire.getEmail());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getMotDePasse()));

        Proprietaire proprietaire = proprietaireRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalStateException("Incohérence: authentification réussie mais propriétaire introuvable"));

        String token = jwtService.generateToken(new ProprietaireUserDetails(proprietaire));
        return new AuthResponse(token, proprietaire.getNom(), proprietaire.getEmail());
    }
}
