package com.isb.library.web.book.entity;

import com.isb.library.web.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
public class Checkout {

    private Book book;
    private Student student;

    private Catalogue catalogue;

    public Catalogue getCatalogue() {
        return catalogue;
    }

    public void setCatalogue(Catalogue catalogue) {
        this.catalogue = catalogue;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    private Genre genre;

    private List<Genre> genres;

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    private List<User> users;
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
