package br.com.clinicah.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

public class MedicalRecordRequest {

    @NotNull(message = "ID do médico é obrigatório")
    private Integer doctorId;

    // appointmentId é opcional — prontuário pode ser criado sem consulta vinculada
    private Integer appointmentId;

    @NotNull(message = "Data do atendimento é obrigatória")
    @PastOrPresent(message = "Data do atendimento não pode ser futura")
    private LocalDateTime recordDate;

    @NotBlank(message = "Queixa principal é obrigatória")
    private String chiefComplaint;

    private String clinicalFindings;

    private String diagnosis;

    private String treatmentPlan;

    private String prescription;

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }
    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public LocalDateTime getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDateTime recordDate) { this.recordDate = recordDate; }
    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }
    public String getClinicalFindings() { return clinicalFindings; }
    public void setClinicalFindings(String clinicalFindings) { this.clinicalFindings = clinicalFindings; }
    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }
    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }
}
