package org.example.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.example.model.agr_1251;
import org.example.model.ust04;
import org.example.repository.agr_1251Repo;
import org.example.repository.ust04Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Transactional
@Service
public class ExcelUploadService {

    @Autowired private agr_1251Repo agrRepository;
    @Autowired private ust04Repo ustRepository;

    private final DataFormatter dataFormatter = new DataFormatter();

    @Transactional
    public void uploadExcel(MultipartFile file, String type) throws Exception {
        IOUtils.setByteArrayMaxOverride(200_000_000);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 0;

            if ("AGR".equalsIgnoreCase(type)) {
                List<agr_1251> list = new ArrayList<>();
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Skip header


                    String firstCell = getCellValue(row, 0);
                    if (firstCell == null || firstCell.trim().isEmpty()) continue;

                    agr_1251 entity = agr_1251.builder()
                            .mandt(firstCell)
                            .agrName(getCellValue(row, 1))
                            .counter(parseInteger(getCellValue(row, 2)))
                            .object(getCellValue(row, 3))
                            .auth(getCellValue(row, 4))
                            .variant(getCellValue(row, 5)) // Indices must be exact!
                            .field(getCellValue(row, 6))
                            .low(getCellValue(row, 7))
                            .build();
                    list.add(entity);
                    rowCount++;
                }
                agrRepository.saveAll(list);
                System.out.println(">>> SUCCESSFULLY SAVED " + rowCount + " ROWS TO AGR_1251");

            } else if ("UST".equalsIgnoreCase(type)) {
                List<ust04> list = new ArrayList<>();
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue;

                    String firstCell = getCellValue(row, 0);
                    if (firstCell == null || firstCell.trim().isEmpty()) continue;

                    ust04 entity = ust04.builder()
                            .mandt(firstCell)
                            .bName(getCellValue(row, 1))
                            .profile(getCellValue(row, 2))
                            .build();
                    list.add(entity);
                    rowCount++;
                }
                ustRepository.saveAll(list);
                System.out.println(">>> SUCCESSFULLY SAVED " + rowCount + " ROWS TO UST04");
            }
        }
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        return dataFormatter.formatCellValue(cell);
    }

    private Integer parseInteger(String val) {
        try {
            return (val == null || val.isEmpty()) ? 0 : (int) Double.parseDouble(val.replace(",", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}