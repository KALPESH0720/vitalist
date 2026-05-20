package com.hospital.controller;

import com.hospital.model.*;
import com.hospital.dto.*;
import com.hospital.service.ServiceFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

// -- Auth Controller -------------------------------------------------------
@RestController
@RequestMapping("/api/auth")
class AuthController {
    private final ServiceFacade facade;
    private final org.springframework.security.crypto.password.PasswordEncoder encoder;

    AuthController(ServiceFacade facade, org.springframework.security.crypto.password.PasswordEncoder encoder) {
        this.facade = facade;
        this.encoder = encoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return ResponseEntity.status(400)
                .body(Map.of("success", false, "error", "Username and password required"));
        }

        try {
            User user = facade.findUserByUsername(username.trim()).orElse(null);
            if (user == null) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "error", "Invalid credentials"));
            }

            if (!user.isActive()) {
                return ResponseEntity.status(403)
                    .body(Map.of("success", false, "error", "Account is disabled"));
            }

            if (!encoder.matches(password, user.getPassword())) {
                return ResponseEntity.status(401)
                    .body(Map.of("success", false, "error", "Invalid credentials"));
            }

            String raw = username.trim() + ":" + password;
            String token = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("userId", user.getId());
            resp.put("username", user.getUsername());
            resp.put("fullName", user.getFullName());
            resp.put("role", user.getRole().name());
            resp.put("basicToken", token);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("success", false, "error", "Server error: " + e.getMessage()));
        }
    }
}

// -- Dashboard Controller -------------------------------------------------------
@RestController
@RequestMapping("/api/dashboard")
class DashboardController {
    private final ServiceFacade facade;
    DashboardController(ServiceFacade facade) { this.facade = facade; }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(facade.getDashboardStats());
    }
}

// -- Patient Controller -------------------------------------------------------
@RestController
@RequestMapping("/api/patients")
class PatientController {
    private final ServiceFacade facade;
    PatientController(ServiceFacade facade) { this.facade = facade; }

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        List<Patient> patients;
        if (search != null && !search.isBlank()) patients = facade.searchPatients(search);
        else if (status != null && !status.isBlank()) {
            try { patients = facade.getPatientsByStatus(Patient.Status.valueOf(status.toUpperCase())); }
            catch (IllegalArgumentException e) { patients = facade.getAllPatients(); }
        } else {
            patients = facade.getAllPatients();
        }
        return ResponseEntity.ok(patients.stream().map(PatientDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getById(@PathVariable Long id) {
        return facade.getPatientById(id)
            .map(p -> ResponseEntity.ok(PatientDTO.fromEntity(p)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PatientDTO> create(@RequestBody Map<String, Object> body) {
        Patient p = mapToPatient(body);
        Long doctorId = body.get("doctorId") != null ? ParseUtils.safeLong(body.get("doctorId")) : null;
        Patient saved = facade.savePatient(p, doctorId);
        return ResponseEntity.ok(PatientDTO.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientDTO> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Patient p = mapToPatient(body);
        Long doctorId = body.get("doctorId") != null ? ParseUtils.safeLong(body.get("doctorId")) : null;
        Patient updated = facade.updatePatient(id, p, doctorId);
        return ResponseEntity.ok(PatientDTO.fromEntity(updated));
    }

    @PostMapping("/{id}/discharge")
    public ResponseEntity<Map<String, String>> discharge(@PathVariable Long id) {
        facade.dischargePatient(id);
        return ResponseEntity.ok(Map.of("status", "Patient discharged"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        facade.deletePatient(id);
        return ResponseEntity.ok(Map.of("status", "Deleted"));
    }

    private Patient mapToPatient(Map<String, Object> b) {
        Patient p = new Patient();
        if (b.get("patientId") != null) p.setPatientId(b.get("patientId").toString());
        p.setFullName(b.get("fullName").toString());
        p.setAge(ParseUtils.safeInt(b.get("age"), 0));
        p.setGender(Patient.Gender.valueOf(b.get("gender").toString().toUpperCase()));
        p.setContact(b.get("contact").toString());
        if (b.get("bloodGroup") != null) p.setBloodGroup(b.get("bloodGroup").toString());
        if (b.get("address") != null) p.setAddress(b.get("address").toString());
        if (b.get("admissionDate") != null) p.setAdmissionDate(LocalDate.parse(b.get("admissionDate").toString()));
        else p.setAdmissionDate(LocalDate.now());
        if (b.get("ward") != null) p.setWard(b.get("ward").toString());
        if (b.get("roomNumber") != null) p.setRoomNumber(b.get("roomNumber").toString());
        if (b.get("status") != null) p.setStatus(Patient.Status.valueOf(b.get("status").toString().toUpperCase()));
        if (b.get("emergencyNotes") != null) p.setEmergencyNotes(b.get("emergencyNotes").toString());
        return p;
    }
}

// -- Appointment Controller -------------------------------------------------------
@RestController
@RequestMapping("/api/appointments")
class AppointmentController {
    private final ServiceFacade facade;
    AppointmentController(ServiceFacade facade) { this.facade = facade; }

    @GetMapping
    public ResponseEntity<List<AppointmentDTO>> getAll(
            @RequestParam(required = false) String scope,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        List<Appointment> appointments;
        if ("today".equals(scope)) appointments = facade.getTodayAppointments();
        else if (patientId != null) appointments = facade.getAppointmentsByPatient(patientId);
        else if (doctorId != null) appointments = facade.getAppointmentsByDoctor(doctorId);
        else appointments = facade.getAllAppointments();
        return ResponseEntity.ok(appointments.stream().map(AppointmentDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AppointmentDTO> getById(@PathVariable Long id) {
        return facade.getAppointmentById(id)
            .map(a -> ResponseEntity.ok(AppointmentDTO.fromEntity(a)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<AppointmentDTO> create(@RequestBody Map<String, Object> body) {
        Appointment a = mapToAppointment(body);
        Long patientId = ParseUtils.safeLong(body.get("patientId"));
        Long doctorId = ParseUtils.safeLong(body.get("doctorId"));
        Long createdById = body.get("createdById") != null ? ParseUtils.safeLong(body.get("createdById")) : null;
        Appointment saved = facade.saveAppointment(a, patientId, doctorId, createdById);
        return ResponseEntity.ok(AppointmentDTO.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AppointmentDTO> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Appointment a = mapToAppointment(body);
        Long patientId = body.get("patientId") != null ? ParseUtils.safeLong(body.get("patientId")) : null;
        Long doctorId = body.get("doctorId") != null ? ParseUtils.safeLong(body.get("doctorId")) : null;
        Appointment updated = facade.updateAppointment(id, a, patientId, doctorId);
        return ResponseEntity.ok(AppointmentDTO.fromEntity(updated));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<AppointmentDTO> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Appointment.Status status = Appointment.Status.valueOf(body.get("status").toUpperCase());
        Appointment updated = facade.updateAppointmentStatus(id, status);
        return ResponseEntity.ok(AppointmentDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        facade.deleteAppointment(id);
        return ResponseEntity.ok(Map.of("status", "Deleted"));
    }

    private Appointment mapToAppointment(Map<String, Object> b) {
        Appointment a = new Appointment();
        if (b.get("appointmentDate") != null) a.setAppointmentDate(LocalDate.parse(b.get("appointmentDate").toString()));
        if (b.get("appointmentTime") != null) a.setAppointmentTime(LocalTime.parse(b.get("appointmentTime").toString()));
        if (b.get("department") != null) a.setDepartment(b.get("department").toString());
        if (b.get("reason") != null) a.setReason(b.get("reason").toString());
        if (b.get("notes") != null) a.setNotes(b.get("notes").toString());
        if (b.get("status") != null) a.setStatus(Appointment.Status.valueOf(b.get("status").toString().toUpperCase()));
        return a;
    }
}

// -- Inventory Controller -------------------------------------------------------
@RestController
@RequestMapping("/api/inventory")
class InventoryController {
    private final ServiceFacade facade;
    InventoryController(ServiceFacade facade) { this.facade = facade; }

    @GetMapping
    public ResponseEntity<List<InventoryDTO>> getAll(@RequestParam(required = false) String search,
                                                      @RequestParam(required = false) String filter) {
        List<Inventory> items;
        if (search != null && !search.isBlank()) items = facade.searchInventory(search);
        else if ("low".equals(filter)) items = facade.getLowStock();
        else if ("expired".equals(filter)) items = facade.getExpired();
        else if ("expiring".equals(filter)) items = facade.getExpiringSoon();
        else items = facade.getAllInventory();
        return ResponseEntity.ok(items.stream().map(InventoryDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryDTO> getById(@PathVariable Long id) {
        return facade.getInventoryById(id)
            .map(i -> ResponseEntity.ok(InventoryDTO.fromEntity(i)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<InventoryDTO> create(@RequestBody Inventory item) {
        Inventory saved = facade.saveInventory(item);
        return ResponseEntity.ok(InventoryDTO.fromEntity(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDTO> update(@PathVariable Long id, @RequestBody Inventory item) {
        Inventory updated = facade.updateInventory(id, item);
        return ResponseEntity.ok(InventoryDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        facade.deleteInventory(id);
        return ResponseEntity.ok(Map.of("status", "Deleted"));
    }
}

// -- User Management Controller -------------------------------------------------------
@RestController
@RequestMapping("/api/users")
class UserController {
    private final ServiceFacade facade;
    UserController(ServiceFacade facade) { this.facade = facade; }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAll() {
        return ResponseEntity.ok(facade.getAllUsers().stream().map(UserDTO::fromEntity).collect(Collectors.toList()));
    }

    @GetMapping("/doctors")
    public ResponseEntity<List<UserDTO>> getDoctors() {
        return ResponseEntity.ok(facade.getDoctors().stream().map(UserDTO::fromEntity).collect(Collectors.toList()));
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(@RequestBody User user) {
        User created = facade.createUser(user);
        return ResponseEntity.ok(UserDTO.fromEntity(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(@PathVariable Long id, @RequestBody User user) {
        User updated = facade.updateUser(id, user);
        return ResponseEntity.ok(UserDTO.fromEntity(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {
        facade.deleteUser(id);
        return ResponseEntity.ok(Map.of("status", "Deleted"));
    }
}

// Utility class
class ParseUtils {
    static Long safeLong(Object val) {
        if (val == null) return null;
        String s = val.toString().trim();
        if (s.isBlank()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }

    static Integer safeInt(Object val, Integer fallback) {
        if (val == null) return fallback;
        String s = val.toString().trim();
        if (s.isBlank()) return fallback;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return fallback; }
    }
}
