package br.com.clinicah.dto;

public class TokenResponse {
    private final String token;
    private final String type = "Bearer";

    public TokenResponse(String token) {
        this.token = token;
    }

    public String getToken() { return token; }
    public String getType() { return type; }
}
