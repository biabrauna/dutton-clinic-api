package br.com.clinicah.dto;

import br.com.clinicah.model.Patient;

import java.time.LocalDate;

public class PatientResponse {

    private Integer id;
    private String name;
    private String phone;
    private LocalDate birthDate;
    private String address;
    private String complement;
    private String neighborhood;
    private String zipCode;
    private String state;

    public static PatientResponse from(Patient p) {
        PatientResponse r = new PatientResponse();
        r.id = p.getId();
        r.name = p.getName();
        r.phone = p.getPhone();
        r.birthDate = p.getBirthDate();
        r.address = p.getAddress();
        r.complement = p.getComplement();
        r.neighborhood = p.getNeighborhood();
        r.zipCode = p.getZipCode();
        r.state = p.getState();
        return r;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getAddress() { return address; }
    public String getComplement() { return complement; }
    public String getNeighborhood() { return neighborhood; }
    public String getZipCode() { return zipCode; }
    public String getState() { return state; }
}
