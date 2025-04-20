package com.mms.model;

import lombok.Data;

@Data
public class MedicalRecord {
    private String date;
    private String diagnosis;
    private String prescription;
    private String doctor;
}
