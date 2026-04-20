package br.com.clinicah.repository;

import br.com.clinicah.model.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Integer> {

    @Query("SELECT DISTINCT d.specialty FROM Doctor d WHERE d.specialty IS NOT NULL ORDER BY d.specialty")
    List<String> findDistinctSpecialties();

    Page<Doctor> findBySpecialtyIgnoreCase(String specialty, Pageable pageable);
}
