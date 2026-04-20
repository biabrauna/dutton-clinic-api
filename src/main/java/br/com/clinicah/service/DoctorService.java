package br.com.clinicah.service;

import br.com.clinicah.dto.DoctorRequest;
import br.com.clinicah.dto.DoctorResponse;
import br.com.clinicah.exception.ResourceNotFoundException;
import br.com.clinicah.model.Doctor;
import br.com.clinicah.repository.DoctorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DoctorService {

    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    public Page<DoctorResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(DoctorResponse::from);
    }

    public DoctorResponse findById(Integer id) {
        return repository.findById(id)
                .map(DoctorResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Médico", id));
    }

    public DoctorResponse create(DoctorRequest req) {
        Doctor doctor = new Doctor();
        applyFields(doctor, req);
        return DoctorResponse.from(repository.save(doctor));
    }

    public DoctorResponse update(Integer id, DoctorRequest req) {
        Doctor doctor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico", id));
        applyFields(doctor, req);
        return DoctorResponse.from(repository.save(doctor));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Médico", id);
        repository.deleteById(id);
    }

    private void applyFields(Doctor d, DoctorRequest req) {
        d.setName(req.getName());
        d.setEmail(req.getEmail());
        d.setSpecialty(req.getSpecialty());
        d.setCrm(req.getCrm());
    }
}
