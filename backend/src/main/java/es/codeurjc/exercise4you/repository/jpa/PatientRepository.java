package es.codeurjc.exercise4you.repository.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;

public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Optional<Patient> findById(Integer id);

    @Query(nativeQuery = true)
    List<PatientDTO> findPatientDtoByUsrId(Integer usrId);

    @Query(nativeQuery = true)
    Page<PatientDTO> customFindPatientDtoByUsrIdAndNameContainingAndSurnamesContaining(Integer usrId, Pageable pageable);

    @Query(value = "SELECT * FROM patients p WHERE p.usr_id = :usrId AND concat(p.name,' ',p.surnames) like CONCAT('%',:search,'%')", nativeQuery = true)
    Page<Patient> findByUsrIdAndNameContaining(Integer usrId, String search, Pageable pageable);

    @Query(value = "SELECT * FROM patients WHERE name = ?1",
    countQuery = "SELECT count(*) FROM patients WHERE name = ?1",
    nativeQuery = true)
    Page<Patient> findByLastname(String name, Pageable pageable);
}