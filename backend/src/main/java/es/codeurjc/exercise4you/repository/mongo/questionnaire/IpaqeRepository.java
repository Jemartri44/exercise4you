package es.codeurjc.exercise4you.repository.mongo.questionnaire;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import es.codeurjc.exercise4you.entity.questionnaire.Ipaqe;

@Repository
public interface IpaqeRepository extends MongoRepository<Ipaqe, String>{

    @Query("{ 'patientId' : ?0 }")
    List<Ipaqe> findByPatientId(Integer patientId);

    Optional<Ipaqe> findById(String id);

    Optional<Ipaqe> findByPatientIdAndCompletionDate(Integer id, LocalDate localDate);

    @Query("{ 'session' : ?0, 'patientId' : ?1 }")
    Optional<Ipaqe> findBySessionAndPatientId(Integer session, Integer id);

    Long deleteIpaqeByPatientIdAndSession(Integer patientId, Integer session);

    
}
