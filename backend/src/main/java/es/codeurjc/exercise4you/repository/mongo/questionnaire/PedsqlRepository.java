package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.questionnaire.Pedsql;

@Repository
public interface PedsqlRepository extends MongoRepository<Pedsql, String>{

    @Query("{ 'patientId' : ?0 }")
    List<Pedsql> findByPatientId(Integer patientId);

    Optional<Pedsql> findById(String id);

    Optional<Pedsql> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);

    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<Pedsql> findBySessionAndPatientId(Integer session, Integer id);

    Long deletePedsqlByPatientIdAndSession(Integer patientId, Integer session);

    
}