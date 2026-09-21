package com.ainjob.domain;

import javax.persistence.*;

@Entity
@Table(name = "status")
public class Status {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
}
