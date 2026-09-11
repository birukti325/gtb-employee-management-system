package com.sms.gtbemployeemanagementsystem.Service;

import com.sms.gtbemployeemanagementsystem.Entity.Employee;
import com.sms.gtbemployeemanagementsystem.Entity.StaffDocument;
import com.sms.gtbemployeemanagementsystem.Repository.StaffDocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Component
public class StaffDocumentService {

    @Autowired
    private StaffDocumentRepository staffDocumentRepository;

    private static final Path DOCS_DIR =
            Path.of(System.getProperty("user.home"), ".gtb_ems", "staff_documents");

    public List<StaffDocument> getDocuments(Long employeeId) {
        return staffDocumentRepository.findByEmployee_IdOrderByUploadedAtDesc(employeeId);
    }

    public void uploadDocument(Employee employee, String documentType, File sourceFile) throws IOException {
        Files.createDirectories(DOCS_DIR);
        String storedName = UUID.randomUUID() + "_" + sourceFile.getName();
        Path destination = DOCS_DIR.resolve(storedName);
        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        StaffDocument doc = new StaffDocument();
        doc.setEmployee(employee);
        doc.setDocumentType(documentType);
        doc.setFileName(sourceFile.getName());
        doc.setFilePath(storedName);
        staffDocumentRepository.save(doc);
    }

    public void deleteDocument(StaffDocument document) {
        try {
            Path filePath = DOCS_DIR.resolve(document.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        staffDocumentRepository.delete(document);
    }

    public File resolveFile(StaffDocument document) {
        return DOCS_DIR.resolve(document.getFilePath()).toFile();
    }
}