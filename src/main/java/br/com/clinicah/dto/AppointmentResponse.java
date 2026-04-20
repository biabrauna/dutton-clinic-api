package br.com.clinicah.dto;

import br.com.clinicah.model.Appointment;
import br.com.clinicah.model.AppointmentStatus;

import java.time.LocalDateTime;

public class AppointmentResponse {

    private Integer id;
    private DoctorSummary doctor;
    private PatientSummary patient;
    private LocalDateTime scheduledAt;
    private AppointmentStatus status;
    private String notes;

    public static AppointmentResponse from(Appointment a) {
        AppointmentResponse r = new AppointmentResponse();
        r.id = a.getId();
        r.doctor = new DoctorSummary(a.getDoctor().getId(), a.getDoctor().getName(), a.getDoctor().getSpecialty());
        r.patient = new PatientSummary(a.getPatient().getId(), a.getPatient().getName(), a.getPatient().getPhone());
        r.scheduledAt = a.getScheduledAt();
        r.status = a.getStatus();
        r.notes = a.getNotes();
        return r;
    }

    public Integer getId() { return id; }
    public DoctorSummary getDoctor() { return doctor; }
    public PatientSummary getPatient() { return patient; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public AppointmentStatus getStatus() { return status; }
    public String getNotes() { return notes; }

    public record DoctorSummary(Integer id, String name, String specialty) {}
    public record PatientSummary(Integer id, String name, String phone) {}
}
