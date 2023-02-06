package com.isb.library.web.book.controller;


import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.io.FileInputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;




public class bookImport {

    public static ArrayList<ArrayList<String>> bookImport(String filepath) {
        //Storage for data from excel files
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> title = new ArrayList<>();
        ArrayList<String> firstName = new ArrayList<>();
        ArrayList<String> lastName = new ArrayList<>();
        ArrayList<String> genre = new ArrayList<>();
        ArrayList<String> bookNumber = new ArrayList<>();
        ArrayList<String> student = new ArrayList<>();
        ArrayList<String> teacher = new ArrayList<>();

        File inputFile = new File(filepath);

        try(FileInputStream in = new FileInputStream(inputFile)) {

            XSSFWorkbook importedFile = new XSSFWorkbook(in);

            XSSFSheet books = importedFile.getSheetAt(0);

            Iterator<Row> rowIterator = books.iterator();
            while (rowIterator.hasNext()){
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (row.getRowNum() == 1 || cell.getColumnIndex() < 5 || cell.getColumnIndex() == 6 || cell.getColumnIndex() == 7) {

                    }
                    else{

                        if(cell.getColumnIndex()==0 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            title.add(cell.getStringCellValue());
                        }
                        if(cell.getColumnIndex() == 1 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            lastName.add(cell.getStringCellValue());
                        }
                        if(cell.getColumnIndex() == 2 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            firstName.add(cell.getStringCellValue());
                        }
                        if(cell.getColumnIndex() == 3 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            genre.add(cell.getStringCellValue());
                        }
                        if(cell.getColumnIndex() == 4 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            bookNumber.add(cell.getStringCellValue());
                        }
                        if(cell.getColumnIndex() == 6 && cell.getRowIndex()>1){
                            if(cell.getStringCellValue() == ""){
                                student.add("");
                            }
                            else {
                                student.add(cell.getStringCellValue());
                            }
                        }
                        if(cell.getColumnIndex() == 7 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            if(cell.getStringCellValue() == ""){
                                teacher.add("");
                            }
                            else {
                                teacher.add(cell.getStringCellValue());
                            }
                        }
                    }
                }
            }


        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ArrayList<ArrayList<String>> masterArray = new ArrayList<>();
        masterArray.add(title);
        masterArray.add(lastName);
        masterArray.add(firstName);
        masterArray.add(genre);
        masterArray.add(bookNumber);
        masterArray.add(student);
        masterArray.add(teacher);

        return masterArray;

    }

    public static void main(String[] args) throws IOException {
        ArrayList<ArrayList<Object>> array = readExcelFile("\\C:\\Users\\Joel\\Downloads\\Book Room Inventory.xlsx");


        for(ArrayList<Object> data : array){
            writeToCSV(data, array.indexOf(data) + ".csv");
        }

    }


        public static ArrayList<ArrayList<Object>> readExcelFile(String fileName) {
            ArrayList<ArrayList<Object>> data = new ArrayList<>();
            try (FileInputStream inputStream = new FileInputStream(fileName)) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet firstSheet = workbook.getSheetAt(0);
                int numberOfColumns = firstSheet.getRow(0).getLastCellNum();
                for (int i = 0; i < numberOfColumns; i++) {
                    data.add(new ArrayList<>());
                }
                for (Row row : firstSheet) {
                    for (Cell cell : row) {
                        Object cellValue = null;
                        switch (cell.getCellType()) {
                            case NUMERIC:
                                cellValue = cell.getNumericCellValue();
                                break;
                            case STRING:
                                cellValue = cell.getStringCellValue();
                                break;
                            case BOOLEAN:
                                cellValue = cell.getBooleanCellValue();
                                break;
                            case FORMULA:
                                switch (cell.getCachedFormulaResultType()) {
                                    case NUMERIC:
                                        cellValue = cell.getNumericCellValue();
                                        break;
                                    case STRING:
                                        cellValue = cell.getStringCellValue();
                                        break;
                                    case BOOLEAN:
                                        cellValue = cell.getBooleanCellValue();
                                        break;
                                    default:
                                        cellValue = null;
                                        break;
                                }
                                break;
                            default:
                                cellValue = null;
                                break;
                        }
                        int columnIndex = cell.getColumnIndex();
                        if(cellValue != null) {
                            data.get(columnIndex).add(cellValue);
                        }
                        else if(columnIndex == 10){
                            return data;
                        }
                        else{
                            data.get(columnIndex).add(null);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }




    public static void writeToCSV(ArrayList<Object> data, String filePath) throws IOException {

        try {
            FileWriter writer = new FileWriter(filePath);

            for (Object object : data) {
                if(object != null) {
                    writer.append(object.toString());
                    writer.append(", ");
                }
                else{
                    writer.append("");
                    writer.append(", ");
                }
            }

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    }






