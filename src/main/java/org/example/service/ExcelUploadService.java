package org.example.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;
import org.example.model.*;
import org.example.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ExcelUploadService {


    @Autowired
    private Ust04Repository ust04Repository;

    @Autowired
    private AgrProfRepository agrProfRepository;

    @Autowired
    private Ust10cRepository ust10cRepository;

    @Autowired
    private Ust10sRepository ust10sRepository;

    @Autowired
    private Ust12Repository ust12Repository;

    @Autowired
    private AgrDefineRepository agrDefineRepository;

    private final DataFormatter dataFormatter = new DataFormatter();

    @Transactional
    public void uploadExcel(MultipartFile file, String type) throws Exception {
        IOUtils.setByteArrayMaxOverride(200_000_000);

        try (InputStream is = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = 0;

            switch (type.toUpperCase()) {

                case "UST04":
                    rowCount = uploadUst04(sheet);
                    break;
                case "AGR_PROF":
                    rowCount = uploadAgrProf(sheet);
                    break;
                case "UST10C":
                    rowCount = uploadUst10c(sheet);
                    break;
                case "UST10S":
                    rowCount = uploadUst10s(sheet);
                    break;
                case "UST12":
                    rowCount = uploadUst12(sheet);
                    break;
                case "AGR_DEFINE":
                    rowCount = uploadAgrDefine(sheet);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown type: " + type);
            }

            System.out.println(">>> SUCCESSFULLY SAVED " + rowCount + " ROWS TO " + type.toUpperCase());
        }
    }



    private int uploadUst04(Sheet sheet) {
        List<ust04> list = new ArrayList<>();
        int rowCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

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

        ust04Repository.saveAll(list);
        return rowCount;
    }

    private int uploadAgrProf(Sheet sheet) {
        List<agr_prof> list = new ArrayList<>();
        int rowCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            String firstCell = getCellValue(row, 0);
            if (firstCell == null || firstCell.trim().isEmpty()) continue;

            agr_prof entity = agr_prof.builder()
                    .mandt(firstCell)
                    .roleName(getCellValue(row, 1))
                    .language(getCellValue(row, 2))
                    .profile(getCellValue(row, 3))
                    .text(getCellValue(row, 4))
                    .build();
            list.add(entity);
            rowCount++;
        }

        agrProfRepository.saveAll(list);
        return rowCount;
    }

    private int uploadUst10c(Sheet sheet) {
        List<ust10c> list = new ArrayList<>();
        int rowCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            String firstCell = getCellValue(row, 0);
            if (firstCell == null || firstCell.trim().isEmpty()) continue;

            ust10c entity = ust10c.builder()
                    .client(firstCell)
                    .compProfile(getCellValue(row, 1))
                    .version(getCellValue(row, 2))
                    .singProfile(getCellValue(row, 3))
                    .build();
            list.add(entity);
            rowCount++;
        }

        ust10cRepository.saveAll(list);
        return rowCount;
    }

    private int uploadUst10s(Sheet sheet) {
        List<ust10s> list = new ArrayList<>();
        int rowCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            String firstCell = getCellValue(row, 0);
            if (firstCell == null || firstCell.trim().isEmpty()) continue;

            ust10s entity = ust10s.builder()
                    .client(firstCell)
                    .profile(getCellValue(row, 1))
                    .version(getCellValue(row, 2))
                    .authObj(getCellValue(row, 3))
                    .usermasterMaint(getCellValue(row, 4))
                    .build();
            list.add(entity);
            rowCount++;
        }

        ust10sRepository.saveAll(list);
        return rowCount;
    }

    private int uploadUst12(Sheet sheet) {
        List<ust12> list = new ArrayList<>();
        int rowCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            String firstCell = getCellValue(row, 0);
            if (firstCell == null || firstCell.trim().isEmpty()) continue;

            ust12 entity = ust12.builder()
                    .client(firstCell)
                    .authObj(getCellValue(row, 1))
                    .usermasterMaint(getCellValue(row, 2))
                    .version(getCellValue(row, 3))
                    .authField(getCellValue(row, 4))
                    .low(getCellValue(row, 5))
                    .high(getCellValue(row, 6))
                    .build();
            list.add(entity);
            rowCount++;
        }

        ust12Repository.saveAll(list);
        return rowCount;
    }

    private int uploadAgrDefine(Sheet sheet) {
        List<agr_define> list = new ArrayList<>();
        int rowCount = 0;

        for (Row row : sheet) {
            if (row.getRowNum() == 0) continue; // Skip header

            String firstCell = getCellValue(row, 0);
            if (firstCell == null || firstCell.trim().isEmpty()) continue;

            agr_define entity = agr_define.builder()
                    .mandt(firstCell)
                    .roleName(getCellValue(row, 1))
                    .build();
            list.add(entity);
            rowCount++;
        }

        agrDefineRepository.saveAll(list);
        return rowCount;
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