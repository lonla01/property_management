package com.rental.management.repository;

import com.rental.management.domain.entity.Unite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UniteRepository extends JpaRepository<Unite, String> {
    List<Unite> findByBienId(String bienId);
}
