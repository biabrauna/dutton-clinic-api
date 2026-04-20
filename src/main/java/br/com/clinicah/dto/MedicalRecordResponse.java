package br.com.clinicah.dto;

import br.com.clinicah.model.MedicalRecord;

import java.time.LocalDateTime;

public class MedicalRecordResponse {

    private Integer id;
    private Integer patientId;
    private String patientName;
    private Integer doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private Integer appointmentId;
    private LocalDateTime recordDate;
    private String chiefComplaint;
    private String clinicalFindings;
    private String diagnosis;
    private String treatmentPlan;
    private String prescription;
    private LocalDateTime createdAt;

    public static MedicalRecordResponse from(MedicalRecord r) {
        MedicalRecordResponse dto = new MedicalRecordResponse();
        dto.id = r.getId();
        dto.patientId = r.getPatient().getId();
        dto.patientName = r.getPatient().getName();
        dto.doctorId = r.getDoctor().getId();
        dto.doctorName = r.getDoctor().getName();
        dto.doctorSpecialty = r.getDoctor().getSpecialty();
        dto.appointmentId = r.getAppointment() != null ? r.getAppointment().getId() : null;
        dto.recordDate = r.getRecordDate();
        dto.chiefComplaint = r.getChiefComplaint();
        dto.clinicalFindings = r.getClinicalFindings();
        dto.diagnosis = r.getDiagnosis();
        dto.treatmentPlan = r.getTreatmentPlan();
        dto.prescription = r.getPrescription();
        dto.createdAt = r.getCreatedAt();
        return dto;
    }

    public Integer getId() { return id; }
    public Integer getPatientId() { return patientId; }
    public String getPatientName() { return patientName; }
    public Integer getDoctorId() { return doctorId; }
    public String getDoctorName() { return doctorName; }
    public String getDoctorSpecialty() { return doctorSpecialty; }
    public Integer getAppointmentId() { return appointmentId; }
    public LocalDateTime getRecordDate() { return recordDate; }
    public String getChiefComplaint() { return chiefComplaint; }
    public String getClinicalFindings() { return clinicalFindings; }
    public String getDiagnosis() { return diagnosis; }
    public String getTreatmentPlan() { return treatmentPlan; }
    public String getPrescription() { return prescription; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
