package br.com.clinicah.service;

import br.com.clinicah.dto.PatientRequest;
import br.com.clinicah.dto.PatientResponse;
import br.com.clinicah.exception.ResourceNotFoundException;
import br.com.clinicah.model.Patient;
import br.com.clinicah.repository.PatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> findAll(Pageable pageable) {
        log.debug("Buscando pacientes - página {}", pageable.getPageNumber());
        return repository.findAll(pageable).map(PatientResponse::from);
    }

    @Transactional(readOnly = true)
    public PatientResponse findById(Integer id) {
        log.debug("Buscando paciente id={}", id);
        return repository.findById(id)
                .map(PatientResponse::from)
                .orElseThrow(() -> {
                    log.warn("Paciente não encontrado id={}", id);
                    return new ResourceNotFoundException("Paciente", id);
                });
    }

    @Transactional
    public PatientResponse create(PatientRequest req) {
        Patient p = new Patient();
        applyFields(p, req);
        PatientResponse saved = PatientResponse.from(repository.save(p));
        log.info("Paciente cadastrado id={} nome={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public PatientResponse update(Integer id, PatientRequest req) {
        Patient p = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", id));
        applyFields(p, req);
        log.info("Paciente atualizado id={}", id);
        return PatientResponse.from(repository.save(p));
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Paciente", id);
        repository.deleteById(id);
        log.info("Paciente removido id={}", id);
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
