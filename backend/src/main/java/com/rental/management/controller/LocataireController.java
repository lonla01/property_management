package com.rental.management.controller;

import com.rental.management.domain.entity.Locataire;
import com.rental.management.dto.request.LocataireRequest;
import com.rental.management.service.LocataireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/locataires")
@RequiredArgsConstructor
public class LocataireController {

    private final LocataireService locataireService;

    @GetMapping
    public Page<Locataire> lister(@PageableDefault(size = 50) Pageable pageable) {
        return locataireService.lister(pageable);
    }

    @PostMapping
    public Locataire creer(@Valid @RequestBody LocataireRequest request) {
        return locataireService.creer(request);
    }

    @GetMapping("/{id}")
    public Locataire obtenir(@PathVariable String id) {
        return locataireService.obtenir(id);
    }

    @PutMapping("/{id}")
    public Locataire modifier(@PathVariable String id, @Valid @RequestBody LocataireRequest request) {
        return locataireService.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    public void archiver(@PathVariable String id) {
        locataireService.archiver(id);
    }
}
