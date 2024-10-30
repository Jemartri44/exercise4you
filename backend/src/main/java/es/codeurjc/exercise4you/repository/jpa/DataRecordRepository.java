package es.codeurjc.exercise4you.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import es.codeurjc.exercise4you.entity.DataRecord;
import es.codeurjc.exercise4you.entity.Patient;

public interface DataRecordRepository extends JpaRepository<DataRecord, Integer>{
    
    Optional<DataRecord> findById(Integer id);

    @Query(value = "SELECT * FROM data_record d WHERE d.patient_id = :id", nativeQuery = true)
    List<DataRecord> findByPatientId(Integer id);

    @Query(value = "SELECT * FROM data_record d WHERE d.patient_id = :id ORDER BY d.n_session asc", nativeQuery = true)
    List<DataRecord> findByPatientIdOrderByNSessionAsc(Integer id);

    Optional<DataRecord> findByPatientIdAndCompletionDate(Patient patient, LocalDate date);

    @Query(value = "SELECT * FROM data_record d WHERE d.patient_id = :id AND d.n_session = :session", nativeQuery = true)
    Optional<DataRecord> findByPatientIdAndNSession(Integer id, Integer session);
}
