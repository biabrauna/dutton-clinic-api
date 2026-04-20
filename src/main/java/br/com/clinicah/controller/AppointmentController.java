package br.com.clinicah.controller;

import br.com.clinicah.dto.AppointmentRequest;
import br.com.clinicah.dto.AppointmentResponse;
import br.com.clinicah.service.AppointmentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AppointmentResponse> findAll(@PageableDefault(size = 10, sort = "schedule") Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{id}")
    public AppointmentResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse create(@Valid @RequestBody AppointmentRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public AppointmentResponse update(@PathVariable Integer id, @Valid @RequestBody AppointmentRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
