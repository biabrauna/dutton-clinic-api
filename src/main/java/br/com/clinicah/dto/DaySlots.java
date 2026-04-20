package br.com.clinicah.dto;

import java.util.List;

/**
 * Represents the available appointment slots for a single day.
 * date: "YYYY-MM-DD"
 * availableSlots: list of "HH:00" strings (e.g., ["08:00", "09:00", "14:00"])
 */
public record DaySlots(String date, List<String> availableSlots) {}
