package com.webstore.entity;


import jakarta.persistence.*;

@Entity
@Table(name = "category", schema = "web_store")
public class Category {

    @Id
    @Column(name = "category_id")
    private Integer categoryId;

}
