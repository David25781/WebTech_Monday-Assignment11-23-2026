package kigali.clinc.clinicmanagement.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity 
@Table(name = "specialization")
public class Specialization {

    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name="name")
    private String name;

    @ManyToMany(mappedBy = "specializations")
    @JsonIgnoreProperties("specializations")
    private List<Doctor> doctors = new ArrayList<>();

    public Specialization() {}

    public Specialization(UUID id, String name, List<Doctor> doctors) {
        this.id = id;
        this.name = name;
        this.doctors = doctors;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public List<Doctor> getDoctors() { return doctors; }
    public void setDoctors(List<Doctor> doctors) { this.doctors = doctors; }
}