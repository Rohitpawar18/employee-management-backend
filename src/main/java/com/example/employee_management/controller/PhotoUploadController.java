package com.example.employee_management.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.employee_management.model.Employee;
import com.example.employee_management.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
public class PhotoUploadController {
    @Autowired
    private Cloudinary cloudinary;

    @Autowired
    private EmployeeRepository employeeRepository;

    @PostMapping("/{id}/upload-photo")
    public ResponseEntity<Employee> uploadPhoto(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file){
        try{
            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder","employee-management")
            );
            String photoUrl = (String) uploadResult.get("secure_url");

            Employee employee = employeeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Employee Not Found"));
            employee.setPhotoUrl(photoUrl);
            employeeRepository.save(employee);

            return ResponseEntity.ok(employee);
        }catch(Exception e){
            return ResponseEntity.status(500).build();
        }
    }
}
