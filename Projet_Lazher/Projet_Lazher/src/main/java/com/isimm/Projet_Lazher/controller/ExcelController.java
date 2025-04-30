package com.isimm.Projet_Lazher.controller;

import com.isimm.Projet_Lazher.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/excel")
public class ExcelController {
    private static final Logger logger = LoggerFactory.getLogger(ExcelController.class);

    @Autowired
    private ExcelService excelService;

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        try {
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Please select a file to upload");
            }

            String contentType = file.getContentType();
            logger.info("Processing file: {} of type: {}", file.getOriginalFilename(), contentType);
            
            if (!contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") &&
                !contentType.equals("application/vnd.ms-excel")) {
                throw new IllegalArgumentException("Please upload an Excel file");
            }
            
            excelService.processExcelFile(file);
            logger.info("File processed successfully");
            
            return "redirect:/schedules?success=true";
        } catch (Exception e) {
            logger.error("Error processing file: ", e);
            model.addAttribute("error", "Error processing file: " + e.getMessage());
            return "error";
        }
    }
} 