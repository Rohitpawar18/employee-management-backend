package com.example.employee_management.controller;

import com.example.employee_management.model.Employee;
import com.example.employee_management.repository.EmployeeRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/salary-slip")
@CrossOrigin(origins = "*")
public class SalarySlipController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping("/{id}")
    public ResponseEntity<byte[]> generateSalarySlip(@PathVariable String id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found"));

        try {
            byte[] pdfBytes = createPDF(employee);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("salary-slip-" + employee.getName().replace(" ", "-") + ".pdf")
                            .build()
            );
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private byte[] createPDF(Employee employee) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        // Colors
        BaseColor darkBlue = new BaseColor(26, 26, 46);
        BaseColor blue = new BaseColor(79, 142, 247);
        BaseColor lightGray = new BaseColor(248, 250, 252);
        BaseColor borderGray = new BaseColor(226, 232, 240);

        // Fonts
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 22, Font.BOLD, BaseColor.WHITE);
        Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 11, Font.NORMAL, BaseColor.WHITE);
        Font headingFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, darkBlue);
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL, new BaseColor(100, 116, 139));
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, darkBlue);
        Font amountFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, blue);

        // Header background
        PdfPTable headerTable = new PdfPTable(1);
        headerTable.setWidthPercentage(100);
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(darkBlue);
        headerCell.setPadding(24);
        headerCell.setBorder(Rectangle.NO_BORDER);

        Paragraph companyName = new Paragraph("Employee Management System", titleFont);
        companyName.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(companyName);

        String month = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
        Paragraph slipTitle = new Paragraph("SALARY SLIP — " + month.toUpperCase(), subtitleFont);
        slipTitle.setAlignment(Element.ALIGN_CENTER);
        headerCell.addElement(slipTitle);

        headerTable.addCell(headerCell);
        document.add(headerTable);

        document.add(Chunk.NEWLINE);

        // Employee Details Section
        Paragraph empDetailsTitle = new Paragraph("Employee Details", headingFont);
        empDetailsTitle.setSpacingAfter(8);
        document.add(empDetailsTitle);

        PdfPTable detailsTable = new PdfPTable(2);
        detailsTable.setWidthPercentage(100);
        detailsTable.setSpacingAfter(16);

        addDetailRow(detailsTable, "Employee Name", employee.getName(), labelFont, valueFont, lightGray, borderGray);
        addDetailRow(detailsTable, "Email Address", employee.getEmail(), labelFont, valueFont, BaseColor.WHITE, borderGray);
        addDetailRow(detailsTable, "Department", employee.getDepartment(), labelFont, valueFont, lightGray, borderGray);
        addDetailRow(detailsTable, "Pay Period", month, labelFont, valueFont, BaseColor.WHITE, borderGray);
        addDetailRow(detailsTable, "Pay Date", LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), labelFont, valueFont, lightGray, borderGray);

        document.add(detailsTable);

        // Salary Breakdown Section
        Paragraph salaryTitle = new Paragraph("Salary Breakdown", headingFont);
        salaryTitle.setSpacingAfter(8);
        document.add(salaryTitle);

        double grossSalary = employee.getSalary();
        double basicSalary = Math.round(grossSalary * 0.50);
        double hra = Math.round(grossSalary * 0.20);
        double transport = Math.round(grossSalary * 0.10);
        double medical = Math.round(grossSalary * 0.10);
        double other = Math.round(grossSalary * 0.10);
        double pf = Math.round(grossSalary * 0.12);
        double tax = Math.round(grossSalary * 0.05);
        double totalDeductions = pf + tax;
        double netSalary = grossSalary - totalDeductions;

        PdfPTable salaryTable = new PdfPTable(2);
        salaryTable.setWidthPercentage(100);
        salaryTable.setSpacingAfter(16);

        // Earnings column header
        PdfPCell earningsHeader = new PdfPCell(new Phrase("EARNINGS", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
        earningsHeader.setBackgroundColor(blue);
        earningsHeader.setPadding(8);
        earningsHeader.setBorderColor(borderGray);
        salaryTable.addCell(earningsHeader);

        PdfPCell deductionsHeader = new PdfPCell(new Phrase("DEDUCTIONS", new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD, BaseColor.WHITE)));
        deductionsHeader.setBackgroundColor(new BaseColor(239, 68, 68));
        deductionsHeader.setPadding(8);
        deductionsHeader.setBorderColor(borderGray);
        salaryTable.addCell(deductionsHeader);

        // Earnings rows
        PdfPCell earningsCell = new PdfPCell();
        earningsCell.setBorderColor(borderGray);
        earningsCell.setPadding(0);
        PdfPTable earningsInner = new PdfPTable(2);
        addSalaryRow(earningsInner, "Basic Salary", basicSalary, labelFont, valueFont, lightGray, borderGray);
        addSalaryRow(earningsInner, "HRA", hra, labelFont, valueFont, BaseColor.WHITE, borderGray);
        addSalaryRow(earningsInner, "Transport", transport, labelFont, valueFont, lightGray, borderGray);
        addSalaryRow(earningsInner, "Medical", medical, labelFont, valueFont, BaseColor.WHITE, borderGray);
        addSalaryRow(earningsInner, "Other", other, labelFont, valueFont, lightGray, borderGray);
        earningsCell.addElement(earningsInner);
        salaryTable.addCell(earningsCell);

        // Deductions rows
        PdfPCell deductionsCell = new PdfPCell();
        deductionsCell.setBorderColor(borderGray);
        deductionsCell.setPadding(0);
        PdfPTable deductionsInner = new PdfPTable(2);
        addSalaryRow(deductionsInner, "Provident Fund", pf, labelFont, valueFont, lightGray, borderGray);
        addSalaryRow(deductionsInner, "Income Tax", tax, labelFont, valueFont, BaseColor.WHITE, borderGray);
        deductionsCell.addElement(deductionsInner);
        salaryTable.addCell(deductionsCell);

        document.add(salaryTable);

        // Net Salary Box
        PdfPTable netTable = new PdfPTable(3);
        netTable.setWidthPercentage(100);
        netTable.setSpacingAfter(16);

        addSummaryCell(netTable, "Gross Salary", "₹" + String.format("%.0f", grossSalary), blue, labelFont, valueFont);
        addSummaryCell(netTable, "Total Deductions", "₹" + String.format("%.0f", totalDeductions), new BaseColor(239, 68, 68), labelFont, valueFont);
        addSummaryCell(netTable, "Net Salary", "₹" + String.format("%.0f", netSalary), new BaseColor(34, 197, 94), labelFont, amountFont);

        document.add(netTable);

        // Footer
        document.add(Chunk.NEWLINE);
        LineSeparator line = new LineSeparator();
        line.setLineColor(borderGray);
        document.add(new Chunk(line));
        document.add(Chunk.NEWLINE);

        Paragraph footer = new Paragraph("This is a system generated salary slip. No signature required.", labelFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        Paragraph generated = new Paragraph(
                "Generated on: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                labelFont
        );
        generated.setAlignment(Element.ALIGN_CENTER);
        document.add(generated);

        document.close();
        return baos.toByteArray();
    }

    private void addDetailRow(PdfPTable table, String label, String value,
                              Font labelFont, Font valueFont,
                              BaseColor bg, BaseColor border) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bg);
        labelCell.setPadding(8);
        labelCell.setBorderColor(border);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBackgroundColor(bg);
        valueCell.setPadding(8);
        valueCell.setBorderColor(border);
        table.addCell(valueCell);
    }

    private void addSalaryRow(PdfPTable table, String label, double amount,
                              Font labelFont, Font valueFont,
                              BaseColor bg, BaseColor border) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBackgroundColor(bg);
        labelCell.setPadding(7);
        labelCell.setBorderColor(border);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase("₹" + String.format("%.0f", amount), valueFont));
        valueCell.setBackgroundColor(bg);
        valueCell.setPadding(7);
        valueCell.setBorderColor(border);
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(valueCell);
    }

    private void addSummaryCell(PdfPTable table, String label, String value,
                                BaseColor color, Font labelFont, Font valueFont) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new BaseColor(
                Math.min(color.getRed() + 220, 255),
                Math.min(color.getGreen() + 220, 255),
                Math.min(color.getBlue() + 220, 255)
        ));
        cell.setPadding(12);
        cell.setBorderColor(color);
        cell.setBorderWidth(2);

        Paragraph labelPara = new Paragraph(label, labelFont);
        labelPara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(labelPara);

        Paragraph valuePara = new Paragraph(value, valueFont);
        valuePara.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(valuePara);

        table.addCell(cell);
    }
}