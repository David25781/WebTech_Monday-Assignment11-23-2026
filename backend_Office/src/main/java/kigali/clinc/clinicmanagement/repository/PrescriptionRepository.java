package kigali.clinc.clinicmanagement.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinc.clinicmanagement.domain.Appointment;
import kigali.clinc.clinicmanagement.domain.Prescription;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    List<Prescription> findByAppointment(Appointment appointment);

    boolean existsByAppointment(Appointment appointment);

    @Query("SELECT p FROM Prescription p " +
           "WHERE p.appointment.patient.ssNumber = :ssNumber " +
           "ORDER BY p.issuedDate DESC")
    List<Prescription> findPrescriptionHistoryBySsNumber(@Param("ssNumber") String ssNumber);

    @Query("SELECT p.medicineName, COUNT(p) " +
           "FROM Prescription p " +
           "GROUP BY p.medicineName " +
           "ORDER BY COUNT(p) DESC, p.medicineName ASC")
    List<Object[]> findTopMedicines();
}