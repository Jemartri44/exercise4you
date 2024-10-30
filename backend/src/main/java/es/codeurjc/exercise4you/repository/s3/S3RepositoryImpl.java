package es.codeurjc.exercise4you.repository.s3;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CopyObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.IOUtils;

import es.codeurjc.exercise4you.entity.Asset;
import es.codeurjc.exercise4you.entity.PdfMultipartFile;

@Repository
public class S3RepositoryImpl implements S3Repository{

    private AmazonS3 s3Client;

    @Autowired
    public S3RepositoryImpl(AmazonS3 s3Client) {

        this.s3Client = s3Client;
    }

    @Override
    public List<Asset> listObjectsInBucket(String bucket) {
        return  s3Client.listObjectsV2(bucket).getObjectSummaries().stream()
                        .parallel()
                        .map(S3ObjectSummary::getKey)
                        .map(key -> mapS3ToObject(bucket, key))
                        .collect(Collectors.toList());

    }
    private Asset mapS3ToObject(String bucket, String key) {

        return Asset.builder()
                .name(s3Client.getObjectMetadata(bucket, key).getUserMetaDataOf("name"))
                .key(key)
                .url(s3Client.getUrl(bucket, key))
                .build();
    }

    @Override
    public S3ObjectInputStream getObject(String bucketName, String filename) throws IOException {
        if (!s3Client.doesBucketExistV2(bucketName)) {
            throw new IOException("Bucket not found");
        }
        S3Object s3object = s3Client.getObject(bucketName, filename);
        return s3object.getObjectContent();
    }

    @Override
    public byte[] downloadFile(String bucketName, String filename) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, filename);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        byte[] content = IOUtils.toByteArray(inputStream);
        return content;
    }

    @Override
    public void moveObject(String bucketName, String fileKey, String destinationFileKey) {
        CopyObjectRequest copyObjRequest = new CopyObjectRequest(bucketName, fileKey, bucketName, destinationFileKey);
        s3Client.copyObject(copyObjRequest);
        deleteObject(bucketName, fileKey);
    }

    @Override
    public void deleteObject (String bucketName, String fileKey) {

        s3Client.deleteObject(bucketName, fileKey);
    }

    @Override
    public String uploadFile(String bucketName, String filename, File fileObj) {
        s3Client.putObject(new PutObjectRequest(bucketName, filename, fileObj));
        fileObj.delete();
        return "File uploaded : " + filename;
    }

    @Override
    public String uploadMultipartFile(String bucketName, String filename, MultipartFile multipartFile) throws IOException, SdkClientException {
        ObjectMetadata data = new ObjectMetadata();
        data.setContentType(multipartFile.getContentType());
        data.setContentLength(multipartFile.getSize());
        s3Client.putObject(bucketName, filename, multipartFile.getInputStream(), data);
        return "File uploaded : " + filename;
    }

    @Override
    public MultipartFile downloadMultipartFile(String bucketName, String filepath, String filename) throws IOException {
        S3Object s3Object = s3Client.getObject(bucketName, filepath + filename);
        S3ObjectInputStream inputStream = s3Object.getObjectContent();
        return new PdfMultipartFile(filename, IOUtils.toByteArray(inputStream));
    }
}