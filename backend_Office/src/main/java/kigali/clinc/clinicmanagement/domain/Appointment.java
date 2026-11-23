package kigali.clinc.clinicmanagement.domain;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "appointment")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "appointment_date")
    private LocalDateTime appointmentDate;

    @Column(name = "reason")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctor_id")
    @JsonIgnoreProperties("appointments")
    private Doctor doctor;

    public Appointment() {}

    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public LocalDateTime getAppointmentDate() { return appointmentDate; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public Patient getPatient() { return patient; }
    public void setPatient(Patient patient) { this.patient = patient; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public void setDoctorId(String doctorId) {
        if (doctorId != null && !doctorId.isEmpty()) {
            Doctor doc = new Doctor();
            doc.setId(UUID.fromString(doctorId));
            this.doctor = doc;
        }
    }

    
    public void setPatientId(String patientId) {
        if (patientId != null && !patientId.isEmpty()) {
            Patient pat = new Patient();
            pat.setId(UUID.fromString(patientId));
            this.patient = pat;
        }
    }
}