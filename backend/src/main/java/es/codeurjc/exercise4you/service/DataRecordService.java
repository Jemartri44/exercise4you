package es.codeurjc.exercise4you.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.DataRecord;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.questionnaire.Eparmed;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaq;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;

@Service
public class DataRecordService {

    @Autowired
    private DataRecordRepository dataRecordRepository;
    @Autowired
    private PatientRepository patientRepository;

    public Integer getSessionNumber(Integer id) {
        List<DataRecord> dataRecordList = dataRecordRepository.findByPatientIdOrderByNSessionAsc(id);
        if(dataRecordList.isEmpty()){
            return 1;
        }
        DataRecord dataRecord = dataRecordList.get(dataRecordList.size()-1);
        if(dataRecord.getCompletionDate().equals(LocalDate.now())){
            return dataRecord.getNSession();
        }
        return dataRecord.getNSession()+1;
    }

    private DataRecord getCurrentDataRecord(Integer patientId, Integer session) {
        Optional<Patient> optionalPatient = patientRepository.findById(patientId);
        if(!optionalPatient.isPresent()){
            throw new InternalError("Patient not found");
        }
        Patient patient = optionalPatient.get();
        Optional<DataRecord> optionalDataRecord = dataRecordRepository.findByPatientIdAndCompletionDate(patient, LocalDate.now());
        DataRecord dataRecord;
        if(!optionalDataRecord.isPresent()){
            dataRecord = DataRecord.builder()
                .patientId(patient)
                .completionDate(LocalDate.now())
                .nSession(session).build();
            dataRecordRepository.save(dataRecord);
        } else {
            dataRecord = optionalDataRecord.get();
        }
        return dataRecord;
    }

    private void deleteDataRecordIfEmpty(DataRecord dataRecord) {
        if(dataRecord.getIpaq() != null){
            return;
        }
        dataRecordRepository.delete(dataRecord);
    }

    public void setIpaq(Ipaq ipaq) {
        if(ipaq == null){
            throw new InternalError("Questionnaire is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(ipaq.getPatientId(), ipaq.getSession());
        dataRecord.setIpaq(ipaq.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void deleteIpaq(Integer patientId, Integer session) {
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(patientId, session);
        dataRecord.setIpaq(null);
        dataRecordRepository.save(dataRecord);
        deleteDataRecordIfEmpty(dataRecord);
    }

    public void setEparmed(Eparmed eparmed) {
        if(eparmed == null){
            throw new InternalError("Questionnaire is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(eparmed.getPatientId(), eparmed.getSession());
        dataRecord.setEparmed(eparmed.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void deleteEparmed(Integer patientId, Integer session) {
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(patientId, session);
        dataRecord.setEparmed(null);
        dataRecordRepository.save(dataRecord);
        deleteDataRecordIfEmpty(dataRecord);
    }
}
