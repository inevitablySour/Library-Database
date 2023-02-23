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

    private int quantity_available;

    private String lastName;

    private String firstName;

    private String original_language;

    private String century;

    private String genre;

    private String version;

    private boolean PRL;

    private String shelf;

}
