package kigali.clinc.clinicmanagement.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinc.clinicmanagement.domain.Specialization;
import kigali.clinc.clinicmanagement.repository.SpecializationRepository;

@Service
public class SpecializationService {

    @Autowired
    private SpecializationRepository specializationRepository;

    
    @Transactional
    public Specialization save(Specialization specialization) {
        return specializationRepository.save(specialization);
    }

    
    @Transactional(readOnly = true)
    public List<Specialization> findAll() {
        return specializationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Specialization> findById(UUID id) {
        return specializationRepository.findById(id);
    }

    @Transactional
    public Specialization update(UUID id, Specialization specializationDetails) {
        Specialization existingSpec = specializationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Specialization não encontrada com o ID: " + id));

        existingSpec.setName(specializationDetails.getName());
        
        if (specializationDetails.getDoctors() != null) {
            existingSpec.setDoctors(specializationDetails.getDoctors());
        }

        return specializationRepository.save(existingSpec);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!specializationRepository.existsById(id)) {
            throw new RuntimeException("Não é possível deletar. Specialization não encontrada com o ID: " + id);
        }
        specializationRepository.deleteById(id);

    }

    @Transactional(readOnly = true)
    public List<Specialization> findUnused() {
        return specializationRepository.findUnusedSpecializations();
    }
}
