package es.codeurjc.exercise4you.repository.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.prescriptions.PrescriptionsResponse;

@Repository
public interface PrescriptionsRepository extends MongoRepository<PrescriptionsResponse, String> {
@Query("{ 'patientId' : ?0 }")
    List<PrescriptionsResponse> findByPatientId(Integer patientId);

    Optional<PrescriptionsResponse> findById(String id);

    Optional<PrescriptionsResponse> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);
    
    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<PrescriptionsResponse> findBySessionAndPatientId(Integer session, Integer id);
}
