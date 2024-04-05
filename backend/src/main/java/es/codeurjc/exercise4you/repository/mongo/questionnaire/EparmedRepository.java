package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.questionnaire.Eparmed;

@Repository
public interface EparmedRepository extends MongoRepository<Eparmed, String>{

    @Query("{ 'patientId' : ?0 }")
    List<Eparmed> findByPatientId(Integer patientId);

    Optional<Eparmed> findById(String id);

    Optional<Eparmed> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);

    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<Eparmed> findBySessionAndPatientId(Integer session, Integer id);

    Long deleteEparmedByPatientIdAndSession(Integer patientId, Integer session);

    
}