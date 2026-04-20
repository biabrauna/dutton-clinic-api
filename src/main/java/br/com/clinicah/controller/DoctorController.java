package br.com.clinicah.controller;

import br.com.clinicah.dto.AppointmentResponse;
import br.com.clinicah.dto.DaySlots;
import br.com.clinicah.dto.DoctorRequest;
import br.com.clinicah.dto.DoctorResponse;
import br.com.clinicah.service.AppointmentService;
import br.com.clinicah.service.DoctorService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    private final DoctorService service;
    private final AppointmentService appointmentService;

    public DoctorController(DoctorService service, AppointmentService appointmentService) {
        this.service = service;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/specialties")
    public List<String> getSpecialties() {
        return service.getSpecialties();
    }

    @GetMapping
    public Page<DoctorResponse> findAll(
            @RequestParam(required = false) String specialty,
            @PageableDefault(size = 10, sort = "name") Pageable pageable) {
        return service.findAll(specialty, pageable);
    }

    @GetMapping("/{id}")
    public DoctorResponse findById(@PathVariable Integer id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DoctorResponse create(@Valid @RequestBody DoctorRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public DoctorResponse update(@PathVariable Integer id, @Valid @RequestBody DoctorRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }

    /**
     * Agenda do médico: consultas de um mês específico.
     * Exemplo: GET /doctors/1/schedule?year=2026&month=5&page=0&size=20
     */
    @GetMapping("/{id}/schedule")
    public Page<AppointmentResponse> getSchedule(
            @PathVariable Integer id,
            @RequestParam int year,
            @RequestParam @Min(1) @Max(12) int month,
            @PageableDefault(size = 20, sort = "scheduledAt") Pageable pageable) {
        return appointmentService.getScheduleByMonth(id, year, month, pageable);
    }

    /**
     * Horários disponíveis do médico em um mês.
     * Slots de 1h, seg-sáb, 08h–19h (último slot às 19:00).
     * Exemplo: GET /doctors/1/available-slots?year=2026&month=5
     */
    @GetMapping("/{id}/available-slots")
    public List<DaySlots> getAvailableSlots(
            @PathVariable Integer id,
            @RequestParam int year,
            @RequestParam @Min(1) @Max(12) int month) {
        return appointmentService.getAvailableSlots(id, year, month);
    }
}
