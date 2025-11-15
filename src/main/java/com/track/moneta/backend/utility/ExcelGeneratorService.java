package com.track.moneta.backend.utility;

import com.track.moneta.backend.dto.ExpenseResponseDTO;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ExcelGeneratorService {

    private static final int COLUMNS_PER_DAY = 3; // Description, Category, and Amount

    public void generateMonthlyExpenseExcel(List<ExpenseResponseDTO> expenses, int year, int month, OutputStream outputStream) throws IOException {
        Map<Integer, List<ExpenseResponseDTO>> expensesByDay = expenses.stream()
                .collect(Collectors.groupingBy(e -> e.getExpenseDate().getDayOfMonth()));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Expenses - " + year + "-" + month);

            // === 1. Create the Big "EXPENSES" Header ===
            int maxRowsOnDay = findMaxExpensesOnSingleDay(expensesByDay);
            int lastDataRow = 1 + maxRowsOnDay; // Row 1 is subheader, then data rows

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("E\nX\nP\nE\nN\nS\nE\nS");
            CellRangeAddress titleRegion = new CellRangeAddress(0, lastDataRow, 0, 0);
            sheet.addMergedRegion(titleRegion);
            CellStyle titleStyle = createTitleStyle(workbook);
            titleCell.setCellStyle(titleStyle);
            setRegionBorder(titleRegion, sheet, BorderStyle.MEDIUM);

            // === 2. Create the Day Headers (Day 1, Day 2, etc.) ===
            Row dayHeaderRow = sheet.createRow(0);
            for (int day = 1; day <= 30; day++) {
                int startCol = (day - 1) * COLUMNS_PER_DAY + 1;
                Cell cell = dayHeaderRow.createCell(startCol);
                cell.setCellValue("Day " + day);
                CellRangeAddress dayHeaderRegion = new CellRangeAddress(0, 0, startCol, startCol + COLUMNS_PER_DAY - 1);
                sheet.addMergedRegion(dayHeaderRegion);

                // Alternate colors for day headers
                CellStyle dayHeaderStyle = createDayHeaderStyle(workbook, day % 2 == 1);
                cell.setCellStyle(dayHeaderStyle);
            }

            // === 3. Create the "Description", "Category", and "Amount" Subheaders ===
            Row subHeaderRow = sheet.createRow(1);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            for (int day = 1; day <= 30; day++) {
                int startCol = (day - 1) * COLUMNS_PER_DAY + 1;
                Cell descCell = subHeaderRow.createCell(startCol);
                descCell.setCellValue("Description");
                descCell.setCellStyle(subHeaderStyle);

                Cell catCell = subHeaderRow.createCell(startCol + 1);
                catCell.setCellValue("Category");
                catCell.setCellStyle(subHeaderStyle);

                Cell amtCell = subHeaderRow.createCell(startCol + 2);
                amtCell.setCellValue("Amount");
                amtCell.setCellStyle(subHeaderStyle);
            }

            // === 4. Populate Expense Data ===
            CellStyle dataCellStyle = createDataCellStyle(workbook);
            CellStyle amountCellStyle = createAmountCellStyle(workbook);

            for (int day = 1; day <= 30; day++) {
                List<ExpenseResponseDTO> dayExpenses = expensesByDay.get(day);
                int startCol = (day - 1) * COLUMNS_PER_DAY + 1;

                if (dayExpenses != null) {
                    for (int i = 0; i < dayExpenses.size(); i++) {
                        Row dataRow = sheet.getRow(i + 2);
                        if (dataRow == null) dataRow = sheet.createRow(i + 2);

                        Cell descCell = dataRow.createCell(startCol);
                        descCell.setCellValue(dayExpenses.get(i).getDescription());
                        descCell.setCellStyle(dataCellStyle);

                        Cell catCell = dataRow.createCell(startCol + 1);
                        catCell.setCellValue(dayExpenses.get(i).getCategoryName());
                        catCell.setCellStyle(dataCellStyle);

                        BigDecimal amount = dayExpenses.get(i).getAmount();
                        Cell amountCell = dataRow.createCell(startCol + 2);
                        if (amount != null) amountCell.setCellValue(amount.doubleValue());
                        amountCell.setCellStyle(amountCellStyle);
                    }
                }

                // Fill empty cells for days with fewer expenses
                if (dayExpenses == null || dayExpenses.size() < maxRowsOnDay) {
                    int startRow = dayExpenses != null ? dayExpenses.size() + 2 : 2;
                    for (int i = startRow; i <= maxRowsOnDay + 1; i++) {
                        Row dataRow = sheet.getRow(i);
                        if (dataRow == null) dataRow = sheet.createRow(i);

                        Cell descCell = dataRow.createCell(startCol);
                        descCell.setCellStyle(dataCellStyle);

                        Cell catCell = dataRow.createCell(startCol + 1);
                        catCell.setCellStyle(dataCellStyle);

                        Cell amountCell = dataRow.createCell(startCol + 2);
                        amountCell.setCellStyle(amountCellStyle);
                    }
                }
            }

            // === 5. Draw Borders Around Each Day Block ===
            for (int day = 1; day <= 30; day++) {
                int startCol = (day - 1) * COLUMNS_PER_DAY + 1;
                CellRangeAddress dayBlockRegion = new CellRangeAddress(1, lastDataRow, startCol, startCol + COLUMNS_PER_DAY - 1);
                setRegionBorder(dayBlockRegion, sheet, BorderStyle.MEDIUM);
            }

            // === 6. Calculate and Write Totals ===
            int totalRowIndex = lastDataRow + 1;
            Row totalRow = sheet.createRow(totalRowIndex);
            totalRow.setHeightInPoints(20);

            CellStyle totalLabelStyle = createTotalLabelStyle(workbook);
            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("Total");
            totalLabelCell.setCellStyle(totalLabelStyle);

            BigDecimal monthTotal = BigDecimal.ZERO;
            CellStyle totalValueStyle = createTotalValueStyle(workbook);

            for (int day = 1; day <= 30; day++) {
                int amountColIndex = (day - 1) * COLUMNS_PER_DAY + 3;
                Cell dayTotalCell = totalRow.createCell(amountColIndex);
                dayTotalCell.setCellStyle(totalValueStyle);

                String colLetter = org.apache.poi.ss.util.CellReference.convertNumToColString(amountColIndex);
                if (maxRowsOnDay > 0) {
                    dayTotalCell.setCellFormula(String.format("SUM(%s3:%s%d)", colLetter, colLetter, 2 + maxRowsOnDay));
                } else {
                    dayTotalCell.setCellValue(0);
                }
                monthTotal = monthTotal.add(getTotalForDay(expensesByDay.get(day)));
            }

            // === 7. Write Month Total ===
            int monthTotalRowStartIndex = totalRowIndex + 1;
            Row monthTotalLabelRow = sheet.createRow(monthTotalRowStartIndex);
            monthTotalLabelRow.setHeightInPoints(20);

            Cell monthLabelCell = monthTotalLabelRow.createCell(0);
            monthLabelCell.setCellValue("MONTH TOTAL");
            monthLabelCell.setCellStyle(totalLabelStyle);

            Row monthTotalValueRow = sheet.createRow(monthTotalRowStartIndex + 1);
            monthTotalValueRow.setHeightInPoints(25);

            Cell grandTotalCell = monthTotalValueRow.createCell(0);
            grandTotalCell.setCellValue(monthTotal.doubleValue());
            CellStyle grandTotalStyle = createGrandTotalStyle(workbook);
            grandTotalCell.setCellStyle(grandTotalStyle);

            CellRangeAddress monthTotalRegion = new CellRangeAddress(monthTotalRowStartIndex, monthTotalRowStartIndex + 1, 0, 0);
            setRegionBorder(monthTotalRegion, sheet, BorderStyle.MEDIUM);

            // === 8. Set Column Widths ===
            sheet.setColumnWidth(0, 2500); // EXPENSES column
            for (int day = 1; day <= 30; day++) {
                int startCol = (day - 1) * COLUMNS_PER_DAY + 1;
                sheet.setColumnWidth(startCol, 5500); // Description column
                sheet.setColumnWidth(startCol + 1, 2500); // Amount column
            }

            workbook.write(outputStream);
        }
    }

    // --- Styling Helper Methods ---

    private void setRegionBorder(CellRangeAddress region, Sheet sheet, BorderStyle borderStyle) {
        RegionUtil.setBorderTop(borderStyle, region, sheet);
        RegionUtil.setBorderBottom(borderStyle, region, sheet);
        RegionUtil.setBorderLeft(borderStyle, region, sheet);
        RegionUtil.setBorderRight(borderStyle, region, sheet);
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createDayHeaderStyle(Workbook workbook, boolean isOdd) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        // Alternate colors
        if (isOdd) {
            style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        } else {
            style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        }
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createAmountCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }

    private CellStyle createTotalLabelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createTotalValueStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createGrandTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private int findMaxExpensesOnSingleDay(Map<Integer, List<ExpenseResponseDTO>> expensesByDay) {
        return expensesByDay.values().stream().mapToInt(List::size).max().orElse(0);
    }

    private BigDecimal getTotalForDay(List<ExpenseResponseDTO> expenses) {
        if (expenses == null) return BigDecimal.ZERO;
        return expenses.stream()
                .map(ExpenseResponseDTO::getAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}