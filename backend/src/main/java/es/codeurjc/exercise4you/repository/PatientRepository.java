package es.codeurjc.exercise4you.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.dto.PatientDTO;

public interface PatientRepository extends JpaRepository<Patient, Integer>{
    Optional<Patient> findById(Integer id);

    @Query(nativeQuery = true)
    List<PatientDTO> findPatientDtoByUsrId(Integer usrId);
}