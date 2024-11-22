package es.codeurjc.exercise4you.repository.mongo.anthropometry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import es.codeurjc.exercise4you.entity.anthropometry.IdealWeight;
import es.codeurjc.exercise4you.entity.anthropometry.dto.IdealWeightDto;

public interface IdealWeightRepository  extends MongoRepository<IdealWeight, String>{
    List<IdealWeight> findByPatientIdOrderBySessionDesc(Integer patientId);

    Optional<IdealWeightDto> findByPatientIdAndSession(Integer patientId, Integer session);

    void deleteByPatientIdAndSession(Integer patientId, Integer session);
}
