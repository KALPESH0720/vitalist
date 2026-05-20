package com.hospital.service;

import com.hospital.model.*;
import com.hospital.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.*;

// -- PatientService ------------------------------------------------------------
@Service
class PatientService {

    private final PatientRepository patientRepo;
    private final UserRepository userRepo;

    PatientService(PatientRepository patientRepo, UserRepository userRepo) {
        this.patientRepo = patientRepo;
        this.userRepo    = userRepo;
    }

    @Transactional(readOnly = true)
    public List<Patient> getAll()                           { return patientRepo.findAll(); }
    @Transactional(readOnly = true)
    public Optional<Patient> getById(Long id)               { return patientRepo.findById(id); }
    @Transactional(readOnly = true)
    public List<Patient> searchByName(String name)          { return patientRepo.findByFullNameContainingIgnoreCase(name); }
    @Transactional(readOnly = true)
    public List<Patient> getByStatus(Patient.Status status) { return patientRepo.findByStatus(status); }
    @Transactional(readOnly = true)
    public long countAdmitted()                             { return patientRepo.countAdmitted(); }

    @Transactional
    public Patient save(Patient patient, Long doctorId) {
        if (patient.getPatientId() == null || patient.getPatientId().isBlank()) {
            patient.setPatientId("P" + System.currentTimeMillis());
        }
        if (doctorId != null) {
            userRepo.findById(doctorId).ifPresent(patient::setAssignedDoctor);
        }
        return patientRepo.save(patient);
    }

    @Transactional
    public Patient update(Long id, Patient updated, Long doctorId) {
        Patient existing = patientRepo.findById(id).orElseThrow();
        existing.setFullName(updated.getFullName());
        existing.setAge(updated.getAge());
        existing.setGender(updated.getGender());
        existing.setContact(updated.getContact());
        existing.setBloodGroup(updated.getBloodGroup());
        existing.setAddress(updated.getAddress());
        existing.setWard(updated.getWard());
        existing.setRoomNumber(updated.getRoomNumber());
        existing.setStatus(updated.getStatus());
        existing.setEmergencyNotes(updated.getEmergencyNotes());
        if (doctorId != null) {
            userRepo.findById(doctorId).ifPresent(existing::setAssignedDoctor);
        }
        return patientRepo.save(existing);
    }

    @Transactional
    public void discharge(Long id) {
        patientRepo.findById(id).ifPresent(p -> {
            p.setStatus(Patient.Status.DISCHARGED);
            patientRepo.save(p);
        });
    }

    @Transactional
    public void delete(Long id) { patientRepo.deleteById(id); }
}

// -- AppointmentService --------------------------------------------------------
@Service
class AppointmentService {

    private final AppointmentRepository apptRepo;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;

    AppointmentService(AppointmentRepository apptRepo, PatientRepository patientRepo, UserRepository userRepo) {
        this.apptRepo   = apptRepo;
        this.patientRepo= patientRepo;
        this.userRepo   = userRepo;
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAll()                                        { return apptRepo.findAll(); }
    @Transactional(readOnly = true)
    public Optional<Appointment> getById(Long id)                            { return apptRepo.findById(id); }
    @Transactional(readOnly = true)
    public List<Appointment> getByPatient(Long patientId)                    { return apptRepo.findByPatientId(patientId); }
    @Transactional(readOnly = true)
    public List<Appointment> getByDoctor(Long doctorId)                      { return apptRepo.findByDoctorId(doctorId); }
    @Transactional(readOnly = true)
    public List<Appointment> getByDate(LocalDate date)                       { return apptRepo.findByAppointmentDate(date); }
    @Transactional(readOnly = true)
    public List<Appointment> getToday()                                      { return apptRepo.findByAppointmentDate(LocalDate.now()); }
    @Transactional(readOnly = true)
    public long countScheduledToday()                                        { return apptRepo.countScheduledForDate(LocalDate.now()); }

    @Transactional
    public Appointment save(Appointment appt, Long patientId, Long doctorId, Long createdById) {
        patientRepo.findById(patientId).ifPresent(appt::setPatient);
        userRepo.findById(doctorId).ifPresent(appt::setDoctor);
        userRepo.findById(createdById).ifPresent(appt::setCreatedBy);
        return apptRepo.save(appt);
    }

    @Transactional
    public Appointment updateStatus(Long id, Appointment.Status status) {
        Appointment a = apptRepo.findById(id).orElseThrow();
        a.setStatus(status);
        return apptRepo.save(a);
    }

    @Transactional
    public Appointment update(Long id, Appointment updated, Long patientId, Long doctorId) {
        Appointment a = apptRepo.findById(id).orElseThrow();
        a.setAppointmentDate(updated.getAppointmentDate());
        a.setAppointmentTime(updated.getAppointmentTime());
        a.setDepartment(updated.getDepartment());
        a.setReason(updated.getReason());
        a.setNotes(updated.getNotes());
        a.setStatus(updated.getStatus());
        if (patientId != null) patientRepo.findById(patientId).ifPresent(a::setPatient);
        if (doctorId  != null) userRepo.findById(doctorId).ifPresent(a::setDoctor);
        return apptRepo.save(a);
    }

    @Transactional
    public void delete(Long id) { apptRepo.deleteById(id); }
}

// -- InventoryService ----------------------------------------------------------
@Service
class InventoryService {

    private final InventoryRepository inventoryRepo;

    InventoryService(InventoryRepository inventoryRepo) {
        this.inventoryRepo = inventoryRepo;
    }

    @Transactional(readOnly = true)
    public List<Inventory> getAll()                         { return inventoryRepo.findAll(); }
    @Transactional(readOnly = true)
    public Optional<Inventory> getById(Long id)             { return inventoryRepo.findById(id); }
    @Transactional(readOnly = true)
    public List<Inventory> search(String name)              { return inventoryRepo.findByMedicineNameContainingIgnoreCase(name); }
    @Transactional(readOnly = true)
    public List<Inventory> getLowStock()                    { return inventoryRepo.findLowStock(); }
    @Transactional(readOnly = true)
    public List<Inventory> getExpired()                     { return inventoryRepo.findExpired(); }
    @Transactional(readOnly = true)
    public List<Inventory> getExpiringSoon()                { return inventoryRepo.findExpiringSoon(LocalDate.now().plusDays(30)); }
    
    @Transactional
    public Inventory save(Inventory item)                   { return inventoryRepo.save(item); }
    
    @Transactional
    public Inventory update(Long id, Inventory updated) {
        Inventory existing = inventoryRepo.findById(id).orElseThrow();
        existing.setMedicineName(updated.getMedicineName());
        existing.setBatchNumber(updated.getBatchNumber());
        existing.setQuantity(updated.getQuantity());
        existing.setUnit(updated.getUnit());
        existing.setSupplier(updated.getSupplier());
        existing.setExpiryDate(updated.getExpiryDate());
        existing.setReorderLevel(updated.getReorderLevel());
        existing.setPurchasePrice(updated.getPurchasePrice());
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setCategory(updated.getCategory());
        return inventoryRepo.save(existing);
    }
    
    @Transactional
    public void delete(Long id) { inventoryRepo.deleteById(id); }
}

// -- UserService ----------------------------------------------------------------
@Service
class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    UserService(UserRepository userRepo, PasswordEncoder encoder) {
        this.userRepo = userRepo;
        this.encoder  = encoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAll()              { return userRepo.findAll(); }
    @Transactional(readOnly = true)
    public Optional<User> getById(Long id)  { return userRepo.findById(id); }
    @Transactional(readOnly = true)
    public List<User> getDoctors()          { return userRepo.findByRole(User.Role.DOCTOR); }

    @Transactional
    public User create(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return userRepo.save(user);
    }

    @Transactional
    public User update(Long id, User updated) {
        User existing = userRepo.findById(id).orElseThrow();
        existing.setFullName(updated.getFullName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setRole(updated.getRole());
        existing.setActive(updated.isActive());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(encoder.encode(updated.getPassword()));
        }
        return userRepo.save(existing);
    }

    @Transactional
    public void delete(Long id) { userRepo.deleteById(id); }
}

// -- Public ServiceFacade (accessed by controllers) ----------------------------
@Service
public class ServiceFacade {

    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final InventoryService inventoryService;
    private final UserService userService;
    private final AiLogRepository aiLogRepo;
    private final PatientRepository patientRepo;
    private final UserRepository userRepo;

    public ServiceFacade(PatientService p, AppointmentService a, InventoryService i,
                         UserService u, AiLogRepository aiLogRepo,
                         PatientRepository patientRepo, UserRepository userRepo) {
        this.patientService     = p;
        this.appointmentService = a;
        this.inventoryService   = i;
        this.userService        = u;
        this.aiLogRepo          = aiLogRepo;
        this.patientRepo        = patientRepo;
        this.userRepo           = userRepo;
    }

    // Patients
    public List<Patient>       getAllPatients()                           { return patientService.getAll(); }
    public Optional<Patient>   getPatientById(Long id)                   { return patientService.getById(id); }
    public List<Patient>       searchPatients(String name)               { return patientService.searchByName(name); }
    public List<Patient>       getPatientsByStatus(Patient.Status s)     { return patientService.getByStatus(s); }
    public Patient             savePatient(Patient p, Long docId)        { return patientService.save(p, docId); }
    public Patient             updatePatient(Long id, Patient p, Long d) { return patientService.update(id, p, d); }
    public void                dischargePatient(Long id)                 { patientService.discharge(id); }
    public void                deletePatient(Long id)                    { patientService.delete(id); }
    public long                countAdmitted()                           { return patientService.countAdmitted(); }

    // Appointments
    public List<Appointment>   getAllAppointments()                               { return appointmentService.getAll(); }
    public Optional<Appointment> getAppointmentById(Long id)                     { return appointmentService.getById(id); }
    public List<Appointment>   getAppointmentsByPatient(Long pid)                 { return appointmentService.getByPatient(pid); }
    public List<Appointment>   getAppointmentsByDoctor(Long did)                  { return appointmentService.getByDoctor(did); }
    public List<Appointment>   getTodayAppointments()                             { return appointmentService.getToday(); }
    public Appointment         saveAppointment(Appointment a, Long pid, Long did, Long uid) { return appointmentService.save(a, pid, did, uid); }
    public Appointment         updateAppointment(Long id, Appointment a, Long pid, Long did){ return appointmentService.update(id, a, pid, did); }
    public Appointment         updateAppointmentStatus(Long id, Appointment.Status s)       { return appointmentService.updateStatus(id, s); }
    public void                deleteAppointment(Long id)                         { appointmentService.delete(id); }
    public long                countScheduledToday()                              { return appointmentService.countScheduledToday(); }

    // Inventory
    public List<Inventory>     getAllInventory()          { return inventoryService.getAll(); }
    public Optional<Inventory> getInventoryById(Long id) { return inventoryService.getById(id); }
    public List<Inventory>     searchInventory(String n) { return inventoryService.search(n); }
    public List<Inventory>     getLowStock()             { return inventoryService.getLowStock(); }
    public List<Inventory>     getExpired()              { return inventoryService.getExpired(); }
    public List<Inventory>     getExpiringSoon()         { return inventoryService.getExpiringSoon(); }
    public Inventory           saveInventory(Inventory i){ return inventoryService.save(i); }
    public Inventory           updateInventory(Long id, Inventory i) { return inventoryService.update(id, i); }
    public void                deleteInventory(Long id)  { inventoryService.delete(id); }

    // Users
    public List<User>          getAllUsers()              { return userService.getAll(); }
    public Optional<User>      getUserById(Long id)      { return userService.getById(id); }
    public List<User>          getDoctors()              { return userService.getDoctors(); }
    public User                createUser(User u)        { return userService.create(u); }
    public User                updateUser(Long id, User u){ return userService.update(id, u); }
    public void                deleteUser(Long id)       { userService.delete(id); }

    // AI Logs
    @Transactional
    public AiLog saveAiLog(AiLog log) { return aiLogRepo.save(log); }
    
    @Transactional(readOnly = true)
    public Optional<Patient> findPatientById(Long id) { return patientRepo.findById(id); }
    
    @Transactional(readOnly = true)
    public Optional<User> findUserByUsername(String username) { return userRepo.findByUsername(username); }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalPatients",      patientRepo.count());
        stats.put("admittedPatients",   patientService.countAdmitted());
        stats.put("todayAppointments",  appointmentService.countScheduledToday());
        stats.put("lowStockItems",      inventoryService.getLowStock().size());
        stats.put("expiredItems",       inventoryService.getExpired().size());
        stats.put("totalInventory",     inventoryService.getAll().size());
        return stats;
    }
}
