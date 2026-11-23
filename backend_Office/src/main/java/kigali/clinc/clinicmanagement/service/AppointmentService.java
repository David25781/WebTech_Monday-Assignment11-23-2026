package kigali.clinc.clinicmanagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinc.clinicmanagement.domain.Appointment;
import kigali.clinc.clinicmanagement.domain.AppointmentStatus;
import kigali.clinc.clinicmanagement.repository.AppointmentRepository;
import kigali.clinc.clinicmanagement.repository.PrescriptionRepository;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Transactional
    public String create(Appointment appointment) {
        if (appointment.getAppointmentDate().toLocalDate().isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        boolean isBooked = appointmentRepository.existsByDoctorAndAppointmentDateAndStatusNot(
                appointment.getDoctor(),
                appointment.getAppointmentDate(),
                AppointmentStatus.CANCELLED
        );
        if (isBooked) {
            return "Doctor is already booked on that date";
        }
        appointment.setId(null);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);
        return "Appointment saved successfully";
    }

    @Transactional
    public Appointment update(UUID id, Appointment appointmentDetails) {
        Appointment existing = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + id));
        existing.setAppointmentDate(appointmentDetails.getAppointmentDate());
        existing.setReason(appointmentDetails.getReason());
        existing.setDoctor(appointmentDetails.getDoctor());
        existing.setPatient(appointmentDetails.getPatient());
        return appointmentRepository.save(existing);
    }

    @Transactional
    public String updateStatus(UUID id, AppointmentStatus newStatus) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            return "The appointment with that id does not exist";
        }
        Appointment appointment = opt.get();
        AppointmentStatus current = appointment.getStatus();
        boolean allowed = false;
        if (current == AppointmentStatus.SCHEDULED) {
            allowed = newStatus == AppointmentStatus.CONFIRMED
                    || newStatus == AppointmentStatus.CANCELLED;
        } else if (current == AppointmentStatus.CONFIRMED) {
            allowed = newStatus == AppointmentStatus.COMPLETED
                    || newStatus == AppointmentStatus.CANCELLED;
        }
        if (!allowed) {
            return "Cannot change status from " + current + " to " + newStatus;
        }
        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);
        return "Appointment status updated successfully";
    }

    @Transactional
    public String reschedule(UUID id, LocalDate newDate) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            return "The appointment with that id does not exist";
        }
        Appointment appointment = opt.get();
        if (appointment.getStatus() == AppointmentStatus.COMPLETED
                || appointment.getStatus() == AppointmentStatus.CANCELLED) {
            return "A " + appointment.getStatus() + " appointment cannot be rescheduled";
        }
        if (newDate.isBefore(LocalDate.now())) {
            return "Appointment date cannot be in the past";
        }
        LocalDateTime start = newDate.atStartOfDay();
        LocalDateTime end = newDate.atTime(LocalTime.MAX);
        boolean booked = appointmentRepository.existsByDoctorAndAppointmentDateBetweenAndStatusNotAndIdNot(
                appointment.getDoctor(), start, end, AppointmentStatus.CANCELLED, id);
        if (booked) {
            return "Doctor is already booked on that date";
        }
        appointment.setAppointmentDate(newDate.atTime(appointment.getAppointmentDate().toLocalTime()));
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointmentRepository.save(appointment);
        return "Appointment rescheduled successfully";
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAll() {
        return appointmentRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> findById(UUID id) {
        return appointmentRepository.findById(id);
    }

    @Transactional
    public String deleteById(UUID id) {
        Optional<Appointment> opt = appointmentRepository.findById(id);
        if (opt.isEmpty()) {
            return "The appointment with that id does not exist";
        }
        if (prescriptionRepository.existsByAppointment(opt.get())) {
            return "That appointment has prescriptions, delete them first";
        }
        appointmentRepository.deleteById(id);
        return "Appointment deleted successfully";
    }

    @Transactional(readOnly = true)
    public List<Appointment> findPendingByDoctorOrdered(UUID doctorId) {
        return appointmentRepository.findByDoctor_IdAndStatusOrderByAppointmentDateAsc(
                doctorId, AppointmentStatus.PENDING);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findPastPending() {
        LocalDateTime today = LocalDateTime.now();
        return appointmentRepository.findPastPendingAppointments(AppointmentStatus.PENDING, today);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatusOrderByAppointmentDateAsc(status);
    }

    @Transactional(readOnly = true)
    public List<Appointment> findAppointmentsBetween(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return appointmentRepository.findByAppointmentDateBetweenOrderByAppointmentDateAsc(startDateTime, endDateTime);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getBusiestDoctorsStats() {
        List<Object[]> results = appointmentRepository.countAppointmentsPerDoctorBusiestFirst();
        return results.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("doctorId", record[0]);
            map.put("doctorName", record[1] + " " + record[2]);
            map.put("appointmentCount", record[3]);
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Appointment> findByDoctorSpecialization(UUID specializationId) {
        return appointmentRepository.findByDoctorSpecializationId(specializationId);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findDoctorAppointmentsPaged(UUID doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentDate").ascending());
        return appointmentRepository.findByDoctor_Id(doctorId, pageable);
    }

    @Transactional(readOnly = true)
    public List<Object[]> countByStatus() {
        return appointmentRepository.countAppointmentsByStatus();
    }

    @Transactional
    public int cancelDay(UUID doctorId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        return appointmentRepository.cancelDayForDoctor(
                doctorId, start, end, AppointmentStatus.CANCELLED, AppointmentStatus.COMPLETED);
    }

    @Transactional(readOnly = true)
    public Page<Appointment> findAllPaged(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    @Transactional
    public int deleteCancelledBefore(LocalDate date) {
        return appointmentRepository.deleteCancelledBefore(
                AppointmentStatus.CANCELLED, date.atStartOfDay());
    }
}