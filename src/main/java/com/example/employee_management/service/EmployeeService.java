package com.example.employee_management.service;

import com.example.employee_management.model.Employee;
import com.example.employee_management.repository.EmployeeRepository;
import org.bson.internal.BsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeeService {
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.username}")
    private String adminEmail;

    public Employee createEmployee(Employee employee){

        Employee saved = employeeRepository.save(employee);
        try{
            // Email to Admin
            emailService.sendAdminEmployeeAddedEmail(
                    adminEmail,
                    saved.getName(),
                    saved.getDepartment()
            );

            //Welcome Email to Employee
            emailService.sendWelcomeEmail(
                    saved.getEmail(),
                    saved.getName(),
                    saved.getDepartment()
            );
        }catch(Exception e){
            System.out.println("Email sending failed : "+e.getMessage());
        }
        return saved;
    }

    public List<Employee> getAllEmployees(){

        return employeeRepository.findAll();
    }

    public Optional<Employee> getEmployeeById(String id){

        return employeeRepository.findById(id);
    }

    public Employee updateEmployee(String id, Employee updatedEmployee){
        updatedEmployee.setId(id);
        return employeeRepository.save(updatedEmployee);
    }

    public void deleteEmployee(String id){

        Employee employee = employeeRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Employee Not found"));

        try{
            //Email To Admin
            emailService.sendAdminEmployeeDeletedEmail(
                    adminEmail,
                    employee.getName(),
                    employee.getDepartment()
            );

            //Removal Email to Employee
            emailService.sendEmployeeRemovedEmail(
                    employee.getEmail(),
                    employee.getName()
            );
        }catch(Exception e){
            System.out.println("Email Sending Failed : "+e.getMessage());
        }

        employeeRepository.deleteById(id);
    }
}
