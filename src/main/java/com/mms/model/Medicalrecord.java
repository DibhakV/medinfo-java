package com.mms.model;

import lombok.Data;
import java.util.List;

@Data
public class Individual {
    private String aadhar;
    private String name;
    private List<MedicalRecord> records;
}
