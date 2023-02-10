package com.isb.library.web.book.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="catalogue")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Catalogue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String name;

    private int quantity;

    private String lastName;

    private String firstName;

    private String ISBN;

    private String publisher;

    private String language;

    private String date;

    private String genre;

}
