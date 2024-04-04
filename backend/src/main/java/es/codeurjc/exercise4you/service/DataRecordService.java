package es.codeurjc.exercise4you.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import es.codeurjc.exercise4you.entity.DataRecord;
import es.codeurjc.exercise4you.repository.jpa.DataRecordRepository;

@Service
public class DataRecordService {

    @Autowired
    private DataRecordRepository dataRecordRepository;

    public Integer getSessionNumber(Integer id) {
        List<DataRecord> dataRecordList = dataRecordRepository.findByPatientId(id);
        if(dataRecordList.isEmpty()){
            return 1;
        }
        DataRecord dataRecord = dataRecordList.get(dataRecordList.size()-1);
        if(dataRecord.getCompletionDate().equals(LocalDate.now())){
            System.out.println("COINCIDE");
            return dataRecord.getNSession();
        }
        System.out.println(dataRecord.getNSession()+1);
        return dataRecord.getNSession()+1;
    }
}
