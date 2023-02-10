package com.isb.library.web.book.controller;


import com.isb.library.web.book.dao.BookRepository;
import com.isb.library.web.book.entity.Book;
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

    private BookRepository bookRepository;
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
        ArrayList<String> array = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Genre.xlsx");
        for(String e : array){
            System.out.println(e);
        }
        System.out.println(array.size());



    }

    public void saveData(){
        ArrayList<String> data = extractData("C:\\Users\\Joel\\OneDrive - International School of Beijing\\Desktop\\Title.xlsx");
        for(int i = 0; i< data.size(); i++){
            Book temp = new Book();
            temp.setName(data.get(i));
            bookRepository.save(temp);
        }
    }


    public static ArrayList<String> extractData(String filePath) {
        ArrayList<String> data = new ArrayList<>();
        try {
            FileInputStream excelFile = new FileInputStream(new File(filePath));
            Workbook workbook = new XSSFWorkbook(excelFile);
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip the first 2 rows
            for (int i = 0; i < 2; i++) {
                if (rowIterator.hasNext()) {
                    rowIterator.next();
                }
            }

            while (rowIterator.hasNext()) {
                Row currentRow = rowIterator.next();
                Cell cell = currentRow.getCell(0);
                if (cell == null || cell.toString().isEmpty()) {
                    continue;
                }
                switch (cell.getCellType()) {
                    case NUMERIC:
                        data.add(Double.toString(cell.getNumericCellValue()));
                        break;
                    case STRING:
                        data.add(cell.getStringCellValue());
                        break;
                }
            }
            workbook.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return data;
    }


}






