package br.com.clinicah.controller;

import br.com.clinicah.dto.PatientRequest;
import br.com.clinicah.dto.PatientResponse;
import br.com.clinicah.service.PatientService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @GetMapping
    public Page<PatientResponse> findAll(@PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public PatientResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientResponse create(@Valid @RequestBody PatientRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public PatientResponse update(@PathVariable Integer id, @Valid @RequestBody PatientRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
