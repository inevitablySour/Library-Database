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

    public static void main(String[] args) throws SQLException {


//        ArrayList<ArrayList<String>> masterArrayList= studentImport("C:\\Users\\Joel\\Downloads\\Book Room Inventory.xlsx");
//        ArrayList<String> ninth= masterArrayList.get(0);
//        ArrayList<String> tenth= masterArrayList.get(1);
//        ArrayList<String> eleventh= masterArrayList.get(2);
//        ArrayList<String> twelfth= masterArrayList.get(3);
//
//        resetIncrement();
//
//        for (String student : ninth) {
//            Student tempStudent = new Student();
//            tempStudent.setName(student);
//            tempStudent.setGrade(9);
//            studentRepository.save(tempStudent);
//        }
//        for (String student : tenth) {
//            Student tempStudent = new Student();
//            tempStudent.setName(student);
//            tempStudent.setGrade(10);
//            studentRepository.save(tempStudent);
//        }
//        for (String student : eleventh) {
//            Student tempStudent = new Student();
//            tempStudent.setName(student);
//            tempStudent.setGrade(11);
//            studentRepository.save(tempStudent);
//        }
//        for (String student : twelfth) {
//            Student tempStudent = new Student();
//            tempStudent.setName(student);
//            tempStudent.setGrade(12);
//            studentRepository.save(tempStudent);
//        }
    }

    public static void resetIncrement() throws SQLException {

        String url = "jdbc:mysql://localhost:3306/javabase";
        String user = "root";
        String password = "Jxhb200516!";
        Connection myConn = DriverManager.getConnection(url, user, password);

        Statement myStmt = myConn.createStatement();
        String sql = "ALTER TABLE students AUTO_INCREMENT = 1";
        boolean rs = myStmt.execute(sql);
    }

    public static int getCatalogueId() throws SQLException{
        String URL = "jdbc:mysql://localhost:3306/javabase";
        String USERNAME = "root";
        String PASSWORD = "Jxhb200516!";
        int id=0;
        try {

            Connection con=DriverManager.getConnection(URL,USERNAME,PASSWORD);
            PreparedStatement ps=con.prepareStatement("insert into catalogue (name) values(?)",Statement.RETURN_GENERATED_KEYS);
            ps.setString(1,"Neeraj");
            ps.executeUpdate();
            ResultSet rs=ps.getGeneratedKeys();
            if(rs.next()){
                id=rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return id;
    }



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
