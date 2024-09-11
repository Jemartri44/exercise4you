package es.codeurjc.exercise4you.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.exercise4you.service.S3Service;

@RestController
@RequestMapping(value = "/s3")
public class AwsController {

    private S3Service s3Service;

    @Autowired
    public AwsController(S3Service s3Service) {

        this.s3Service = s3Service;
    }

    @PostMapping("/uploadFile")
    public ResponseEntity<String> uploadFile(@RequestParam(value = "filePath") String filePath, @RequestParam(value = "file") MultipartFile file) throws IOException {
        return new ResponseEntity<>(s3Service.uploadMultipartFile(filePath, file), HttpStatus.OK);
    }

}