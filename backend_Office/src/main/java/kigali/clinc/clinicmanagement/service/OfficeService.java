package kigali.clinc.clinicmanagement.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinc.clinicmanagement.domain.Office;
import kigali.clinc.clinicmanagement.repository.OfficeRepository;

@Service
public class OfficeService {

    @Autowired
    private OfficeRepository officeRepository;

   
    @Transactional
    public Office create(Office office) {
        if (officeRepository.existsByOfficeNumber(office.getOfficeNumber())) {
            throw new IllegalArgumentException("Office is already registered with number: " + office.getOfficeNumber());
        }
        return officeRepository.save(office);
    }

    @Transactional
    public Office update(UUID id, Office officeDetails) {
        
        Office existingOffice = officeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Office não encontrado com o ID: " + id));

        
        if (existingOffice.getOfficeNumber() != officeDetails.getOfficeNumber() && 
            officeRepository.existsByOfficeNumber(officeDetails.getOfficeNumber())) {
            throw new IllegalArgumentException("Office is already registered with number: " + officeDetails.getOfficeNumber());
        }

       
        existingOffice.setName(officeDetails.getName());
        existingOffice.setOfficeNumber(officeDetails.getOfficeNumber());

        return officeRepository.save(existingOffice);
    }

  
    @Transactional(readOnly = true)
    public List<Office> findAll() {
        return officeRepository.findAll();
    }

    
    @Transactional(readOnly = true)
    public Optional<Office> findById(UUID id) {
        return officeRepository.findById(id);
    }

    
    @Transactional(readOnly = true)
    public Office findByOfficeNumber(int officeNumber) {
        return officeRepository.findByOfficeNumber(officeNumber);
    }

    // 6. Deletar (Delete)
    @Transactional
    public void deleteById(UUID id) {
        if (!officeRepository.existsById(id)) {
            throw new IllegalArgumentException("Não é possível deletar. Office não encontrado com o ID: " + id);
        }
        officeRepository.deleteById(id);

        
    }
    @Transactional(readOnly = true)
    public Optional<Map<String, Object>> findBusiestOffice() {
        List<Object[]> rows = officeRepository.findBusiestOffice(PageRequest.of(0, 1));
        if (rows.isEmpty()) {
            return Optional.empty();
        }
        Object[] top = rows.get(0);
        Map<String, Object> result = new HashMap<>();
        result.put("officeName", top[0]);
        result.put("officeNumber", top[1]);
        result.put("appointmentCount", top[2]);
        return Optional.of(result);
    }

        @Transactional(readOnly = true)
    public List<Office> findFreeOffices() {
        return officeRepository.findFreeOffices();
    }
}
