package features.step_definitions;

import io.cucumber.java.en.And;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Assert;
import org.openqa.selenium.JavascriptExecutor;
import runtime.Driver;
import runtime.Logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class UtilityStepdefs {

    public String className = getClass().getSimpleName();

    @And("^I verify the files are loaded from server in the networking tab for \"(.*?)\" screen and if not store the details in excel")
    public void verifyNetworkTabSize(String screenName) throws IOException {
        Logging.log("Class: " + className + "; Method: " + new Object() {
        }.getClass().getEnclosingMethod().getName() + " : Start");

        screenName = screenName.replace(" ", "");
        Boolean fileCreation = true;
        XSSFWorkbook workbook = null;
        XSSFSheet sheet = null;
        FileOutputStream out;
        String filePath = null;

        String scriptToExecute = "var performance = window.performance || window.mozPerformance || window.msPerformance || window.webkitPerformance || {}; var network = performance.getEntries() || {}; return network;";

        String networkingData = ((JavascriptExecutor) Driver.getDriver()).executeScript(scriptToExecute).toString();
        Logging.log(networkingData);

        Boolean filesLoadedFromCache = false;
        String[] sectionlog = networkingData.split("}");
        for (int iterator = 0; iterator < sectionlog.length; iterator++) {
            if (sectionlog[iterator].contains("initiatorType=")) {
                String fileSize = (sectionlog[iterator].split("transferSize=")[1]).split(",")[0];
                if (sectionlog[iterator].contains("&activity=PreCacheUserData") | fileSize.equals("0")) {
                    filesLoadedFromCache = true;
                    String nameSection = sectionlog[iterator].split("&activity=PreCacheUserData")[0];
                    String fileName = (nameSection.split("name=")[1]).split(",")[0];
                    String initiatorType = (sectionlog[iterator].split("initiatorType=")[1]).split(",")[0];
                    System.out.println("Loading from cache and Details are " +
                            "File name = " + fileName + " " +
                            "Transfer size = " + fileSize + " " +
                            "Initiator Type = " + initiatorType);

                    if (fileCreation) {
                        workbook = new XSSFWorkbook();
                        sheet = workbook.createSheet(screenName + "_Networking Log deatils");
                        Map<String, Object[]> data = new TreeMap<String, Object[]>();
                        data.put("1", new Object[]{"Page Name", "File Name", "Intiator Type", "Transfer Size"});

                        Set<String> keyset = data.keySet();
                        int rownum = 0;
                        for (String key : keyset) {
                            // this creates a new row in the sheet
                            Row row = sheet.createRow(rownum++);
                            Object[] objArr = data.get(key);
                            int cellnum = 0;
                            for (Object obj : objArr) {
                                // this line creates a cell in the next column of that row
                                Cell cell = row.createCell(cellnum++);
                                if (obj instanceof String)
                                    cell.setCellValue((String) obj);
                                else if (obj instanceof Integer)
                                    cell.setCellValue((Integer) obj);
                            }
                        }

                        //FileOutputStream out;
                        LocalDateTime systemDate = LocalDateTime.now();
                        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-ddHH-mm-ss");
                        String dateTime = dateFormatter.format(systemDate);
                        filePath = System.getProperty("user.dir") + "\\" + screenName + dateTime + "_NetWorkingLogs.xlsx";
                        try {
                            // this Writes the workbook
                            out = new FileOutputStream(new File(filePath));
                            workbook.write(out);
                            out.close();
                            System.out.println("NetWorkingLogs written successfully on disk.");
                        } catch (FileNotFoundException e) {
                            Logging.log("Failed to write to the excel file");
                        }
                        fileCreation = false;
                    }

                    XSSFRow row = sheet.getRow(0);
                    XSSFCell cell = null;
                    String value = null;
                    int rowNum = sheet.getLastRowNum();
                    int col_Num = -1;
                    for (int i = 0; i < row.getLastCellNum(); i++) {
                        if (row.getCell(i).getStringCellValue().trim().equals("Page Name")) {
                            col_Num = i;
                            sheet.autoSizeColumn(col_Num);
                            row = sheet.getRow(rowNum + 1);
                            if (row == null)
                                row = sheet.createRow(rowNum + 1);

                            cell = row.getCell(col_Num);
                            if (cell == null)
                                cell = row.createCell(col_Num);

                            cell.setCellValue(screenName);
                            row = sheet.getRow(0);
                        }
                        if (row.getCell(i).getStringCellValue().trim().equals("File Name")) {
                            col_Num = i;
                            sheet.autoSizeColumn(col_Num);
                            row = sheet.getRow(rowNum + 1);
                            if (row == null)
                                row = sheet.createRow(rowNum + 1);

                            cell = row.getCell(col_Num);
                            if (cell == null)
                                cell = row.createCell(col_Num);

                            cell.setCellValue(fileName);
                            row = sheet.getRow(0);
                        } else if (row.getCell(i).getStringCellValue().trim().equals("Intiator Type")) {
                            col_Num = i;
                            sheet.autoSizeColumn(col_Num);
                            row = sheet.getRow(rowNum + 1);
                            if (row == null)
                                row = sheet.createRow(rowNum + 1);

                            cell = row.getCell(col_Num);
                            if (cell == null)
                                cell = row.createCell(col_Num);

                            cell.setCellValue(initiatorType);
                            row = sheet.getRow(0);
                        } else if (row.getCell(i).getStringCellValue().trim().equals("Transfer Size")) {
                            col_Num = i;
                            sheet.autoSizeColumn(col_Num);
                            row = sheet.getRow(rowNum + 1);
                            if (row == null)
                                row = sheet.createRow(rowNum + 1);

                            cell = row.getCell(col_Num);
                            if (cell == null)
                                cell = row.createCell(col_Num);

                            cell.setCellValue(fileSize);
                        }
                    }
                    out = new FileOutputStream(filePath);
                    workbook.write(out);
                    out.close();
                }
            }
        }
        Assert.assertFalse("Files are loading from the cache", filesLoadedFromCache);
    }
}
