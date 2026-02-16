package com.marina.demo.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "jobs")
public class JobEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @Column(name = "company_name")
    private String companyName;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private String location;
    private BigDecimal salary;

    // НОВО: Кој работодавач го објавил огласот
    @ManyToOne
    @JoinColumn(name = "employer_id")
    private User employer;

    public JobEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }
    public User getEmployer() { return employer; }
    public void setEmployer(User employer) { this.employer = employer; }
}