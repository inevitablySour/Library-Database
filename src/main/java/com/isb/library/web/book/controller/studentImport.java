package com.isb.library.web.book.controller;

import com.isb.library.web.book.dao.StudentRepository;
import com.isb.library.web.book.entity.Student;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class studentImport {

    @Autowired
    StudentRepository studentRepository;

    /**
     * Resets the increment of the student list
     * @throws SQLException
     */
    public static void resetIncrement() throws SQLException {

        String url = "jdbc:mysql://localhost:3306/javabase";
        String user = "root";
        String password = "Jxhb200516!";
        Connection myConn = DriverManager.getConnection(url, user, password);

        Statement myStmt = myConn.createStatement();
        String sql = "ALTER TABLE students AUTO_INCREMENT = 1";
        boolean rs = myStmt.execute(sql);
    }

    /**
     * Method to import a list of students
     * @param file file to be used
     * @return Returns an Arraylist of one set of parallel arrays that contains the Student name and their corresponding
     * grade
     */
    public static ArrayList<ArrayList<String>> studentImport(MultipartFile file) {

        // Storage for data from excel files
        ArrayList<String> headers = new ArrayList<>();
        ArrayList<String> ninth = new ArrayList<>();
        ArrayList<String> tenth = new ArrayList<>();
        ArrayList<String> eleventh = new ArrayList<>();
        ArrayList<String> twelfth = new ArrayList<>();

        try (InputStream inputStream = file.getInputStream()) {

            XSSFWorkbook importedFile = new XSSFWorkbook(inputStream);
            XSSFSheet names = importedFile.getSheetAt(0);

            Iterator<Row> rowIterator = names.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    if (row.getRowNum() == 0) {
                        headers.add(cell.getStringCellValue());
                    } else {

                        if (cell.getColumnIndex() == 0 && cell.getStringCellValue() != "" && cell.getRowIndex() > 0) {
                            ninth.add(cell.getStringCellValue());
                        }
                        if (cell.getColumnIndex() == 1 && cell.getStringCellValue() != "" && cell.getRowIndex() > 0) {
                            tenth.add(cell.getStringCellValue());
                        }
                        if (cell.getColumnIndex() == 2 && cell.getStringCellValue() != "" && cell.getRowIndex() > 0) {
                            eleventh.add(cell.getStringCellValue());
                        }
                        if (cell.getColumnIndex() == 3 && cell.getStringCellValue() != "" && cell.getRowIndex() > 0) {
                            twelfth.add(cell.getStringCellValue());
                        }
                    }
                }
            }

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
