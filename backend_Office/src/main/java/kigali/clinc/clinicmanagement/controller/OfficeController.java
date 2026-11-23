package kigali.clinc.clinicmanagement.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinc.clinicmanagement.domain.Office;
import kigali.clinc.clinicmanagement.service.OfficeService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping({"/api/office", "/api/offices"})
public class OfficeController {

    @Autowired
    private OfficeService officeService;

    // POST /api/office/save
    @PostMapping(value = "/save")
    public ResponseEntity<?> saveOffice(@RequestBody Office office) {
        try {
            Office savedOffice = officeService.create(office);
            return new ResponseEntity<>(savedOffice, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // PUT /api/office/update/{id}
    @PutMapping(value = "/update/{id}")
    public ResponseEntity<?> updateOffice(@PathVariable UUID id, @RequestBody Office office) {
        try {
            Office updatedOffice = officeService.update(id, office);
            return new ResponseEntity<>(updatedOffice, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    // GET /api/office/all
    @GetMapping(value = "/all")
    public ResponseEntity<List<Office>> getAllOffices() {
        return new ResponseEntity<>(officeService.findAll(), HttpStatus.OK);
    }

    // GET /api/office/{id}
    @GetMapping(value = "/{id}")
    public ResponseEntity<?> getOfficeById(@PathVariable UUID id) {
        return officeService.findById(id)
                .map(office -> new ResponseEntity<>(office, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // DELETE /api/office/delete/{id}
    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<?> deleteOffice(@PathVariable UUID id) {
        try {
            officeService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // GET /api/office/busiest
    @GetMapping("/busiest")
    public ResponseEntity<?> getBusiestOffice() {
        return officeService.findBusiestOffice()
                .<ResponseEntity<?>>map(result -> new ResponseEntity<>(result, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>("No appointments yet", HttpStatus.OK));
    }

    // GET /api/office/free
    @GetMapping("/free")
    public ResponseEntity<List<Office>> getFreeOffices() {
        return new ResponseEntity<>(officeService.findFreeOffices(), HttpStatus.OK);
    }
}