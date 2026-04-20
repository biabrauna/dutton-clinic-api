package br.com.clinicah.service;

import br.com.clinicah.dto.DoctorRequest;
import br.com.clinicah.dto.DoctorResponse;
import br.com.clinicah.exception.ResourceNotFoundException;
import br.com.clinicah.model.Doctor;
import br.com.clinicah.repository.DoctorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class DoctorService {

    private final DoctorRepository repository;

    public DoctorService(DoctorRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<DoctorResponse> findAll(Pageable pageable) {
        log.debug("Buscando médicos - página {}, tamanho {}", pageable.getPageNumber(), pageable.getPageSize());
        return repository.findAll(pageable).map(DoctorResponse::from);
    }

    @Transactional(readOnly = true)
    public DoctorResponse findById(Integer id) {
        log.debug("Buscando médico id={}", id);
        return repository.findById(id)
                .map(DoctorResponse::from)
                .orElseThrow(() -> {
                    log.warn("Médico não encontrado id={}", id);
                    return new ResourceNotFoundException("Médico", id);
                });
    }

    @Transactional
    public DoctorResponse create(DoctorRequest req) {
        Doctor doctor = new Doctor();
        applyFields(doctor, req);
        DoctorResponse saved = DoctorResponse.from(repository.save(doctor));
        log.info("Médico cadastrado id={} crm={}", saved.getId(), saved.getCrm());
        return saved;
    }

    @Transactional
    public DoctorResponse update(Integer id, DoctorRequest req) {
        Doctor doctor = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Médico", id));
        applyFields(doctor, req);
        DoctorResponse updated = DoctorResponse.from(repository.save(doctor));
        log.info("Médico atualizado id={}", id);
        return updated;
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Médico", id);
        repository.deleteById(id);
        log.info("Médico removido id={}", id);
    }

    private void applyFields(Doctor d, DoctorRequest req) {
        d.setName(req.getName());
        d.setEmail(req.getEmail());
        d.setSpecialty(req.getSpecialty());
        d.setCrm(req.getCrm());
    }
}
