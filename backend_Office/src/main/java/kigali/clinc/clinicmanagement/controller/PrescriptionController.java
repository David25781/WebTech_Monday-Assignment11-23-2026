package kigali.clinc.clinicmanagement.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinc.clinicmanagement.domain.Prescription;
import kigali.clinc.clinicmanagement.service.PrescriptionService;

@RestController
@RequestMapping("/api/prescriptions")
@CrossOrigin(origins = "*")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    // C1: POST /api/prescriptions/save?appointmentId=<appointmentId>

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestParam("appointmentId") UUID appointmentId,
                                       @RequestBody Prescription prescription) {
        String message = prescriptionService.save(appointmentId, prescription);
        if (message.equals("The appointment with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else if (message.equals("Prescriptions are only for COMPLETED appointments")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        }
    }

    // C2: GET /api/prescriptions/appointment/{appointmentId}
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<?> getByAppointment(@PathVariable UUID appointmentId) {
        Optional<List<Prescription>> result = prescriptionService.findByAppointmentId(appointmentId);
        if (result.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("The appointment with that id does not exist");
        }
        return ResponseEntity.status(HttpStatus.OK).body(result.get());
    }

    // C3: GET /api/prescriptions/patient/{ssNumber}
    @GetMapping("/patient/{ssNumber}")
    public ResponseEntity<List<Prescription>> getByPatient(@PathVariable String ssNumber) {
        return ResponseEntity.ok(prescriptionService.findByPatientSsNumber(ssNumber));
    }

    // C4: PUT /api/prescriptions/update/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<String> update(@PathVariable UUID id, @RequestBody Prescription prescription) {
        String message = prescriptionService.update(id, prescription);
        if (message.equals("The prescription with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    // C4: DELETE /api/prescriptions/delete/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> delete(@PathVariable UUID id) {
        String message = prescriptionService.delete(id);
        if (message.equals("The prescription with that id does not exist")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(message);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(message);
        }
    }

    //  GET /api/prescriptions/stats/top-medicines
    @GetMapping("/stats/top-medicines")
    public ResponseEntity<List<Object[]>> getTopMedicines() {
        return ResponseEntity.ok(prescriptionService.findTopMedicines());
    }
}