package es.codeurjc.exercise4you.repository.mongo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import es.codeurjc.exercise4you.entity.biometrics.BiometricsData;
import es.codeurjc.exercise4you.entity.biometrics.BiometricsDataDto;


public interface BiometricsDataRepository  extends MongoRepository<BiometricsData, String>{
    List<BiometricsData> findByPatientIdOrderBySessionDesc(Integer patientId);

    Optional<BiometricsDataDto> findByPatientIdAndSession(Integer patientId, Integer session);

    void deleteByPatientIdAndSession(Integer patientId, Integer session);
}
