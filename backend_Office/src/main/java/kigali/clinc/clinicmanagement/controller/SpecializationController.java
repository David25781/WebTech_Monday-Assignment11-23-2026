package kigali.clinc.clinicmanagement.controller;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import kigali.clinc.clinicmanagement.domain.Specialization;
import kigali.clinc.clinicmanagement.service.SpecializationService;

@RestController
@RequestMapping({"/api/specialization", "/api/specializations"})
@CrossOrigin(origins = "*")
public class SpecializationController {

    @Autowired
    private SpecializationService specializationService;

    @PostMapping(value = "/save")
    public ResponseEntity<Specialization> createSpecialization(@RequestBody Specialization specialization) {
        Specialization savedSpecialization = specializationService.save(specialization);
        return new ResponseEntity<>(savedSpecialization, HttpStatus.CREATED);
    }

    @GetMapping(value = "/all")
    public ResponseEntity<List<Specialization>> getAllSpecializations() {
        List<Specialization> specializations = specializationService.findAll();
        return new ResponseEntity<>(specializations, HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Specialization> getSpecializationById(@PathVariable UUID id) {
        return specializationService.findById(id)
                .map(specialization -> new ResponseEntity<>(specialization, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(value = "/update/{id}")
    public ResponseEntity<Specialization> updateSpecialization(@PathVariable UUID id, @RequestBody Specialization specializationDetails) {
        try {
            Specialization updatedSpecialization = specializationService.update(id, specializationDetails);
            return new ResponseEntity<>(updatedSpecialization, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

   
    @DeleteMapping(value = "/delete/{id}")
    public ResponseEntity<HttpStatus> deleteSpecialization(@PathVariable UUID id) {
        try {
            specializationService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

     @GetMapping(value = "/unused")
    public ResponseEntity<List<Specialization>> getUnusedSpecializations() {
        return new ResponseEntity<>(specializationService.findUnused(), HttpStatus.OK);
    }
}
