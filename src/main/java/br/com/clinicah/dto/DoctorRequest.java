package br.com.clinicah.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class DoctorRequest {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Especialidade é obrigatória")
    private String specialty;

    @NotBlank(message = "CRM é obrigatório")
    private String crm;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSpecialty() { return specialty; }
    public void setSpecialty(String specialty) { this.specialty = specialty; }
    public String getCrm() { return crm; }
    public void setCrm(String crm) { this.crm = crm; }
}
