package es.codeurjc.exercise4you.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.Patient;
import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryAllData;
import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryData;
import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryGeneralData;
import es.codeurjc.exercise4you.entity.anthropometry.Icc;
import es.codeurjc.exercise4you.entity.anthropometry.IdealWeight;
import es.codeurjc.exercise4you.entity.anthropometry.Imc;
import es.codeurjc.exercise4you.entity.anthropometry.SkinFolds;
import es.codeurjc.exercise4you.entity.anthropometry.WaistCircumference;
import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryAllData.Previous;
import es.codeurjc.exercise4you.entity.anthropometry.AnthropometryGeneralData.Session;
import es.codeurjc.exercise4you.entity.anthropometry.dto.IccDto;
import es.codeurjc.exercise4you.entity.anthropometry.dto.IdealWeightDto;
import es.codeurjc.exercise4you.entity.anthropometry.dto.ImcDto;
import es.codeurjc.exercise4you.entity.anthropometry.dto.SkinFoldsDto;
import es.codeurjc.exercise4you.entity.anthropometry.dto.WaistCircumferenceDto;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;
import es.codeurjc.exercise4you.repository.jpa.PatientRepository;
import es.codeurjc.exercise4you.repository.mongo.anthropometry.IccRepository;
import es.codeurjc.exercise4you.repository.mongo.anthropometry.IdealWeightRepository;
import es.codeurjc.exercise4you.repository.mongo.anthropometry.ImcRepository;
import es.codeurjc.exercise4you.repository.mongo.anthropometry.SkinFoldsRepository;
import es.codeurjc.exercise4you.repository.mongo.anthropometry.WaistCircumferenceRepository;
import es.codeurjc.exercise4you.service.auth.AuthService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AnthropometryService {

    @Autowired
    private final AuthService authService;
    @Autowired
    private final DataRecordService dataRecordService;
    @Autowired
    private final DataRecordRepository dataRecordRepository;
    @Autowired
    private final PatientRepository patientRepository;
    @Autowired
    private final ImcRepository imcRepository;
    @Autowired
    private final IccRepository iccRepository;
    @Autowired
    private final WaistCircumferenceRepository waistCircumferenceRepository;
    @Autowired
    private final IdealWeightRepository idealWeightRepository;
    @Autowired
    private final SkinFoldsRepository skinFoldsRepository;


    public AnthropometryGeneralData getImcGeneralData(Integer id) {
        checkPatient(id);
        List<Imc> imcList = imcRepository.findByPatientIdOrderBySessionDesc(id);
        List<Session> sessions = new ArrayList<>();
        boolean allEmpty = true;
        boolean todayCompleted = false;
        Session today = Session.builder().date(LocalDate.now()).number(dataRecordService.getSessionNumber(id)).build();
        ImcDto data = null;
        if(!imcList.isEmpty()){
            allEmpty = false;
            if(imcList.get(0).getCompletionDate().equals(LocalDate.now())){
                data = new ImcDto(imcList.get(0));
                todayCompleted = true;
                imcList.remove(0);
            }
        }
        for(Imc imc : imcList){
            sessions.add(Session.builder().date(imc.getCompletionDate()).number(imc.getSession()).build());
        }
        return AnthropometryGeneralData.builder().data(data).allEmpty(allEmpty).sessions(sessions).today(today).todayCompleted(todayCompleted).build();
    }

    public AnthropometryGeneralData getIccGeneralData(Integer id) {
        checkPatient(id);
        List<Icc> iccList = iccRepository.findByPatientIdOrderBySessionDesc(id);
        List<Session> sessions = new ArrayList<>();
        boolean allEmpty = true;
        boolean todayCompleted = false;
        Session today = Session.builder().date(LocalDate.now()).number(dataRecordService.getSessionNumber(id)).build();
        IccDto data = new IccDto();
        if(!iccList.isEmpty()){
            allEmpty = false;
            if(iccList.get(0).getCompletionDate().equals(LocalDate.now())){
                data = new IccDto(iccList.get(0));
                todayCompleted = true;
                iccList.remove(0);
            }
        }
        boolean gender = getPatientGender(id);
        data.setGender(gender);
        for(Icc icc : iccList){
            sessions.add(Session.builder().date(icc.getCompletionDate()).number(icc.getSession()).build());
        }
        return AnthropometryGeneralData.builder().data(data).allEmpty(allEmpty).sessions(sessions).today(today).todayCompleted(todayCompleted).build();
    }

    public AnthropometryGeneralData getWaistCircumferenceGeneralData(Integer id) {
        checkPatient(id);
        List<WaistCircumference> waistCircumferenceList = waistCircumferenceRepository.findByPatientIdOrderBySessionDesc(id);
        List<Session> sessions = new ArrayList<>();
        boolean allEmpty = true;
        boolean todayCompleted = false;
        Session today = Session.builder().date(LocalDate.now()).number(dataRecordService.getSessionNumber(id)).build();
        WaistCircumferenceDto data = new WaistCircumferenceDto();
        if(!waistCircumferenceList.isEmpty()){
            allEmpty = false;
            if(waistCircumferenceList.get(0).getCompletionDate().equals(LocalDate.now())){
                data = new WaistCircumferenceDto(waistCircumferenceList.get(0));
                todayCompleted = true;
                waistCircumferenceList.remove(0);
            }
        }
        boolean gender = getPatientGender(id);
        data.setGender(gender);
        for(WaistCircumference waistCircumference : waistCircumferenceList){
            sessions.add(Session.builder().date(waistCircumference.getCompletionDate()).number(waistCircumference.getSession()).build());
        }
        return AnthropometryGeneralData.builder().data(data).allEmpty(allEmpty).sessions(sessions).today(today).todayCompleted(todayCompleted).build();
    }

    public AnthropometryGeneralData getIdealWeightGeneralData(Integer id) {
        checkPatient(id);
        List<IdealWeight> idealWeightList = idealWeightRepository.findByPatientIdOrderBySessionDesc(id);
        List<Session> sessions = new ArrayList<>();
        boolean allEmpty = true;
        boolean todayCompleted = false;
        Session today = Session.builder().date(LocalDate.now()).number(dataRecordService.getSessionNumber(id)).build();
        IdealWeightDto data = new IdealWeightDto();
        data.setData(new IdealWeight.DataInfo("Fórmula de Lorentz"));
        if(!idealWeightList.isEmpty()){
            allEmpty = false;
            if(idealWeightList.get(0).getCompletionDate().equals(LocalDate.now())){
                data = new IdealWeightDto(idealWeightList.get(0));
                todayCompleted = true;
                idealWeightList.remove(0);
            } else {
                if(idealWeightList.size()>1) {
                    data.setData(new IdealWeight.DataInfo(idealWeightList.get(1).getData().getFormula()));
                }
            }
        }
        boolean gender = getPatientGender(id);
        data.setGender(gender);
        for(IdealWeight idealWeight : idealWeightList){
            sessions.add(Session.builder().date(idealWeight.getCompletionDate()).number(idealWeight.getSession()).build());
        }
        return AnthropometryGeneralData.builder().data(data).allEmpty(allEmpty).sessions(sessions).today(today).todayCompleted(todayCompleted).build();
    }

    public AnthropometryGeneralData getSkinFoldsGeneralData(Integer id) {
        checkPatient(id);
        List<SkinFolds> skinFoldsList = skinFoldsRepository.findByPatientIdOrderBySessionDesc(id);
        List<Session> sessions = new ArrayList<>();
        boolean allEmpty = true;
        boolean todayCompleted = false;
        Session today = Session.builder().date(LocalDate.now()).number(dataRecordService.getSessionNumber(id)).build();
        SkinFoldsDto data = new SkinFoldsDto();
        if(!skinFoldsList.isEmpty()){
            allEmpty = false;
            if(skinFoldsList.get(0).getCompletionDate().equals(LocalDate.now())){
                data = new SkinFoldsDto(skinFoldsList.get(0));
                todayCompleted = true;
                skinFoldsList.remove(0);
            }
        }
        boolean gender = getPatientGender(id);
        data.setGender(gender);
        int age = getYearsBetween(getPatientBirthdate(id), LocalDate.now());
        data.setAge(age);
        for(SkinFolds skinFolds : skinFoldsList){
            sessions.add(Session.builder().date(skinFolds.getCompletionDate()).number(skinFolds.getSession()).build());
        }
        return AnthropometryGeneralData.builder().data(data).allEmpty(allEmpty).sessions(sessions).today(today).todayCompleted(todayCompleted).build();
    }


    public AnthropometryAllData getImcAllData(Integer id) {
        checkPatient(id);
        List<Imc> imcList = imcRepository.findByPatientIdOrderBySessionDesc(id);
        List<Previous> previousList = new ArrayList<>();
        if(imcList.isEmpty()){
            return AnthropometryAllData.builder().empty(true).todayCompleted(false).previous(previousList).build();
        }
        for(Imc imc : imcList){
            previousList.add(Previous.builder().session(Session.builder().date(imc.getCompletionDate()).number(imc.getSession()).build()).anthropometry(new ImcDto(imc)).build());
        }
        return AnthropometryAllData.builder().empty(false).todayCompleted(imcList.get(imcList.size()-1).getCompletionDate().equals(LocalDate.now())).previous(previousList).build();
    }

    public AnthropometryAllData getIccAllData(Integer id) {
        checkPatient(id);
        List<Icc> iccList = iccRepository.findByPatientIdOrderBySessionDesc(id);
        List<Previous> previousList = new ArrayList<>();
        if(iccList.isEmpty()){
            return AnthropometryAllData.builder().empty(true).todayCompleted(false).previous(previousList).build();
        }
        boolean gender = getPatientGender(id);
        for(Icc icc : iccList){
            previousList.add(Previous.builder().session(Session.builder().date(icc.getCompletionDate()).number(icc.getSession()).build()).anthropometry(new IccDto(icc, gender)).build());
        }
        return AnthropometryAllData.builder().empty(false).todayCompleted(iccList.get(iccList.size()-1).getCompletionDate().equals(LocalDate.now())).previous(previousList).build();
    }

    public AnthropometryAllData getWaistCircumferenceAllData(Integer id) {
        checkPatient(id);
        List<WaistCircumference> waistCircumferenceList = waistCircumferenceRepository.findByPatientIdOrderBySessionDesc(id);
        List<Previous> previousList = new ArrayList<>();
        if(waistCircumferenceList.isEmpty()){
            return AnthropometryAllData.builder().empty(true).todayCompleted(false).previous(previousList).build();
        }
        boolean gender = getPatientGender(id);
        for(WaistCircumference waistCircumference : waistCircumferenceList){
            previousList.add(Previous.builder().session(Session.builder().date(waistCircumference.getCompletionDate()).number(waistCircumference.getSession()).build()).anthropometry(new WaistCircumferenceDto(waistCircumference, gender)).build());
        }
        return AnthropometryAllData.builder().empty(false).todayCompleted(waistCircumferenceList.get(waistCircumferenceList.size()-1).getCompletionDate().equals(LocalDate.now())).previous(previousList).build();
    }

    public AnthropometryAllData getIdealWeightAllData(Integer id) {
        checkPatient(id);
        List<IdealWeight> idealWeightList = idealWeightRepository.findByPatientIdOrderBySessionDesc(id);
        List<Previous> previousList = new ArrayList<>();
        if(idealWeightList.isEmpty()){
            return AnthropometryAllData.builder().empty(true).todayCompleted(false).previous(previousList).build();
        }
        boolean gender = getPatientGender(id);
        for(IdealWeight idealWeight : idealWeightList){
            previousList.add(Previous.builder().session(Session.builder().date(idealWeight.getCompletionDate()).number(idealWeight.getSession()).build()).anthropometry(new IdealWeightDto(idealWeight, gender)).build());
        }
        return AnthropometryAllData.builder().empty(false).todayCompleted(idealWeightList.get(idealWeightList.size()-1).getCompletionDate().equals(LocalDate.now())).previous(previousList).build();
    }

    public AnthropometryAllData getSkinFoldsAllData(Integer id) {
        checkPatient(id);
        List<SkinFolds> skinFoldsList = skinFoldsRepository.findByPatientIdOrderBySessionDesc(id);
        List<Previous> previousList = new ArrayList<>();
        if(skinFoldsList.isEmpty()){
            return AnthropometryAllData.builder().empty(true).todayCompleted(false).previous(previousList).build();
        }
        boolean gender = getPatientGender(id);
        LocalDate birthdate = getPatientBirthdate(id);
        for(SkinFolds skinFolds : skinFoldsList){
            previousList.add(Previous.builder().session(Session.builder().date(skinFolds.getCompletionDate()).number(skinFolds.getSession()).build()).anthropometry(new SkinFoldsDto(skinFolds, gender, getYearsBetween(birthdate, skinFolds.getCompletionDate()))).build());
        }
        return AnthropometryAllData.builder().empty(false).todayCompleted(skinFoldsList.get(skinFoldsList.size()-1).getCompletionDate().equals(LocalDate.now())).previous(previousList).build();
    }


    public AnthropometryData getImcData(Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        Optional<ImcDto> imc = imcRepository.findByPatientIdAndSession(id, nSession);
        if(!imc.isPresent()){
            if(nSession.equals(dataRecordService.getSessionNumber(id))){
                return new ImcDto();
            }
            throw new RuntimeException("Data not found");
        }
        return imc.get();
    }

    public AnthropometryData getIccData(Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        Optional<IccDto> optional = iccRepository.findByPatientIdAndSession(id, nSession);
        if(!optional.isPresent()){
            if(nSession.equals(dataRecordService.getSessionNumber(id))){
                return new IccDto();
            }
            throw new RuntimeException("Data not found");
        }
        boolean gender = getPatientGender(id);
        IccDto icc = optional.get();
        icc.setGender(gender);
        return icc;
    }

    public AnthropometryData getWaistCircumferenceData(Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        Optional<WaistCircumferenceDto> optional = waistCircumferenceRepository.findByPatientIdAndSession(id, nSession);
        if(!optional.isPresent()){
            if(nSession.equals(dataRecordService.getSessionNumber(id))){
                return new WaistCircumferenceDto();
            }
            throw new RuntimeException("Data not found");
        }
        boolean gender = getPatientGender(id);
        WaistCircumferenceDto waistCircumference = optional.get();
        waistCircumference.setGender(gender);
        return waistCircumference;
    }

    public AnthropometryData getIdealWeightData(Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        Optional<IdealWeightDto> optional = idealWeightRepository.findByPatientIdAndSession(id,nSession);
        IdealWeightDto data = new IdealWeightDto();
        boolean gender = getPatientGender(id);
        data.setGender(gender);
        if(!optional.isPresent()){
            if(nSession.equals(dataRecordService.getSessionNumber(id))){
                List<IdealWeight> idealWeightList = idealWeightRepository.findByPatientIdOrderBySessionDesc(id);
                if(idealWeightList.isEmpty()){
                    data.setData(new IdealWeight.DataInfo("Fórmula de Lorentz"));
                } else {
                    data.setData(new IdealWeight.DataInfo(idealWeightList.get(idealWeightList.size()-2).getData().getFormula()));
                }
                return data;
            }
            throw new RuntimeException("Data not found");
        }
        data = optional.get();
        data.setGender(gender);
        return data;
    }

    public AnthropometryData getSkinFoldsData(Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        Optional<SkinFolds> optional = skinFoldsRepository.findByPatientIdAndSession(id, nSession);
        SkinFoldsDto data = new SkinFoldsDto();
        boolean gender = getPatientGender(id);
        data.setGender(gender);
        if(!optional.isPresent()){
            if(nSession.equals(dataRecordService.getSessionNumber(id))){
                List<SkinFolds> skinFoldsList = skinFoldsRepository.findByPatientIdOrderBySessionDesc(id);
                if(skinFoldsList.isEmpty()){
                    data.setData(new SkinFolds.DataInfo());
                } else {
                    data.setData(skinFoldsList.get(skinFoldsList.size()-1).getData());
                }
                data.setAge(getYearsBetween(getPatientBirthdate(id), LocalDate.now()));
                return data;
            }
            throw new RuntimeException("Data not found");
        }
        SkinFolds skinFolds = optional.get();
        data = new SkinFoldsDto(skinFolds);
        int age = getYearsBetween(getPatientBirthdate(id), skinFolds.getCompletionDate());
        data.setGender(gender);
        data.setAge(age);
        return data;
    }


    public void saveImcData(ImcDto imcDto, Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        imcRepository.deleteByPatientIdAndSession(id, nSession);
        Imc imc = Imc.builder().patientId(id).completionDate(LocalDate.now()).session(nSession).data(imcDto.getData()).build();
        imc = imcRepository.save(imc);
        dataRecordService.setImc(imc);
    }

    public void saveIccData(IccDto iccDto, Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        iccRepository.deleteByPatientIdAndSession(id, nSession);
        Icc icc = Icc.builder().patientId(id).completionDate(LocalDate.now()).session(nSession).data(iccDto.getData()).build();
        icc = iccRepository.save(icc);
        dataRecordService.setIcc(icc);
    }

    public void saveWaistCircumferenceData(WaistCircumferenceDto waistCircumferenceDto, Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        waistCircumferenceRepository.deleteByPatientIdAndSession(id, nSession);
        WaistCircumference waistCircumference = WaistCircumference.builder().patientId(id).completionDate(LocalDate.now()).session(nSession).data(waistCircumferenceDto.getData()).build();
        waistCircumference = waistCircumferenceRepository.save(waistCircumference);
        dataRecordService.setWaistCircumference(waistCircumference);
    }

    public void saveIdealWeightData(IdealWeightDto idealWeightDto, Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        idealWeightRepository.deleteByPatientIdAndSession(id, nSession);
        IdealWeight idealWeight = IdealWeight.builder().patientId(id).completionDate(LocalDate.now()).session(nSession).data(idealWeightDto.getData()).build();
        idealWeight = idealWeightRepository.save(idealWeight);
        dataRecordService.setIdealWeight(idealWeight);
    }

    public void saveSkinFoldsData(SkinFoldsDto skinFoldsDto, Integer id, Integer nSession) {
        checkPatient(id);
        checkSession(nSession);
        skinFoldsRepository.deleteByPatientIdAndSession(id, nSession);
        SkinFolds skinFolds = SkinFolds.builder().patientId(id).completionDate(LocalDate.now()).session(nSession).data(skinFoldsDto.getData()).build();
        skinFolds = skinFoldsRepository.save(skinFolds);
        dataRecordService.setSkinFolds(skinFolds);
    }


    private void checkPatient(Integer id) {
        Optional<Patient> optional = patientRepository.findById(id);
        if(!optional.isPresent()){
            throw new RuntimeException("Patient not found");
        }
        if(!optional.get().getUsr().getId().equals(authService.getLoggedUser().getId())){
            throw new RuntimeException("Not allowed");
        }
    }
    
    private void checkSession(Integer session) {
        if(session<1){
            throw new RuntimeException("Invalid session number");
        }
    }

    private boolean getPatientGender(Integer id) {
        Optional<Patient> optional = patientRepository.findById(id);
        if(!optional.isPresent()){
            throw new RuntimeException("Patient not found");
        }
        return optional.get().getGender().equals("Masculino");
    }

    private LocalDate getPatientBirthdate(Integer id) {
        Optional<Patient> optional = patientRepository.findById(id);
        if(!optional.isPresent()){
            throw new RuntimeException("Patient not found");
        }
        return optional.get().getBirthdate();
    }

    private int getYearsBetween(LocalDate date1, LocalDate date2) {
        return Period.between(date1, date2).getYears();
    }

}
