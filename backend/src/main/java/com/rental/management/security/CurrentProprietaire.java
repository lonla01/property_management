package com.rental.management.security;

import com.rental.management.domain.entity.Proprietaire;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Petit utilitaire pour récupérer le propriétaire actuellement authentifié
 * à partir du contexte de sécurité (rempli par JwtAuthenticationFilter).
 * Utilisé par tous les services pour garantir l'isolation stricte des
 * données par compte (un propriétaire ne voit jamais les biens d'un autre).
 */
@Component
public class CurrentProprietaire {

    public Proprietaire get() {
        ProprietaireUserDetails details =
                (ProprietaireUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return details.getProprietaire();
    }

    public String id() {
        return get().getId();
    }
}
