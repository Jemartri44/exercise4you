package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.questionnaire.Apalq;

@Repository
public interface ApalqRepository extends MongoRepository<Apalq, String>{
    @Query("{ 'patientId' : ?0 }")
    List<Apalq> findByPatientId(Integer patientId);

    Optional<Apalq> findById(String id);

    Optional<Apalq> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);
    
    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<Apalq> findBySessionAndPatientId(Integer session, Integer id);

    Long deleteApalqByPatientIdAndSession(Integer patientId, Integer session);
}
