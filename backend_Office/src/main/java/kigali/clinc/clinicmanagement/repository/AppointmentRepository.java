package kigali.clinc.clinicmanagement.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kigali.clinc.clinicmanagement.domain.Appointment;
import kigali.clinc.clinicmanagement.domain.AppointmentStatus;
import kigali.clinc.clinicmanagement.domain.Doctor;
import kigali.clinc.clinicmanagement.domain.Patient;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByDoctor(Doctor doctor);

    List<Appointment> findByDoctorAndAppointmentDate(Doctor doctor, LocalDateTime appointmentDate);

    Boolean existsByDoctorAndAppointmentDate(Doctor doctor, LocalDateTime appointmentDate);

    boolean existsByDoctorAndAppointmentDateAndStatusNot(Doctor doctor, LocalDateTime appointmentDate, AppointmentStatus status);

    boolean existsByDoctorAndAppointmentDateBetweenAndStatusNotAndIdNot(
            Doctor doctor,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status,
            UUID id);

    List<Appointment> findByDoctorAndAppointmentDateBetweenAndStatusNotOrderByAppointmentDateAsc(
            Doctor doctor,
            LocalDateTime start,
            LocalDateTime end,
            AppointmentStatus status);

    List<Appointment> findByPatientAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
            Patient patient,
            LocalDateTime date,
            List<AppointmentStatus> statuses);

    long countByAppointmentDateBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(AppointmentStatus status);

    List<Appointment> findByStatusOrderByAppointmentDateAsc(AppointmentStatus status);

    List<Appointment> findByAppointmentDateBetweenOrderByAppointmentDateAsc(LocalDateTime startDate, LocalDateTime endDate);

    List<Appointment> findByDoctor_IdAndStatusOrderByAppointmentDateAsc(UUID doctorId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a WHERE a.status = :status AND a.appointmentDate < :currentDate")
    List<Appointment> findPastPendingAppointments(
        @Param("status") AppointmentStatus status,
        @Param("currentDate") LocalDateTime currentDate
    );

    @Query("SELECT a.doctor.id, a.doctor.firstName, a.doctor.lastName, COUNT(a) FROM Appointment a GROUP BY a.doctor.id, a.doctor.firstName, a.doctor.lastName ORDER BY COUNT(a) DESC")
    List<Object[]> countAppointmentsPerDoctorBusiestFirst();

    @Query("SELECT a FROM Appointment a JOIN a.doctor d JOIN d.specializations s WHERE s.id = :specializationId")
    List<Appointment> findByDoctorSpecializationId(@Param("specializationId") UUID specializationId);

    Page<Appointment> findByDoctor_Id(UUID doctorId, Pageable pageable);

    @Query("SELECT a.status, COUNT(a) FROM Appointment a GROUP BY a.status")
    List<Object[]> countAppointmentsByStatus();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Appointment a SET a.status = :newStatus " +
           "WHERE a.doctor.id = :doctorId " +
           "AND a.appointmentDate >= :start AND a.appointmentDate < :end " +
           "AND a.status <> :excluded")
    int cancelDayForDoctor(@Param("doctorId") UUID doctorId,
                           @Param("start") LocalDateTime start,
                           @Param("end") LocalDateTime end,
                           @Param("newStatus") AppointmentStatus newStatus,
                           @Param("excluded") AppointmentStatus excluded);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM Appointment a " +
           "WHERE a.status = :status AND a.appointmentDate < :before")
    int deleteCancelledBefore(@Param("status") AppointmentStatus status,
                              @Param("before") LocalDateTime before);
}