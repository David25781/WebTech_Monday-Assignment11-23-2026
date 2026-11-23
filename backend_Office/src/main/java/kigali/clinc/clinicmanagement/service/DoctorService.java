package kigali.clinc.clinicmanagement.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kigali.clinc.clinicmanagement.domain.Appointment;
import kigali.clinc.clinicmanagement.domain.AppointmentStatus;
import kigali.clinc.clinicmanagement.domain.Doctor;
import kigali.clinc.clinicmanagement.domain.Office;
import kigali.clinc.clinicmanagement.domain.Specialization;
import kigali.clinc.clinicmanagement.repository.AppointmentRepository;
import kigali.clinc.clinicmanagement.repository.DoctorRepository;
import kigali.clinc.clinicmanagement.repository.OfficeRepository;
import kigali.clinc.clinicmanagement.repository.SpecializationRepository;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private SpecializationRepository specializationRepository;

    @Autowired
    private OfficeRepository officeRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Transactional
    public Doctor saveDoctor(Doctor doctor) {

        List<Specialization> incomingSpecs = doctor.getSpecializations();
        doctor.setSpecializations(new ArrayList<>());

        Doctor savedDoctor = doctorRepository.save(doctor);

        if (incomingSpecs != null && !incomingSpecs.isEmpty()) {
            List<Specialization> managedSpecs = new ArrayList<>();
            for (Specialization spec : incomingSpecs) {
                if (spec.getId() != null) {
                    specializationRepository.findById(spec.getId()).ifPresent(existingSpec -> {
                        if (!existingSpec.getDoctors().contains(savedDoctor)) {
                            existingSpec.getDoctors().add(savedDoctor);
                        }
                        specializationRepository.save(existingSpec);
                        managedSpecs.add(existingSpec);
                    });
                }
            }
            savedDoctor.setSpecializations(managedSpecs);
        }

        return savedDoctor;
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorById(UUID id) {
        return doctorRepository.findById(id);
    }

    @Transactional
    public Doctor updateDoctor(UUID id, Doctor doctorDetails) {
        Doctor existingDoctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor não encontrado com o ID: " + id));
        existingDoctor.setFirstName(doctorDetails.getFirstName());
        existingDoctor.setLastName(doctorDetails.getLastName());
        existingDoctor.setDateOfBirth(doctorDetails.getDateOfBirth());
        existingDoctor.setOffice(doctorDetails.getOffice());
        return doctorRepository.save(existingDoctor);
    }

    @Transactional
    public void deleteDoctor(UUID id) {
        if (!doctorRepository.existsById(id)) {
            throw new RuntimeException("Não é possível deletar. Doctor não encontrado com o ID: " + id);
        }
        doctorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsBySpecialization(String name) {
        return doctorRepository.findBySpecializationName(name);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsWithoutOffice() {
        return doctorRepository.findDoctorsWithoutOffice();
    }

    @Transactional
    public String addSpecialization(UUID doctorId, UUID specializationId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return "The doctor with that id does not exist";
        }
        Optional<Specialization> specOpt = specializationRepository.findById(specializationId);
        if (specOpt.isEmpty()) {
            return "That specialization does not exist";
        }
        Doctor doctor = doctorOpt.get();
        Specialization specialization = specOpt.get();
        if (doctor.getSpecializations().contains(specialization)) {
            return "The doctor already has that specialization";
        }
        doctor.getSpecializations().add(specialization);
        doctorRepository.save(doctor);
        return "Specialization added to the doctor";
    }

    @Transactional
    public String removeSpecialization(UUID doctorId, UUID specializationId) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return "The doctor with that id does not exist";
        }
        Optional<Specialization> specOpt = specializationRepository.findById(specializationId);
        if (specOpt.isEmpty()) {
            return "That specialization does not exist";
        }
        Doctor doctor = doctorOpt.get();
        Specialization specialization = specOpt.get();
        if (!doctor.getSpecializations().contains(specialization)) {
            return "The doctor does not have that specialization";
        }
        doctor.getSpecializations().remove(specialization);
        doctorRepository.save(doctor);
        return "Specialization removed from the doctor";
    }

    @Transactional
    public String changeOffice(UUID doctorId, int officeNumber) {
        Optional<Doctor> doctorOpt = doctorRepository.findById(doctorId);
        if (doctorOpt.isEmpty()) {
            return "The doctor with that id does not exist";
        }
        Office office = officeRepository.findByOfficeNumber(officeNumber);
        if (office == null) {
            return "The office with that office number does not exist";
        }
        Doctor doctor = doctorOpt.get();
        if (doctorRepository.existsByOfficeAndIdNot(office, doctorId)) {
            return "That office is taken by other doctor";
        }
        doctor.setOffice(office);
        doctorRepository.save(doctor);
        return "Office changed successfully";
    }

    @Transactional(readOnly = true)
    public Optional<List<Appointment>> getAgenda(UUID doctorId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return doctorRepository.findById(doctorId)
                .map(doctor -> appointmentRepository
                        .findByDoctorAndAppointmentDateBetweenAndStatusNotOrderByAppointmentDateAsc(
                                doctor, start, end, AppointmentStatus.CANCELLED));
    }

    @Transactional(readOnly = true)
    public List<Doctor> searchByName(String name) {
        return doctorRepository.searchByName(name);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getWorkload(LocalDate start, LocalDate end) {
        return doctorRepository.findDoctorWorkload(
                AppointmentStatus.COMPLETED,
                start.atStartOfDay(),
                end.atTime(LocalTime.MAX));
    }
}