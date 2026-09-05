package com.rental.management.controller;

import com.rental.management.domain.entity.Unite;
import com.rental.management.dto.request.UniteRequest;
import com.rental.management.service.UniteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unites")
@RequiredArgsConstructor
public class UniteController {

    private final UniteService uniteService;

    // Note: liste non paginée volontairement — le nombre d'unités par bien reste borné
    // (un immeuble n'a jamais des milliers d'appartements), contrairement aux collections
    // globales (locataires, paiements, historique) qui doivent, elles, être paginées.
    @GetMapping
    public List<Unite> listerParBien(@RequestParam String bienId) {
        return uniteService.listerParBien(bienId);
    }

    @PostMapping
    public Unite creer(@Valid @RequestBody UniteRequest request) {
        return uniteService.creer(request);
    }

    @DeleteMapping("/{id}")
    public void archiver(@PathVariable String id) {
        uniteService.archiver(id);
    }
}
