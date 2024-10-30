package es.codeurjc.exercise4you.repository.mongo.anthropometry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import es.codeurjc.exercise4you.entity.anthropometry.WaistCircumference;
import es.codeurjc.exercise4you.entity.anthropometry.dto.WaistCircumferenceDto;

public interface WaistCircumferenceRepository  extends MongoRepository<WaistCircumference, String>{
    List<WaistCircumference> findByPatientIdOrderBySessionDesc(Integer patientId);

    Optional<WaistCircumferenceDto> findByPatientIdAndSession(Integer patientId, Integer session);

    void deleteByPatientIdAndSession(Integer patientId, Integer session);
}
