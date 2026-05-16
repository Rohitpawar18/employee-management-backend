package com.example.employee_management.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private void sendEmail(String toEmail, String subject, String body){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    // Admin mail - new employee added
    @Async
    public void sendAdminEmployeeAddedEmail(String adminEmail, String employeeName, String department){
        sendEmail(
                adminEmail,
                "New Employee Added : "+ employeeName,
                "Hello Admin,\n\n"+
                "A new employee has been added to the system.\n\n"+
                "Employee Name : "+employeeName+"\n"+
                "Department    : "+department+"\n\n"+
                "Please login to the Employee Management System for more details.\n\n"+
                "Regards,\nEmployee Management System"
        );
    }

    // Employee Email : welcome email
    @Async
    public void sendWelcomeEmail(String employeeEmail, String employeeName, String department){
        sendEmail(
                employeeEmail,
                "Welcome to the Company, "+employeeName+"!",
                "Dear "+employeeName+",\n\n"+
                "Welcome to company! We are excited to have you on board.\n\n"+
                "Your profile has been successfully added to our system.\n\n" +
                "Department : " + department + "\n\n" +
                "If you have any questions, please contact the HR department.\n\n" +
                "Best Regards,\nEmployee Management System"
        );
    }

    //Admin email : employee deleted
    @Async
    public void  sendAdminEmployeeDeletedEmail(String adminEmail, String employeeName, String department){
        sendEmail(
                adminEmail,
                "Employee Removed : "+employeeName,
                "Hello Admin,\n\n"+
                "An employee has been removed from the system.\n\n"+
                "Employee Name : "+employeeName+"\n"+
                "Department    : "+department+"\n\n"+
                "Please login to the Employee Management System for more details.\n\n"+
                "Regards,\nEmployee Management System"
        );
    }

    // Emloyee email : removal notification
    @Async
    public void sendEmployeeRemovedEmail(String employeeEmail, String employeeName){
        sendEmail(
                employeeEmail,
                "Your Profile Has Been Removed",
                "Dear "+employeeName+",\n\n"+
                "We want to inform you that your profile has been removed from our "+
                "Employee Management System.\n\n"+
                "If you think this is a mistake, please contact the HR department immediately.\n\n"+
                "Best Regards,\nEmployee Management System"
        );
    }
}
