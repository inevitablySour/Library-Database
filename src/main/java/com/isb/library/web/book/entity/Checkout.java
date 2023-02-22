package com.isb.library.web.book.entity;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class Checkout {

    private Book book;
    private Student student;

    private List<Student> students;

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
        this.books = books;
    }

    private List<Book> books;

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public Book getBook() {
        return book;
    }

    public Student getStudent() {
        return student;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public void setStudent(Student student) {
        this.student = student;
    }
}
