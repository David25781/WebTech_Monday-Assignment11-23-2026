package kigali.clinc.clinicmanagement.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinc.clinicmanagement.domain.Appointment;
import kigali.clinc.clinicmanagement.domain.Patient;
import kigali.clinc.clinicmanagement.service.PatientService;

@RestController
@RequestMapping({"/api/patient", "/api/patients"})
@CrossOrigin(origins = "*")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @PostMapping(value = "/save")
    public ResponseEntity<?> savePatient(@RequestBody Patient patient) {
        try {
            Patient savedPatient = patientService.create(patient);
            return new ResponseEntity<>(savedPatient, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<Patient>> getAllPatients() {
        List<Patient> patients = patientService.findAll();
        return new ResponseEntity<>(patients, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Patient> getPatientById(@PathVariable UUID id) {
        return patientService.findById(id)
                .map(patient -> new ResponseEntity<>(patient, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(value = "/name/{firstName}")
    public ResponseEntity<List<Patient>> getPatientsByFirstName(@PathVariable String firstName) {
        List<Patient> patients = patientService.findByFirstName(firstName);
        if (!patients.isEmpty()) {
            return new ResponseEntity<>(patients, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/by-last-name")
    public ResponseEntity<List<Patient>> getPatientsByLastName(@RequestParam String lastName) {
        List<Patient> patients = patientService.findByLastName(lastName);
        return ResponseEntity.ok(patients);
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updatePatient(@PathVariable UUID id, @RequestBody Patient patientDetails) {
        try {
            Patient updatedPatient = patientService.update(id, patientDetails);
            return new ResponseEntity<>(updatedPatient, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<HttpStatus> deletePatient(@PathVariable UUID id) {
        try {
            patientService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/of-doctor/{doctorId}")
    public ResponseEntity<?> getPatientsOfDoctor(@PathVariable UUID doctorId) {
        try {
            return new ResponseEntity<>(patientService.findByDoctorId(doctorId), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/frequent")
    public ResponseEntity<List<Patient>> getFrequentPatients(@RequestParam long min) {
        return new ResponseEntity<>(patientService.findFrequentPatients(min), HttpStatus.OK);
    }

    // D2: GET /api/patients/{ssNumber}/upcoming
    @GetMapping("/{ssNumber}/upcoming")
    public ResponseEntity<?> getUpcoming(@PathVariable String ssNumber) {
        Optional<List<Appointment>> result = patientService.findUpcomingBySsNumber(ssNumber);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("That patient with that ssnumber does not exist");
        }
        return ResponseEntity.status(HttpStatus.OK).body(result.get());
    }
}