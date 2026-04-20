package br.com.clinicah.service;

import br.com.clinicah.dto.MedicalRecordRequest;
import br.com.clinicah.dto.MedicalRecordResponse;
import br.com.clinicah.exception.ResourceNotFoundException;
import br.com.clinicah.model.MedicalRecord;
import br.com.clinicah.repository.AppointmentRepository;
import br.com.clinicah.repository.DoctorRepository;
import br.com.clinicah.repository.MedicalRecordRepository;
import br.com.clinicah.repository.PatientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<MedicalRecordResponse> findByPatient(Integer patientId, Pageable pageable) {
        if (!patientRepository.existsById(patientId))
            throw new ResourceNotFoundException("Paciente", patientId);

        return repository.findByPatientIdOrderByRecordDateDesc(patientId, pageable)
                .map(MedicalRecordResponse::from);
    }

    public MedicalRecordResponse findById(Integer patientId, Integer recordId) {
        MedicalRecord record = repository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Prontuário", recordId));

        if (!record.getPatient().getId().equals(patientId)) {
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

            // garante que a consulta pertence ao mesmo médico e paciente
            if (!appointment.getDoctor().getId().equals(req.getDoctorId())) {
                throw new IllegalStateException("A consulta não pertence ao médico informado");
            }
            if (!appointment.getPatient().getId().equals(patientId)) {
                throw new IllegalStateException("A consulta não pertence ao paciente informado");
            }
            record.setAppointment(appointment);
        }

        return MedicalRecordResponse.from(repository.save(record));
    }

    @Transactional
    public MedicalRecordResponse update(Integer patientId, Integer recordId, MedicalRecordRequest req) {
        MedicalRecord record = repository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Prontuário", recordId));

        if (!record.getPatient().getId().equals(patientId)) {
            throw new IllegalStateException("Prontuário não pertence ao paciente informado");
        }

        // apenas o médico que criou pode editar (ou ROOT, controlado na camada de segurança)
        if (!record.getDoctor().getId().equals(req.getDoctorId())) {
            throw new IllegalStateException("Somente o médico que criou o prontuário pode editá-lo");
        }

        record.setRecordDate(req.getRecordDate());
        record.setChiefComplaint(req.getChiefComplaint());
        record.setClinicalFindings(req.getClinicalFindings());
        record.setDiagnosis(req.getDiagnosis());
        record.setTreatmentPlan(req.getTreatmentPlan());
        record.setPrescription(req.getPrescription());

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
    }
}
