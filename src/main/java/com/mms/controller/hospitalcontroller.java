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
@RequestMapping("/hospital")
public class HospitalController {

    private final ObjectMapper mapper = new ObjectMapper();
    private final File dbFile = new File("/tmp/medical_info.json");

    private final String hospitalUsername = "admin";
    private final String hospitalPassword = "password";

    private List<Individual> readDb() throws Exception {
        if (!dbFile.exists()) return new ArrayList<>();
        return mapper.readValue(dbFile, new TypeReference<>() {});
    }

    private void writeDb(List<Individual> users) throws Exception {
        mapper.writerWithDefaultPrettyPrinter().writeValue(dbFile, users);
    }

    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> creds, HttpSession session) {
        if (hospitalUsername.equals(creds.get("username")) && hospitalPassword.equals(creds.get("password"))) {
            session.setAttribute("hospital", true);
            return Map.of("message", "Hospital login successful");
        }
        return Map.of("message", "Invalid credentials");
    }

    @GetMapping("/records/{aadhar}")
    public Object getUserByAadhar(@PathVariable String aadhar, HttpSession session) throws Exception {
        if (session.getAttribute("hospital") == null) return Map.of("message", "Unauthorized");

        for (Individual user : readDb()) {
            if (user.getAadhar().equals(aadhar)) return user;
        }
        return Map.of("message", "User not found");
    }

    @PutMapping("/records/{aadhar}")
    public Map<String, String> updateUserByAadhar(@PathVariable String aadhar,
                                                  @RequestBody Map<String, Object> body,
                                                  HttpSession session) throws Exception {
        if (session.getAttribute("hospital") == null) return Map.of("message", "Unauthorized");

        List<Individual> users = readDb();
        for (Individual user : users) {
            if (user.getAadhar().equals(aadhar)) {
                List<MedicalRecord> updated = mapper.convertValue(body.get("records"), new TypeReference<>() {});
                user.setRecords(updated);
                writeDb(users);
                return Map.of("message", "User record updated");
            }
        }
        return Map.of("message", "User not found");
    }

    @DeleteMapping("/records/{aadhar}")
    public Map<String, String> deleteUserRecord(@PathVariable String aadhar, HttpSession session) throws Exception {
        if (session.getAttribute("hospital") == null) return Map.of("message", "Unauthorized");

        List<Individual> users = readDb();
        for (Individual user : users) {
            if (user.getAadhar().equals(aadhar)) {
                user.setRecords(new ArrayList<>());
                writeDb(users);
                return Map.of("message", "User records deleted");
            }
        }
        return Map.of("message", "User not found");
    }
}
