package br.com.clinicah.service;

import br.com.clinicah.dto.AppointmentRequest;
import br.com.clinicah.dto.AppointmentResponse;
import br.com.clinicah.dto.DaySlots;
import br.com.clinicah.exception.ResourceNotFoundException;
import br.com.clinicah.model.Appointment;
import br.com.clinicah.model.AppointmentStatus;
import br.com.clinicah.repository.AppointmentRepository;
import br.com.clinicah.repository.DoctorRepository;
import br.com.clinicah.repository.PatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AppointmentService {

    private final AppointmentRepository repository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public AppointmentService(AppointmentRepository repository,
                               DoctorRepository doctorRepository,
                               PatientRepository patientRepository) {
        this.repository = repository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(AppointmentResponse::from);
    }

    @Transactional(readOnly = true)
    public AppointmentResponse findById(Integer id) {
        return repository.findById(id)
                .map(AppointmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", id));
    }

    @Transactional
    public AppointmentResponse create(AppointmentRequest req) {
        var doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico", req.getDoctorId()));
        var patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", req.getPatientId()));

        checkConflict(req.getDoctorId(), req.getScheduledAt(), null);

        Appointment a = new Appointment();
        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setScheduledAt(req.getScheduledAt());
        a.setNotes(req.getNotes());
        AppointmentResponse saved = AppointmentResponse.from(repository.save(a));
        log.info("Consulta agendada id={} médico={} paciente={} horário={}",
                saved.getId(), req.getDoctorId(), req.getPatientId(), req.getScheduledAt());
        return saved;
    }

    @Transactional
    public AppointmentResponse update(Integer id, AppointmentRequest req) {
        Appointment a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", id));

        var doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico", req.getDoctorId()));
        var patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", req.getPatientId()));

        if (!req.getScheduledAt().equals(a.getScheduledAt()) || !req.getDoctorId().equals(a.getDoctor().getId())) {
            checkConflict(req.getDoctorId(), req.getScheduledAt(), id);
        }

        a.setDoctor(doctor);
        a.setPatient(patient);
        a.setScheduledAt(req.getScheduledAt());
        a.setNotes(req.getNotes());
        if (req.getStatus() != null) {
            a.setStatus(req.getStatus());
        }
        log.info("Consulta atualizada id={} status={}", id, a.getStatus());
        return AppointmentResponse.from(repository.save(a));
    }

    @Transactional
    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Consulta", id);
        repository.deleteById(id);
        log.info("Consulta removida id={}", id);
    }

    @Transactional(readOnly = true)
    public Page<AppointmentResponse> getScheduleByMonth(Integer doctorId, int year, int month, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) throw new ResourceNotFoundException("Médico", doctorId);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        log.debug("Buscando agenda médico={} período={}/{}", doctorId, month, year);
        return repository.findByDoctorIdAndScheduledAtBetweenOrderByScheduledAt(doctorId, start, end, pageable)
                .map(AppointmentResponse::from);
    }

    /**
     * Calcula os horários disponíveis de um médico para um dado mês.
     * Regras: seg-sáb, 08h–19h (slots de 1h), exclui passado e consultas já agendadas/realizadas.
     */
    @Transactional(readOnly = true)
    public List<DaySlots> getAvailableSlots(Integer doctorId, int year, int month) {
        if (!doctorRepository.existsById(doctorId)) throw new ResourceNotFoundException("Médico", doctorId);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // Busca consultas ativas (não canceladas) do médico no período
        List<Appointment> booked = repository.findByDoctorIdAndScheduledAtBetweenAndStatusNot(
                doctorId, start, end, AppointmentStatus.CANCELADA);

        Set<LocalDateTime> bookedSlots = booked.stream()
                .map(Appointment::getScheduledAt)
                .collect(Collectors.toSet());

        LocalDateTime now = LocalDateTime.now();
        List<DaySlots> result = new ArrayList<>();

        // Horário: 08:00 ao slot das 19:00 (último início às 19h = término 20h)
        final int START_HOUR = 8;
        final int END_HOUR = 19;

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);

            // Só seg-sáb
            if (date.getDayOfWeek() == DayOfWeek.SUNDAY) continue;

            List<String> slots = new ArrayList<>();
            for (int h = START_HOUR; h <= END_HOUR; h++) {
                LocalDateTime slot = date.atTime(h, 0);
                if (slot.isBefore(now)) continue;          // ignora passado
                if (bookedSlots.contains(slot)) continue;  // ignora ocupados
                slots.add(String.format("%02d:00", h));
            }

            if (!slots.isEmpty()) {
                result.add(new DaySlots(date.toString(), slots));
            }
        }

        log.debug("Slots disponíveis médico={} mês={}/{} dias={}", doctorId, month, year, result.size());
        return result;
    }

    private void checkConflict(Integer doctorId, LocalDateTime scheduledAt, Integer excludeId) {
        boolean conflict = excludeId == null
                ? repository.existsByDoctorIdAndScheduledAt(doctorId, scheduledAt)
                : repository.existsByDoctorIdAndScheduledAtAndIdNot(doctorId, scheduledAt, excludeId);

        if (conflict) {
            log.warn("Conflito de horário: médico={} horário={}", doctorId, scheduledAt);
            throw new IllegalStateException("Médico já possui consulta agendada para " + scheduledAt);
        }
    }
}
