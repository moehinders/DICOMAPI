package com.dicom.model;

import org.springframework.web.multipart.MultipartFile;
// DicomFile class with its accessor methods.
public class DicomFile {

    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
