package com.rental.management.controller;

import com.rental.management.domain.entity.Bien;
import com.rental.management.dto.request.BienRequest;
import com.rental.management.service.BienService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/biens")
@RequiredArgsConstructor
public class BienController {

    private final BienService bienService;

    @GetMapping
    public Page<Bien> lister(@PageableDefault(size = 50) Pageable pageable) {
        return bienService.lister(pageable);
    }

    @PostMapping
    public Bien creer(@Valid @RequestBody BienRequest request) {
        return bienService.creer(request);
    }

    @GetMapping("/{id}")
    public Bien obtenir(@PathVariable String id) {
        return bienService.obtenir(id);
    }

    @PutMapping("/{id}")
    public Bien modifier(@PathVariable String id, @Valid @RequestBody BienRequest request) {
        return bienService.modifier(id, request);
    }

    @DeleteMapping("/{id}")
    public void archiver(@PathVariable String id) {
        bienService.archiver(id);
    }
}
