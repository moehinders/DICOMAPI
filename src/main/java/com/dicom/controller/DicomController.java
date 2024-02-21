package com.dicom.controller;

import com.dicom.service.DicomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class DicomController {

    // Autowire the service.
    @Autowired
    private DicomService dicomService;

    // Mapping the "upload" feature.
    @PostMapping("/upload") public String uploadDicom(@RequestParam("file") MultipartFile file) {
        return dicomService.uploadDicom(file);
    }

    // Mapping the getDicomHeader feature with the "tag" and "file" as query parameters.
    @GetMapping("/header")
    public String getDicomHeader(@RequestParam("tag") String tag, @RequestParam("file") String fileName) {
        return dicomService.getDicomHeader(tag, fileName);
    }

    // Mapping the "view" feature with the "file" as the query parameter.
    @GetMapping("/view")
    public ResponseEntity<byte[]> viewDicomAsPNG(@RequestParam("file") String fileName) {
        try {
            return dicomService.viewDicomAsPNG(fileName);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
            return null;
        }
    }
}
