package com.dicom.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface DicomService {

    String uploadDicom(MultipartFile file);

    String getDicomHeader(String tag, String fileName);

    ResponseEntity<byte[]> viewDicomAsPNG(String fileName) throws IOException;
}
