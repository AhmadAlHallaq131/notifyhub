package com.ahmad.notifyhub.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false ,unique = true)
    private String email;

    @Column(name = "password_hash" ,nullable = false)
    private String passwordHash;

    @Column(name = "created_at" , nullable = false)
    private LocalDateTime createdAt;
}
