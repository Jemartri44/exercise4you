package es.codeurjc.exercise4you.repository.mongo.anthropometry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import es.codeurjc.exercise4you.entity.anthropometry.Icc;
import es.codeurjc.exercise4you.entity.anthropometry.dto.IccDto;

public interface IccRepository  extends MongoRepository<Icc, String>{
    List<Icc> findByPatientIdOrderBySessionDesc(Integer patientId);

    Optional<IccDto> findByPatientIdAndSession(Integer patientId, Integer session);

    void deleteByPatientIdAndSession(Integer patientId, Integer session);
}
