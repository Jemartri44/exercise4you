package es.codeurjc.exercise4you.repository.mongo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.objectives.ObjectivesResponse;

@Repository
public interface ObjectivesRepository extends MongoRepository<ObjectivesResponse, String> {
    @Query("{ 'patientId' : ?0 }")
    List<ObjectivesResponse> findByPatientId(Integer patientId);

    Optional<ObjectivesResponse> findById(String id);

    Optional<ObjectivesResponse> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);
    
    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<ObjectivesResponse> findBySessionAndPatientId(Integer session, Integer id);
}
