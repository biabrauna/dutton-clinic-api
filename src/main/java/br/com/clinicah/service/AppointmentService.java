package br.com.clinicah.service;

import br.com.clinicah.dto.AppointmentRequest;
import br.com.clinicah.dto.AppointmentResponse;
import br.com.clinicah.exception.ResourceNotFoundException;
import br.com.clinicah.model.Appointment;
import br.com.clinicah.model.AppointmentStatus;
import br.com.clinicah.repository.AppointmentRepository;
import br.com.clinicah.repository.DoctorRepository;
import br.com.clinicah.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;

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

    public Page<AppointmentResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(AppointmentResponse::from);
    }

    public AppointmentResponse findById(Integer id) {
        return repository.findById(id)
                .map(AppointmentResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", id));
    }

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
        // status default é AGENDADA (definido no model)
        return AppointmentResponse.from(repository.save(a));
    }

    public AppointmentResponse update(Integer id, AppointmentRequest req) {
        Appointment a = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta", id));

        var doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico", req.getDoctorId()));
        var patient = patientRepository.findById(req.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", req.getPatientId()));

        // só checa conflito se horário ou médico mudou
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
        return AppointmentResponse.from(repository.save(a));
    }

    public void delete(Integer id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Consulta", id);
        repository.deleteById(id);
    }

    public Page<AppointmentResponse> getScheduleByMonth(Integer doctorId, int year, int month, Pageable pageable) {
        if (!doctorRepository.existsById(doctorId)) throw new ResourceNotFoundException("Médico", doctorId);

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        return repository.findByDoctorIdAndScheduledAtBetweenOrderByScheduledAt(doctorId, start, end, pageable)
                .map(AppointmentResponse::from);
    }

    private void checkConflict(Integer doctorId, LocalDateTime scheduledAt, Integer excludeId) {
        boolean conflict = excludeId == null
                ? repository.existsByDoctorIdAndScheduledAt(doctorId, scheduledAt)
                : repository.existsByDoctorIdAndScheduledAtAndIdNot(doctorId, scheduledAt, excludeId);

        if (conflict) {
            throw new IllegalStateException(
                    "Médico já possui consulta agendada para " + scheduledAt);
        }
    }
}
