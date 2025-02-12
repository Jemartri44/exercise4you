package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.questionnaire.Sf;

@Repository
public interface SfRepository extends MongoRepository<Sf, String>{

    @Query("{ 'patientId' : ?0 }")
    List<Sf> findByPatientId(Integer patientId);

    Optional<Sf> findById(String id);

    Optional<Sf> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);

    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<Sf> findBySessionAndPatientId(Integer session, Integer id);

    Long deleteSfByPatientIdAndSession(Integer patientId, Integer session);

    
}