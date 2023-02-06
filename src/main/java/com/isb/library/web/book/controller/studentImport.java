package com.isb.library.web.book.controller;

import com.isb.library.web.book.dao.StudentRepository;
import com.isb.library.web.book.entity.Student;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class studentImport {

    public static void resetIncrement() throws SQLException {

        String url = "jdbc:mysql://localhost:3306/javabase";
        String user = "root";
        String password = "Jxhb200516!";
        Connection myConn = DriverManager.getConnection(url, user, password);

        Statement myStmt = myConn.createStatement();
        String sql = "ALTER TABLE students AUTO_INCREMENT = 1";
        boolean rs = myStmt.execute(sql);
    }


    public static ArrayList<ArrayList<String>> studentImport(String filepath) {

        //Storage for data from excel files
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> ninth = new ArrayList<>();
        ArrayList<String> tenth = new ArrayList<>();
        ArrayList<String> eleventh = new ArrayList<>();
        ArrayList<String> twelfth = new ArrayList<>();

        File inputFile = new File(filepath);

        try(FileInputStream in = new FileInputStream(inputFile)) {

            XSSFWorkbook importedFile = new XSSFWorkbook(in);
            XSSFSheet names = importedFile.getSheetAt(2);

            Iterator<Row> rowIterator = names.iterator();
            while (rowIterator.hasNext()){
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while(cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (row.getRowNum() == 1) {
                        headers.add(cell.getStringCellValue());
                    }
                    else{

                        if(cell.getColumnIndex()==0 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            ninth.add(cell.getStringCellValue());
                        }
                        if(cell.getColumnIndex() == 1 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            tenth.add(cell.getStringCellValue());
                        }
                        if(cell.getColumnIndex() == 2 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            eleventh.add(cell.getStringCellValue());
                        }
                        if(cell.getColumnIndex() == 3 && cell.getStringCellValue() != "" && cell.getRowIndex()>1){
                            twelfth.add(cell.getStringCellValue());
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
        masterArray.add(ninth);
        masterArray.add(tenth);
        masterArray.add(eleventh);
        masterArray.add(twelfth);

        return masterArray;

    }

}
