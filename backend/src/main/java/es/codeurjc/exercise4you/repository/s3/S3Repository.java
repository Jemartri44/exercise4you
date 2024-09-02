package es.codeurjc.exercise4you.repository.s3;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import es.codeurjc.exercise4you.entity.Asset;

public interface S3Repository {

    String uploadFile(String bucketName, String fileName, File fileObj);

    String uploadMultipartFile(String bucketName, String fileName, MultipartFile fileObj) throws IOException, SdkClientException;
/*
    List<Asset> listObjectsInBucket(String bucket);

    S3ObjectInputStream getObject(String bucketName, String fileName) throws IOException;

    byte[] downloadFile(String bucketName, String fileName) throws IOException;

    void moveObject(String bucketName, String fileKey, String destinationFileKey);

    void deleteObject(String bucketName, String fileKey);
 */
}