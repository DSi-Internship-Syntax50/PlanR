package com.example.PlanR.service;

import com.example.PlanR.model.SeatAllocation;
import com.example.PlanR.model.SeatPlan;
import com.example.PlanR.repository.SeatPlanRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class SeatPlanPdfService {

    @Autowired
    private SeatPlanRepository seatPlanRepository;

    @Transactional(readOnly = true)
    public byte[] generatePdf(Long seatPlanId) {
        SeatPlan plan = seatPlanRepository.findById(seatPlanId)
                .orElseThrow(() -> new RuntimeException("Seat Plan not found"));

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // Use Landscape A4 for wider grids
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22);
            Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 14);
            Font cellBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font cellSmall = FontFactory.getFont(FontFactory.HELVETICA, 9);

            // Title
            Paragraph title = new Paragraph("PlanR - Official Exam Seat Plan", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            // Subtitle
            String roomName = (plan.getRoom() != null) ? plan.getRoom().getRoomNumber() : "Unassigned Room";
            String dateStr = plan.getGeneratedAt().format(DateTimeFormatter.ofPattern("MMM dd, yyyy - HH:mm"));
            Paragraph subtitle = new Paragraph("Venue: " + roomName + " | Generated: " + dateStr, subtitleFont);
            subtitle.setAlignment(Element.ALIGN_CENTER);
            subtitle.setSpacingAfter(25);
            document.add(subtitle);

            // Board / Invigilator Desk Visual
            PdfPTable deskTable = new PdfPTable(1);
            deskTable.setWidthPercentage(40);
            PdfPCell deskCell = new PdfPCell(
                    new Phrase("INVIGILATOR DESK", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10)));
            deskCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            deskCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            deskCell.setBackgroundColor(new java.awt.Color(230, 230, 230));
            deskCell.setPadding(10);
            deskTable.addCell(deskCell);
            document.add(deskTable);

            document.add(new Paragraph(" ")); // Spacer

            // Main Grid
            int cols = plan.getGridCols();
            PdfPTable table = new PdfPTable(cols);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            // Reconstruct Grid array for easy drawing
            String[][] grid = new String[plan.getGridRows()][cols];
            for (SeatAllocation alloc : plan.getAllocations()) {
                grid[alloc.getRowIndex()][alloc.getColIndex()] = alloc.getDepartmentCode();
            }

            for (int r = 0; r < plan.getGridRows(); r++) {
                for (int c = 0; c < cols; c++) {
                    String dept = grid[r][c];
                    PdfPCell cell = new PdfPCell();
                    cell.setMinimumHeight(45f);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(5);

                    if (dept != null && !dept.isEmpty()) {
                        cell.addElement(new Paragraph(dept, cellBold));
                        Paragraph seatLabel = new Paragraph("S" + (r + 1) + "-" + (c + 1), cellSmall);
                        seatLabel.setAlignment(Element.ALIGN_CENTER);
                        cell.addElement(seatLabel);
                    } else {
                        Paragraph emptyText = new Paragraph("Empty", cellSmall);
                        emptyText.setAlignment(Element.ALIGN_CENTER);
                        cell.addElement(emptyText);
                        cell.setBackgroundColor(new java.awt.Color(245, 245, 245));
                    }
                    table.addCell(cell);
                }
            }

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }
}