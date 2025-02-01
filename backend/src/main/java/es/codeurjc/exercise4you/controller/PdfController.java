package es.codeurjc.exercise4you.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import es.codeurjc.exercise4you.service.questionnaire.PdfService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/v1")
public class PdfController {

    @Autowired
    private final PdfService pdfService;

    @GetMapping("/manual")
    public ResponseEntity<Resource> downloadManual() throws IOException {   
        MultipartFile multipartFile = pdfService.getManual();
        byte[] array = multipartFile.getBytes();

        ByteArrayResource resource = new ByteArrayResource(array);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                            .filename(multipartFile.getName())
                            .build().toString())
                .body(resource);
    }

    @GetMapping("/skin-folds-guide")
    public ResponseEntity<Resource> downloadSkinFoldsGuide() throws IOException {   
        MultipartFile multipartFile = pdfService.getSkinFoldsGuide();
        byte[] array = multipartFile.getBytes();

        ByteArrayResource resource = new ByteArrayResource(array);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                            .filename(multipartFile.getName())
                            .build().toString())
                .body(resource);
    }

    @GetMapping("/pdf/{id}/{pdfType}/{nSession}")
    public ResponseEntity<Resource> downloadPdf(@PathVariable Integer id, @PathVariable String pdfType, @PathVariable Integer nSession) throws IOException {   
        MultipartFile multipartFile = pdfService.getPdf(id, pdfType.replace("-X","").replace("-","").toLowerCase(), nSession);
        byte[] array = multipartFile.getBytes();

        ByteArrayResource resource = new ByteArrayResource(array);
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                            .filename(multipartFile.getName())
                            .build().toString())
                .body(resource);
    }
}
