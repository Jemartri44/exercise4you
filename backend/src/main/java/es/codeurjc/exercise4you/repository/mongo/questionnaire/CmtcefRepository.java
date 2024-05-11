package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.questionnaire.Cmtcef;

@Repository
public interface CmtcefRepository extends MongoRepository<Cmtcef, String>{

    @Query("{ 'patientId' : ?0 }")
    List<Cmtcef> findByPatientId(Integer patientId);

    Optional<Cmtcef> findById(String id);

    Optional<Cmtcef> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);

    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<Cmtcef> findBySessionAndPatientId(Integer session, Integer id);

    Long deleteCmtcefByPatientIdAndSession(Integer patientId, Integer session);

    
}