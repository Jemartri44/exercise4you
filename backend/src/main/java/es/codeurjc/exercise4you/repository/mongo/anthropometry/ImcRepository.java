package es.codeurjc.exercise4you.repository.mongo.anthropometry;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import es.codeurjc.exercise4you.entity.anthropometry.Imc;
import es.codeurjc.exercise4you.entity.anthropometry.dto.ImcDto;

public interface ImcRepository  extends MongoRepository<Imc, String>{
    List<Imc> findByPatientIdOrderBySessionDesc(Integer patientId);

    Optional<ImcDto> findByPatientIdAndSession(Integer patientId, Integer session);

    void deleteByPatientIdAndSession(Integer patientId, Integer session);
}