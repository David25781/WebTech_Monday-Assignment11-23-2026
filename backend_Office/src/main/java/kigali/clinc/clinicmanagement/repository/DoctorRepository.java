package kigali.clinc.clinicmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinc.clinicmanagement.domain.AppointmentStatus;
import kigali.clinc.clinicmanagement.domain.Doctor;
import kigali.clinc.clinicmanagement.domain.Office;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    @Query("SELECT DISTINCT d FROM Doctor d JOIN d.specializations s " +
           "WHERE LOWER(s.name) = LOWER(:name)")
    List<Doctor> findBySpecializationName(@Param("name") String name);

    @Query("SELECT d FROM Doctor d WHERE d.office IS NULL ORDER BY d.lastName ASC")
    List<Doctor> findDoctorsWithoutOffice();

    boolean existsByOfficeAndIdNot(Office office, UUID id);

    @Query("SELECT d FROM Doctor d " +
           "WHERE LOWER(d.firstName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "OR LOWER(d.lastName) LIKE LOWER(CONCAT('%', :name, '%')) " +
           "ORDER BY d.lastName ASC")
    List<Doctor> searchByName(@Param("name") String name);

    @Query("SELECT d.firstName, d.lastName, COUNT(a) " +
           "FROM Doctor d JOIN d.appointments a " +
           "WHERE a.status = :status " +
           "AND a.appointmentDate BETWEEN :start AND :end " +
           "GROUP BY d.id, d.firstName, d.lastName " +
           "ORDER BY COUNT(a) DESC, d.lastName ASC")
    List<Object[]> findDoctorWorkload(@Param("status") AppointmentStatus status,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end);
}