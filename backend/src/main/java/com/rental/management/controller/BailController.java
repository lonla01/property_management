package com.rental.management.controller;

import com.rental.management.domain.entity.Bail;
import com.rental.management.dto.request.BailRequest;
import com.rental.management.service.BailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/baux")
@RequiredArgsConstructor
public class BailController {

    private final BailService bailService;

    @GetMapping
    public Page<Bail> lister(@PageableDefault(size = 50) Pageable pageable) {
        return bailService.lister(pageable);
    }

    @PostMapping
    public Bail creer(@Valid @RequestBody BailRequest request) {
        return bailService.creer(request);
    }

    @GetMapping("/{id}")
    public Bail obtenir(@PathVariable String id) {
        return bailService.obtenir(id);
    }

    @PostMapping("/{id}/resilier")
    public Bail resilier(@PathVariable String id) {
        return bailService.resilier(id);
    }
}
