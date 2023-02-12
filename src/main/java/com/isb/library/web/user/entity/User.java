package com.isb.library.web.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@Table(name="users")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    //name email age address password createTime delete
    private String id;
    private String name;

    private String username;
    private String password;

    // 1 : management 2 , putong user
    private String type;

}
