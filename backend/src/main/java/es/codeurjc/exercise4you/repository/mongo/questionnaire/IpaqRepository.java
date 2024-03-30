package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.questionnaire.Ipaq;

@Repository
public interface IpaqRepository extends MongoRepository<Ipaq, String>{

    @Query("{ 'patientId' : ?0 }")
    List<Ipaq> findByPatientId(Integer patientId);

    Optional<Ipaq> findById(String id);

    Optional<Ipaq> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);

    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<Ipaq> findBySessionAndPatientId(Integer session, Integer id);

    Long deleteIpaqByPatientIdAndSession(Integer patientId, Integer session);

    
}