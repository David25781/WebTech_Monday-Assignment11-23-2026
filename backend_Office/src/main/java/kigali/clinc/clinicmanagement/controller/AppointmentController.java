package kigali.clinc.clinicmanagement.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinc.clinicmanagement.domain.Appointment;
import kigali.clinc.clinicmanagement.domain.AppointmentStatus;
import kigali.clinc.clinicmanagement.service.AppointmentService;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

   
    @PostMapping("/save")
    public ResponseEntity<String> create(@RequestBody Appointment appointment) {
        String message = appointmentService.create(appointment);
        if (message.equals("Appointment date cannot be in the past")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else if (message.equals("Doctor is already booked on that date")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        }
    }

    // B2: PATCH /api/appointments/{id}/status?status=CONFIRMED
    @PatchMapping("/{id}/status")
    public ResponseEntity<String> updateStatus(@PathVariable UUID id,
                                               @RequestParam("status") AppointmentStatus status) {
        String message = appointmentService.updateStatus(id, status);
        if (message.equals("The appointment with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.startsWith("Cannot change status")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    // B3: PATCH /api/appointments/{id}/reschedule?newDate=2026-10-30
    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<String> reschedule(@PathVariable UUID id,
                                             @RequestParam("newDate")
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newDate) {
        String message = appointmentService.reschedule(id, newDate);
        if (message.equals("The appointment with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.endsWith("cannot be rescheduled")
                || message.equals("Appointment date cannot be in the past")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else if (message.equals("Doctor is already booked on that date")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    @GetMapping
    public List<Appointment> getAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<Appointment>> getAppointmentsByStatus(@RequestParam("status") AppointmentStatus status) {
        List<Appointment> appointments = appointmentService.getAppointmentsByStatus(status);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/between")
    public ResponseEntity<List<Appointment>> getAppointmentsBetween(
            @RequestParam("start") String startDateStr,
            @RequestParam("end") String endDateStr) {

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        List<Appointment> appointments = appointmentService.findAppointmentsBetween(startDate, endDate);
        return ResponseEntity.ok(appointments);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Appointment> getById(@PathVariable UUID id) {
        return appointmentService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Appointment> update(@PathVariable UUID id, @RequestBody Appointment appointment) {
        try {
            return ResponseEntity.ok(appointmentService.update(id, appointment));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

           // C4: DELETE /api/appointments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = appointmentService.deleteById(id);
        if (message.equals("The appointment with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("That appointment has prescriptions, delete them first")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    @GetMapping("/doctor/{doctorId}/pending")
    public List<Appointment> getPendingByDoctor(@PathVariable UUID doctorId) {
        return appointmentService.findPendingByDoctorOrdered(doctorId);
    }

    @GetMapping("/past-pending")
    public List<Appointment> getPastPending() {
        return appointmentService.findPastPending();
    }

    @GetMapping("/doctor-stats")
    public List<Map<String, Object>> getDoctorStats() {
        return appointmentService.getBusiestDoctorsStats();
    }

    @GetMapping("/by-specialization/{specializationId}")
    public List<Appointment> getBySpecialization(@PathVariable UUID specializationId) {
        return appointmentService.findByDoctorSpecialization(specializationId);
    }

    @GetMapping("/doctor/{doctorId}/paged")
    public Page<Appointment> getDoctorAppointmentsPaged(
            @PathVariable UUID doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return appointmentService.findDoctorAppointmentsPaged(doctorId, page, size);
    }

    @GetMapping("/stats/by-status")
    public List<Object[]> getAppointmentStatusStats() {
        return appointmentService.countByStatus();
    }

    @PatchMapping("/cancel-day")
    public ResponseEntity<String> cancelDay(
            @RequestParam("doctorId") UUID doctorId,
            @RequestParam("date") String dateStr) {
        int cancelled = appointmentService.cancelDay(doctorId, LocalDate.parse(dateStr));
        return ResponseEntity.ok(cancelled + " appointments cancelled");
    }

    @GetMapping("/page")
    public Page<Appointment> getAppointmentsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "appointmentDate,desc") String sort) {

        String[] parts = sort.split(",");
        String property = parts[0];
        Sort.Direction direction = parts.length > 1
                ? Sort.Direction.fromString(parts[1])
                : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));
        return appointmentService.findAllPaged(pageable);
    }

    @DeleteMapping("/cancelled-before")
    public ResponseEntity<String> deleteCancelledBefore(@RequestParam("date") String dateStr) {
        int deleted = appointmentService.deleteCancelledBefore(LocalDate.parse(dateStr));
        return ResponseEntity.ok(deleted + " appointments deleted");
    }

    
}