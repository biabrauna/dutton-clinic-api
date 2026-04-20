package br.com.clinicah.service;

import br.com.clinicah.dto.PatientRequest;
import br.com.clinicah.dto.PatientResponse;
import br.com.clinicah.exception.ResourceNotFoundException;
import br.com.clinicah.model.Patient;
import br.com.clinicah.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public Page<PatientResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(PatientResponse::from);
    }

    public PatientResponse findById(Integer id) {
        return repository.findById(id)
                .map(PatientResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
    }

    public PatientResponse create(PatientRequest req) {
        Patient p = new Patient();
        applyFields(p, req);
        return PatientResponse.from(repository.save(p));
    }

    public PatientResponse update(Integer id, PatientRequest req) {
        Patient p = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
        applyFields(p, req);
        return PatientResponse.from(repository.save(p));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Paciente", id);
        repository.deleteById(id);
    }

    private void applyFields(Patient p, PatientRequest req) {
        p.setName(req.getName());
        p.setPhone(req.getPhone());
        p.setBirthDate(req.getBirthDate());
        p.setAddress(req.getAddress());
        p.setComplement(req.getComplement());
        p.setNeighborhood(req.getNeighborhood());
        p.setZipCode(req.getZipCode());
        p.setState(req.getState());
    }
}
