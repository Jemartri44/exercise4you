package es.codeurjc.exercise4you.repository.mongo.anthropometry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import es.codeurjc.exercise4you.entity.anthropometry.SkinFolds;
import es.codeurjc.exercise4you.entity.anthropometry.dto.SkinFoldsDto;

public interface SkinFoldsRepository  extends MongoRepository<SkinFolds, String>{
    List<SkinFolds> findByPatientIdOrderBySessionDesc(Integer patientId);

    Optional<SkinFoldsDto> findSkinFoldsDtoByPatientIdAndSession(Integer patientId, Integer session);

    Optional<SkinFolds> findByPatientIdAndSession(Integer patientId, Integer session);

    void deleteByPatientIdAndSession(Integer patientId, Integer session);
}
