package es.codeurjc.exercise4you.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.SdkClientException;

import es.codeurjc.exercise4you.repository.s3.S3Repository;

@Service
public class S3Service {
    private S3Repository s3Repository;
    @Value("${s3.bucket-name}")
    private String bucketName;

    @Autowired
    public S3Service(S3Repository s3Repository) {
        this.s3Repository = s3Repository;
    }
    
    public String uploadMultipartFile(String filePath, MultipartFile file) throws SdkClientException, IOException {
        String fileName = file.getOriginalFilename();
        return s3Repository.uploadMultipartFile(bucketName, filePath + fileName, file);
    }

    public MultipartFile downloadMultipartFile(String filePath, String filename) throws IOException{
        return s3Repository.downloadMultipartFile(bucketName, filePath, filename);
    }
}
