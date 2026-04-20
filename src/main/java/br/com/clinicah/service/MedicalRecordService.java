package br.com.clinicah.service;

import br.com.clinicah.dto.MedicalRecordRequest;
import br.com.clinicah.dto.MedicalRecordResponse;
import br.com.clinicah.exception.ResourceNotFoundException;
import br.com.clinicah.model.MedicalRecord;
import br.com.clinicah.repository.AppointmentRepository;
import br.com.clinicah.repository.DoctorRepository;
import br.com.clinicah.repository.MedicalRecordRepository;
import br.com.clinicah.repository.PatientRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class MedicalRecordService {

    private final MedicalRecordRepository repository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;

    public MedicalRecordService(MedicalRecordRepository repository,
                                 PatientRepository patientRepository,
                                 DoctorRepository doctorRepository,
                                 AppointmentRepository appointmentRepository) {
        this.repository = repository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public Page<MedicalRecordResponse> findByPatient(Integer patientId, Pageable pageable) {
        if (!patientRepository.existsById(patientId))
            throw new ResourceNotFoundException("Paciente", patientId);

        log.debug("Buscando prontuários paciente={}", patientId);
        return repository.findByPatientIdOrderByRecordDateDesc(patientId, pageable)
                .map(MedicalRecordResponse::from);
    }

    @Transactional(readOnly = true)
    public MedicalRecordResponse findById(Integer patientId, Integer recordId) {
        MedicalRecord record = repository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Prontuário", recordId));

        if (!record.getPatient().getId().equals(patientId)) {
            log.warn("Acesso negado: prontuário={} não pertence ao paciente={}", recordId, patientId);
            throw new IllegalStateException("Prontuário não pertence ao paciente informado");
        }
        return MedicalRecordResponse.from(record);
    }

    @Transactional
    public MedicalRecordResponse create(Integer patientId, MedicalRecordRequest req) {
        var patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente", patientId));
        var doctor = doctorRepository.findById(req.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Médico", req.getDoctorId()));

        MedicalRecord record = new MedicalRecord();
        record.setPatient(patient);
        record.setDoctor(doctor);
        record.setRecordDate(req.getRecordDate());
        record.setChiefComplaint(req.getChiefComplaint());
        record.setClinicalFindings(req.getClinicalFindings());
        record.setDiagnosis(req.getDiagnosis());
        record.setTreatmentPlan(req.getTreatmentPlan());
        record.setPrescription(req.getPrescription());

        if (req.getAppointmentId() != null) {
            var appointment = appointmentRepository.findById(req.getAppointmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Consulta", req.getAppointmentId()));

            if (!appointment.getDoctor().getId().equals(req.getDoctorId())) {
                throw new IllegalStateException("A consulta não pertence ao médico informado");
            }
            if (!appointment.getPatient().getId().equals(patientId)) {
                throw new IllegalStateException("A consulta não pertence ao paciente informado");
            }
            record.setAppointment(appointment);
        }

        MedicalRecordResponse saved = MedicalRecordResponse.from(repository.save(record));
        log.info("Prontuário criado id={} paciente={} médico={}", saved.getId(), patientId, req.getDoctorId());
        return saved;
    }

    @Transactional
    public MedicalRecordResponse update(Integer patientId, Integer recordId, MedicalRecordRequest req) {
        MedicalRecord record = repository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Prontuário", recordId));

        if (!record.getPatient().getId().equals(patientId)) {
            throw new IllegalStateException("Prontuário não pertence ao paciente informado");
        }
        if (!record.getDoctor().getId().equals(req.getDoctorId())) {
            log.warn("Edição negada: prontuário={} criado por médico={}, tentativa de médico={}",
                    recordId, record.getDoctor().getId(), req.getDoctorId());
            throw new IllegalStateException("Somente o médico que criou o prontuário pode editá-lo");
        }

        record.setRecordDate(req.getRecordDate());
        record.setChiefComplaint(req.getChiefComplaint());
        record.setClinicalFindings(req.getClinicalFindings());
        record.setDiagnosis(req.getDiagnosis());
        record.setTreatmentPlan(req.getTreatmentPlan());
        record.setPrescription(req.getPrescription());

        log.info("Prontuário atualizado id={}", recordId);
        return MedicalRecordResponse.from(repository.save(record));
    }

    @Transactional
    public void delete(Integer patientId, Integer recordId) {
        MedicalRecord record = repository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Prontuário", recordId));

        if (!record.getPatient().getId().equals(patientId)) {
            throw new IllegalStateException("Prontuário não pertence ao paciente informado");
        }
        repository.delete(record);
        log.info("Prontuário removido id={}", recordId);
    }
}
