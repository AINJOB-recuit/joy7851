package com.ainjob.domain;

import javax.persistence.*;

@Entity
@Table(name = "applicant")
public class Applicant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
