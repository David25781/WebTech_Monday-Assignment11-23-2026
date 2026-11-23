package kigali.clinc.clinicmanagement.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinc.clinicmanagement.domain.Appointment;
import kigali.clinc.clinicmanagement.domain.Doctor;
import kigali.clinc.clinicmanagement.service.DoctorService;

@RestController
@RequestMapping("/api/doctors")
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<Doctor> createDoctor(@RequestBody Doctor doctor) {
        Doctor savedDoctor = doctorService.saveDoctor(doctor);
        return new ResponseEntity<>(savedDoctor, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable UUID id) {
        return doctorService.getDoctorById(id)
                .map(doctor -> new ResponseEntity<>(doctor, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> updateDoctor(@PathVariable UUID id, @RequestBody Doctor doctorDetails) {
        try {
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctorDetails);
            return new ResponseEntity<>(updatedDoctor, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deleteDoctor(@PathVariable UUID id) {
        try {
            doctorService.deleteDoctor(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/by-specialization")
    public List<Doctor> getDoctorsBySpecialization(@RequestParam String name) {
        return doctorService.getDoctorsBySpecialization(name);
    }

    @GetMapping("/without-office")
    public List<Doctor> getDoctorsWithoutOffice() {
        return doctorService.getDoctorsWithoutOffice();
    }

    // A1: PUT /api/doctors/{doctorId}/specializations/{specializationId}
    @PutMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> addSpecialization(@PathVariable UUID doctorId,
                                                    @PathVariable UUID specializationId) {
        String message = doctorService.addSpecialization(doctorId, specializationId);
        if (message.equals("The doctor with that id does not exist")
                || message.equals("That specialization does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("The doctor already has that specialization")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    // A2: DELETE /api/doctors/{doctorId}/specializations/{specializationId}
    @DeleteMapping("/{doctorId}/specializations/{specializationId}")
    public ResponseEntity<String> removeSpecialization(@PathVariable UUID doctorId,
                                                       @PathVariable UUID specializationId) {
        String message = doctorService.removeSpecialization(doctorId, specializationId);
        if (message.equals("The doctor with that id does not exist")
                || message.equals("That specialization does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("The doctor does not have that specialization")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    // A4: PATCH /api/doctors/{doctorId}/office?officeNumber=104
    @PatchMapping("/{doctorId}/office")
    public ResponseEntity<String> changeOffice(@PathVariable UUID doctorId,
                                               @RequestParam int officeNumber) {
        String message = doctorService.changeOffice(doctorId, officeNumber);
        if (message.equals("The doctor with that id does not exist")
                || message.equals("The office with that office number does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("That office is taken by other doctor")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    // D1: GET /api/doctors/{doctorId}/agenda?date=2026-10-20
    @GetMapping("/{doctorId}/agenda")
    public ResponseEntity<?> getAgenda(@PathVariable UUID doctorId,
                                       @RequestParam("date")
                                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Optional<List<Appointment>> result = doctorService.getAgenda(doctorId, date);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The doctor with that id does not exist");
        }
        return ResponseEntity.status(HttpStatus.OK).body(result.get());
    }

    // D3: GET /api/doctors/search?name=gis
    @GetMapping("/search")
    public ResponseEntity<List<Doctor>> searchDoctors(@RequestParam("name") String name) {
        return ResponseEntity.ok(doctorService.searchByName(name));
    }

    // : GET /api/doctors/workload?start=2026-10-01&end=2026-10-31
    @GetMapping("/workload")
    public ResponseEntity<List<Object[]>> getWorkload(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(doctorService.getWorkload(start, end));
    }
}