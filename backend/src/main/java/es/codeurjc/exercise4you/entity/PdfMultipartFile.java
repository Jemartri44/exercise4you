package es.codeurjc.exercise4you.entity;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class PdfMultipartFile implements MultipartFile {
    private final byte[] pdfContent;
    private final String name;

    public PdfMultipartFile(String name, byte[] pdfContent) {
        this.pdfContent = pdfContent;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return name;
    }

    @Override
    public String getContentType() {
        return("application/pdf");
    }

    @Override
    public boolean isEmpty() {
        return pdfContent == null || pdfContent.length == 0;
    }

    @Override
    public long getSize() {
        return pdfContent.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return pdfContent;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(pdfContent);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException { 
        new FileOutputStream(dest).write(pdfContent);
    }
}