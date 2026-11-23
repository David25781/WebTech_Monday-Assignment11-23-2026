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
import kigali.clinc.clinicmanagement.domain.Patient;
import kigali.clinc.clinicmanagement.repository.AppointmentRepository;
import kigali.clinc.clinicmanagement.repository.DoctorRepository;
import kigali.clinc.clinicmanagement.repository.PatientRepository;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public List<Patient> findByDoctorId(UUID doctorId) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new IllegalArgumentException("The doctor with that id does not exist");
        }
        return patientRepository.findPatientsByDoctorId(doctorId);
    }

    @Transactional
    public Patient create(Patient patient) {
        if (patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                patient.getFirstName(), patient.getLastName(), patient.getDateOfBirth())) {
            throw new IllegalArgumentException("Patient with this name and date of birth is already registered.");
        }
        return patientRepository.save(patient);
    }

    @Transactional
    public Patient update(UUID id, Patient patientDetails) {

        Patient existingPatient = patientRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Patient não encontrado com o ID: " + id));

        if ((!existingPatient.getFirstName().equals(patientDetails.getFirstName()) ||
             !existingPatient.getLastName().equals(patientDetails.getLastName()) ||
             !existingPatient.getDateOfBirth().equals(patientDetails.getDateOfBirth())) &&
            patientRepository.existsByFirstNameAndLastNameAndDateOfBirth(
                patientDetails.getFirstName(), patientDetails.getLastName(), patientDetails.getDateOfBirth())) {
            throw new IllegalArgumentException("Patient with this name and date of birth is already registered.");
        }

        existingPatient.setFirstName(patientDetails.getFirstName());
        existingPatient.setLastName(patientDetails.getLastName());
        existingPatient.setDateOfBirth(patientDetails.getDateOfBirth());
        if (patientDetails.getSsNumber() != null) {
            existingPatient.setSsNumber(patientDetails.getSsNumber());
        }
        return patientRepository.save(existingPatient);
    }

    @Transactional(readOnly = true)
    public List<Patient> findAll() {
        return patientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Patient> findById(UUID id) {
        return patientRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Patient> findByFirstName(String firstName) {
        return patientRepository.findByFirstName(firstName);
    }

    @Transactional
    public void deleteById(UUID id) {
        if (!patientRepository.existsById(id)) {
            throw new IllegalArgumentException("Não é possível deletar. Patient não encontrado com o ID: " + id);
        }
        patientRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Patient> findByLastName(String lastName) {
        return patientRepository.findByLastNameIgnoreCaseOrderByFirstNameAsc(lastName);
    }

    @Transactional(readOnly = true)
    public List<Patient> findFrequentPatients(long min) {
        return patientRepository.findFrequentPatients(min);
    }

    @Transactional(readOnly = true)
    public Optional<List<Appointment>> findUpcomingBySsNumber(String ssNumber) {
        return patientRepository.findBySsNumber(ssNumber)
                .map(patient -> appointmentRepository
                        .findByPatientAndAppointmentDateGreaterThanEqualAndStatusInOrderByAppointmentDateAsc(
                                patient,
                                LocalDate.now().atStartOfDay(),
                                List.of(AppointmentStatus.SCHEDULED, AppointmentStatus.CONFIRMED)));
    }
}