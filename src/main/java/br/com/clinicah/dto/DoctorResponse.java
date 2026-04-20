package br.com.clinicah.dto;

import br.com.clinicah.model.Doctor;

public class DoctorResponse {
    private Integer id;
    private String name;
    private String email;
    private String specialty;
    private String crm;

    public static DoctorResponse from(Doctor d) {
        DoctorResponse r = new DoctorResponse();
        r.id = d.getId();
        r.name = d.getName();
        r.email = d.getEmail();
        r.specialty = d.getSpecialty();
        r.crm = d.getCrm();
        return r;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getSpecialty() { return specialty; }
    public String getCrm() { return crm; }
}
