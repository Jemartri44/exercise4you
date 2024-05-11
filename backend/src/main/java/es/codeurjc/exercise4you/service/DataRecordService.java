package es.codeurjc.exercise4you.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.DataRecord;
import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.anthropometry.Icc;
import es.codeurjc.exercise4you.entity.anthropometry.IdealWeight;
import es.codeurjc.exercise4you.entity.anthropometry.Imc;
import es.codeurjc.exercise4you.entity.anthropometry.SkinFolds;
import es.codeurjc.exercise4you.entity.anthropometry.WaistCircumference;
import es.codeurjc.exercise4you.entity.questionnaire.Apalq;
import es.codeurjc.exercise4you.entity.questionnaire.Cmtcef;
import es.codeurjc.exercise4you.entity.questionnaire.Eparmed;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaq;
import es.codeurjc.exercise4you.entity.questionnaire.Ipaqe;
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

    public LocalDate getCompletionDateBySession(Integer id, Integer session) {
        Optional<DataRecord> optionalDataRecord = dataRecordRepository.findByPatientIdAndNSession(id, session);
        if(!optionalDataRecord.isPresent()){
            throw new InternalError("Data record not found");
        }
        return optionalDataRecord.get().getCompletionDate();
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
    
    public void setApalq(Apalq apalq) {
        if(apalq == null){
            throw new InternalError("Questionnaire is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(apalq.getPatientId(), apalq.getSession());
        dataRecord.setApalq(apalq.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void deleteApalq(Integer patientId, Integer session) {
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(patientId, session);
        dataRecord.setApalq(null);
        dataRecordRepository.save(dataRecord);
        deleteDataRecordIfEmpty(dataRecord);
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

    public void setIpaqe(Ipaqe ipaqe) {
        if(ipaqe == null){
            throw new InternalError("Questionnaire is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(ipaqe.getPatientId(), ipaqe.getSession());
        dataRecord.setIpaq(ipaqe.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void deleteIpaqe(Integer patientId, Integer session) {
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(patientId, session);
        dataRecord.setIpaqe(null);
        dataRecordRepository.save(dataRecord);
        deleteDataRecordIfEmpty(dataRecord);
    }

    public void setCmtcef(Cmtcef cmtcef) {
        if(cmtcef == null){
            throw new InternalError("Questionnaire is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(cmtcef.getPatientId(), cmtcef.getSession());
        dataRecord.setCmtcef(cmtcef.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void deleteCmtcef(Integer patientId, Integer session) {
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(patientId, session);
        dataRecord.setCmtcef(null);
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

    
    public void setImc(Imc imc) {
        if(imc == null){
            throw new InternalError("Anthropometry is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(imc.getPatientId(), imc.getSession());
        dataRecord.setImc(imc.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void setIcc(Icc icc) {
        if(icc == null){
            throw new InternalError("Anthropometry is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(icc.getPatientId(), icc.getSession());
        dataRecord.setIcc(icc.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void setWaistCircumference(WaistCircumference waistCircumference) {
        if(waistCircumference == null){
            throw new InternalError("Anthropometry is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(waistCircumference.getPatientId(), waistCircumference.getSession());
        dataRecord.setWaistCircumference(waistCircumference.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void setIdealWeight(IdealWeight idealWeight) {
        if(idealWeight == null){
            throw new InternalError("Anthropometry is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(idealWeight.getPatientId(), idealWeight.getSession());
        dataRecord.setIdealWeight(idealWeight.getId());
        dataRecordRepository.save(dataRecord);
    }

    public void setSkinFolds(SkinFolds skinFolds) {
        if(skinFolds == null){
            throw new InternalError("Anthropometry is not valid");
        }
        // We get the data record we should update
        DataRecord dataRecord = getCurrentDataRecord(skinFolds.getPatientId(), skinFolds.getSession());
        dataRecord.setSkinFolds(skinFolds.getId());
        dataRecordRepository.save(dataRecord);
    }

}
