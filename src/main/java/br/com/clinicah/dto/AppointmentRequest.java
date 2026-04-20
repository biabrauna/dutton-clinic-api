package br.com.clinicah.dto;

import br.com.clinicah.model.AppointmentStatus;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class AppointmentRequest {

    @NotNull(message = "ID do médico é obrigatório")
    private Integer doctorId;

    @NotNull(message = "ID do paciente é obrigatório")
    private Integer patientId;

    @NotNull(message = "Data/hora é obrigatória")
    @Future(message = "A consulta deve ser agendada para uma data futura")
    private LocalDateTime scheduledAt;

    private String notes;

    // usado apenas em PUT para atualizar status
    private AppointmentStatus status;

    public Integer getDoctorId() { return doctorId; }
    public void setDoctorId(Integer doctorId) { this.doctorId = doctorId; }
    public Integer getPatientId() { return patientId; }
    public void setPatientId(Integer patientId) { this.patientId = patientId; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }
}
