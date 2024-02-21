package com.dicom.service;

import jakarta.servlet.http.HttpServletResponse;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.io.DicomInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Objects;
import java.io.File;
import org.apache.log4j.Logger;



// Implement the DicomService here.
@Service
public class DicomServiceImpl implements DicomService {

    private static final String UPLOAD_DIR = "C:\\DICOM\\";
    private static final Logger logger = Logger.getLogger(DicomServiceImpl.class);

    /**
     * Uploads a DICOM file to the server.
     *
     * @param file The DICOM file to upload
     * @return A message indicating the success or failure of the upload
     */
    @Override
    public String uploadDicom(MultipartFile file) {
        try {
            File dir = new File(UPLOAD_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
                logger.info("Created upload directory: " + UPLOAD_DIR);
            }
            String fileName = UPLOAD_DIR + Objects.requireNonNull(file.getOriginalFilename());
            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(file.getBytes());
            logger.info("Uploaded DICOM file successfully: " + fileName);
            return "Uploaded DICOM file successfully: " + fileName;
        } catch (IOException e) {
            logger.error("Failed to upload DICOM file: " + e.getMessage(), e);
            return "Failed to upload DICOM file.";
        }

    }

    /**
     * Retrieves a DICOM header attribute based on the DICOM tag.
     *
     * @param tagStr The DICOM tag in hexadecimal format
     * @param fileName The name of the DICOM file
     * @return The value of the DICOM header attribute, or an error message if not found
     */
    @Override
    public String getDicomHeader(String tagStr, String fileName) {
        try {
            File dicomFile = new File(UPLOAD_DIR + fileName);
            if (!dicomFile.exists()) {
                return "DICOM file not found.";
            }

            DicomInputStream dicomInputStream = new DicomInputStream(new FileInputStream(dicomFile));
            Attributes attributes = dicomInputStream.readDataset(-1, Tag.PixelData);
            System.out.println(attributes.size());
            dicomInputStream.close();

            String[] parts = tagStr.split(",");
            if (parts.length != 2) {
                return "Invalid DICOM tag format.";
            }

            int groupNumber = Integer.parseInt(parts[0], 16);
            int elementNumber = Integer.parseInt(parts[1], 16);
            int tag = (groupNumber << 16) | elementNumber;

            if (attributes.contains(tag)) {
                return attributes.getString(tag);
            } else {
                return "DICOM tag not found.";
            }
        } catch (NumberFormatException e) {
            return "Invalid DICOM tag format.";
        } catch (IOException e) {
            return "Error reading DICOM file: " + e.getMessage();
        }
    }

    /**
     * Retrieves a DICOM file as a PNG image.
     *
     * @param fileName The name of the DICOM file
     * @return The DICOM file converted to a PNG image
     */
    @Override
    public ResponseEntity<byte[]> viewDicomAsPNG(String fileName) {
        BufferedImage bufferedImage =null;
        File dicomFile = new File(UPLOAD_DIR + fileName);
        Iterator<ImageReader> iterator =ImageIO.getImageReadersByFormatName("DICOM");
        while (iterator.hasNext()) {
            ImageReader imageReader = (ImageReader) iterator.next();
            DicomImageReadParam dicomImageReadParam = (DicomImageReadParam) imageReader.getDefaultReadParam();
            try {
                ImageInputStream imageInputStream = ImageIO.createImageInputStream(dicomFile);
                imageReader.setInput(imageInputStream, false);
                bufferedImage = imageReader.read(0, dicomImageReadParam);
                imageInputStream.close();
                if (bufferedImage == null) {
                    logger.error("Could not read image.");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                }
            } catch (IOException e) {
               logger.error("Error reading DICOM file" + e.getMessage(), e);
               return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            try {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", byteArrayOutputStream);
                byte[] imageData = byteArrayOutputStream.toByteArray();
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_PNG)
                        .body(imageData);

            } catch (IOException e) {
                logger.error("Error writing PNG file" + e.getMessage(), e);
            }
        }
        return null;
    }
 }
