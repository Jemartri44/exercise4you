package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.questionnaire.Parq;

@Repository
public interface ParqRepository extends MongoRepository<Parq, String>{

    @Query("{ 'patientId' : ?0 }")
    List<Parq> findByPatientId(Integer patientId);

    Optional<Parq> findById(String id);

    Optional<Parq> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);

    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<Parq> findBySessionAndPatientId(Integer session, Integer id);

    Long deleteParqByPatientIdAndSession(Integer patientId, Integer session);

    
}