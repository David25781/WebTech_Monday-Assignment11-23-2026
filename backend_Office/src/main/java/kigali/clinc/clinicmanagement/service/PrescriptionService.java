package kigali.clinc.clinicmanagement.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinc.clinicmanagement.domain.Appointment;
import kigali.clinc.clinicmanagement.domain.AppointmentStatus;
import kigali.clinc.clinicmanagement.domain.Prescription;
import kigali.clinc.clinicmanagement.repository.AppointmentRepository;
import kigali.clinc.clinicmanagement.repository.PrescriptionRepository;

@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Transactional
    public String save(UUID appointmentId, Prescription prescription) {
        Optional<Appointment> opt = appointmentRepository.findById(appointmentId);
        if (opt.isEmpty()) {
            return "The appointment with that id does not exist";
        }
        Appointment appointment = opt.get();
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            return "Prescriptions are only for COMPLETED appointments";
        }
        prescription.setId(null);
        prescription.setIssuedDate(LocalDate.now());
        prescription.setAppointment(appointment);
        prescriptionRepository.save(prescription);
        return "Prescription saved successfully";
    }

    @Transactional(readOnly = true)
    public Optional<List<Prescription>> findByAppointmentId(UUID appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> prescriptionRepository.findByAppointment(appointment));
    }

    @Transactional(readOnly = true)
    public List<Prescription> findByPatientSsNumber(String ssNumber) {
        return prescriptionRepository.findPrescriptionHistoryBySsNumber(ssNumber);
    }

    @Transactional
    public String update(UUID id, Prescription details) {
        Optional<Prescription> opt = prescriptionRepository.findById(id);
        if (opt.isEmpty()) {
            return "The prescription with that id does not exist";
        }
        Prescription existing = opt.get();
        existing.setMedicineName(details.getMedicineName());
        existing.setDosage(details.getDosage());
        existing.setDurationDays(details.getDurationDays());
        prescriptionRepository.save(existing);
        return "Prescription updated successfully";
    }

    @Transactional
    public String delete(UUID id) {
        if (!prescriptionRepository.existsById(id)) {
            return "The prescription with that id does not exist";
        }
        prescriptionRepository.deleteById(id);
        return "Prescription deleted successfully";
    }

    @Transactional(readOnly = true)
    public List<Object[]> findTopMedicines() {
        return prescriptionRepository.findTopMedicines();
    }
}