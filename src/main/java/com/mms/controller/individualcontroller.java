package com.mms.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mms.model.Individual;
import com.mms.model.MedicalRecord;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/individual")
public class IndividualController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File dbFile = new File("/tmp/medical_info.json");

    private List<Individual> readDb() throws Exception {
        if (!dbFile.exists()) return new ArrayList<>();
        return mapper.readValue(dbFile, new TypeReference<>() {});
    }

    private void writeDb(List<Individual> users) throws Exception {
        mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, users);
    }

    @PostMapping("/register")
    public Map<String, String> register(@RequestBody Individual newUser) throws Exception {
        List<Individual> users = readDb();

        for (Individual u : users) {
            if (u.getAadhar().equals(newUser.getAadhar())) {
                return Map.of("message", "User already exists");
            }
        }

        users.add(newUser);
        writeDb(users);
        return Map.of("message", "Registered successfully");
    }

    @PostMapping("/login")
    public Object login(@RequestBody Map<String, String> data, HttpSession session) throws Exception {
        String aadhar = data.get("aadhar");
        for (Individual u : readDb()) {
            if (u.getAadhar().equals(aadhar)) {
                session.setAttribute("aadhar", aadhar);
                return u;
            }
        }
        return Map.of("message", "Invalid Aadhar");
    }

    @GetMapping("/record")
    public Object getRecord(HttpSession session) throws Exception {
        String aadhar = (String) session.getAttribute("aadhar");
        if (aadhar == null) return Map.of("message", "Unauthorized");

        for (Individual u : readDb()) {
            if (u.getAadhar().equals(aadhar)) return u;
        }
        return Map.of("message", "Not found");
    }

    @PutMapping("/record")
    public Map<String, String> updateRecord(@RequestBody Map<String, Object> request, HttpSession session) throws Exception {
        String aadhar = (String) session.getAttribute("aadhar");
        if (aadhar == null) return Map.of("message", "Unauthorized");

        List<Individual> users = readDb();
        for (Individual u : users) {
            if (u.getAadhar().equals(aadhar)) {
                List<MedicalRecord> updated = mapper.convertValue(request.get("records"), new TypeReference<>() {});
                u.setRecords(updated);
                writeDb(users);
                return Map.of("message", "Record updated");
            }
        }

        return Map.of("message", "User not found");
    }

    @DeleteMapping("/record")
    public Map<String, String> deleteRecord(HttpSession session) throws Exception {
        String aadhar = (String) session.getAttribute("aadhar");
        if (aadhar == null) return Map.of("message", "Unauthorized");

        List<Individual> users = readDb();
        for (Individual u : users) {
            if (u.getAadhar().equals(aadhar)) {
                u.setRecords(new ArrayList<>());
                writeDb(users);
                return Map.of("message", "Records cleared");
            }
        }

        return Map.of("message", "User not found");
    }
}
