package com.hospital.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hospital.client.model.Session;
import okhttp3.*;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ApiClient {

    public static final String BASE = "http://localhost:8081/api";
    private static final MediaType JSON = MediaType.get("application/json");
    public static final ObjectMapper MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build();

    // -- Auth ------------------------------------------------------------------
    public static JsonNode login(String username, String password) throws IOException {
        // Plain JSON POST - no auth header needed, controller validates manually
        RequestBody rb = RequestBody.create(
            MAPPER.writeValueAsString(Map.of("username", username, "password", password)), JSON);
        Request req = new Request.Builder()
            .url(BASE + "/auth/login")
            .post(rb)
            .build();
        try (Response r = HTTP.newCall(req).execute()) {
            String body = r.body() != null ? r.body().string() : "{}";
            return MAPPER.readTree(body);
        }
    }

    // -- Dashboard -------------------------------------------------------------
    public static JsonNode getDashboardStats() throws IOException { return get("/dashboard/stats"); }

    // -- Patients --------------------------------------------------------------
    public static JsonNode getPatients()                   throws IOException { return get("/patients"); }
    public static JsonNode searchPatients(String q)        throws IOException { return get("/patients?search=" + encode(q)); }
    public static JsonNode getPatient(Long id)             throws IOException { return get("/patients/" + id); }
    public static JsonNode createPatient(Map<String,Object> body) throws IOException { return post("/patients", body, true); }
    public static JsonNode updatePatient(Long id, Map<String,Object> body) throws IOException { return put("/patients/" + id, body); }
    public static JsonNode dischargePatient(Long id)       throws IOException { return post("/patients/" + id + "/discharge", Map.of(), true); }
    public static JsonNode deletePatient(Long id)          throws IOException { return delete("/patients/" + id); }

    // -- Appointments ----------------------------------------------------------
    public static JsonNode getAppointments()               throws IOException { return get("/appointments"); }
    public static JsonNode getTodayAppointments()          throws IOException { return get("/appointments?scope=today"); }
    public static JsonNode getPatientAppointments(Long pid)throws IOException { return get("/appointments?patientId=" + pid); }
    public static JsonNode createAppointment(Map<String,Object> b) throws IOException { return post("/appointments", b, true); }
    public static JsonNode updateAppointment(Long id, Map<String,Object> b) throws IOException { return put("/appointments/" + id, b); }
    public static JsonNode updateAppointmentStatus(Long id, String status) throws IOException {
        return put("/appointments/" + id + "/status", Map.of("status", status));
    }
    public static JsonNode deleteAppointment(Long id)      throws IOException { return delete("/appointments/" + id); }

    // -- Inventory -------------------------------------------------------------
    public static JsonNode getInventory()                  throws IOException { return get("/inventory"); }
    public static JsonNode searchInventory(String q)       throws IOException { return get("/inventory?search=" + encode(q)); }
    public static JsonNode getLowStock()                   throws IOException { return get("/inventory?filter=low"); }
    public static JsonNode createInventory(Map<String,Object> b) throws IOException { return post("/inventory", b, true); }
    public static JsonNode updateInventory(Long id, Map<String,Object> b) throws IOException { return put("/inventory/" + id, b); }
    public static JsonNode deleteInventory(Long id)        throws IOException { return delete("/inventory/" + id); }

    // -- Users -----------------------------------------------------------------
    public static JsonNode getUsers()                      throws IOException { return get("/users"); }
    public static JsonNode getDoctors()                    throws IOException { return get("/users/doctors"); }
    public static JsonNode createUser(Map<String,Object> b) throws IOException { return post("/users", b, true); }
    public static JsonNode updateUser(Long id, Map<String,Object> b) throws IOException { return put("/users/" + id, b); }
    public static JsonNode deleteUser(Long id)             throws IOException { return delete("/users/" + id); }

    // -- AI --------------------------------------------------------------------
    public static JsonNode getAiSettings() throws IOException {
        return get("/ai/settings");
    }
    public static JsonNode getAiHealth() throws IOException {
        return get("/ai/health");
    }
    public static JsonNode setAiModel(String model) throws IOException {
        return post("/ai/settings", Map.of("model", model), true);
    }
    public static JsonNode extractClinicalProfile(Map<String,Object> b) throws IOException {
        return post("/ai/casetwin/extract-profile", b, true);
    }
    public static JsonNode compareCases(Map<String,Object> b) throws IOException {
        return post("/ai/casetwin/compare-cases", b, true);
    }
    public static JsonNode generateReferral(Map<String,Object> b) throws IOException {
        return post("/ai/casetwin/generate-referral", b, true);
    }
    public static JsonNode annotateCXR(File image, String context, Long patientId) throws IOException {
        return postMultipart("/ai/casetwin/annotate-cxr",
            Map.of("context", context, "patientId", patientId != null ? patientId.toString() : ""), image, "image");
    }
    public static JsonNode spotAnalysis(File image, String modality, Long patientId) throws IOException {
        return postMultipart("/ai/unirad3s/spot",
            Map.of("modality", modality, "patientId", patientId != null ? patientId.toString() : ""), image, "image");
    }
    public static JsonNode segmentAnalysis(File image, String lesionType, String region, Long patientId) throws IOException {
        return postMultipart("/ai/unirad3s/segment",
            Map.of("lesionType", lesionType, "region", region, "patientId", patientId != null ? patientId.toString() : ""), image, "image");
    }
    public static JsonNode clinicalReport(File image, String findings, String context, Long patientId) throws IOException {
        return postMultipart("/ai/unirad3s/simplify/clinical",
            Map.of("findings", findings, "context", context, "patientId", patientId != null ? patientId.toString() : ""), image, "image");
    }
    public static JsonNode patientSummary(Map<String,Object> b) throws IOException {
        return post("/ai/unirad3s/simplify/patient", b, true);
    }

    // -- Helpers ---------------------------------------------------------------
    private static JsonNode get(String path) throws IOException {
        Request req = new Request.Builder()
            .url(BASE + path)
            .header("Authorization", "Basic " + Session.get().getBasicToken())
            .get().build();
        try (Response r = HTTP.newCall(req).execute()) {
            return MAPPER.readTree(r.body() != null ? r.body().string() : "{}");
        }
    }

    private static JsonNode post(String path, Object body, boolean auth) throws IOException {
        RequestBody rb = RequestBody.create(MAPPER.writeValueAsString(body), JSON);
        Request.Builder b = new Request.Builder().url(BASE + path).post(rb);
        if (auth && Session.get().isLoggedIn())
            b.header("Authorization", "Basic " + Session.get().getBasicToken());
        try (Response r = HTTP.newCall(b.build()).execute()) {
            return MAPPER.readTree(r.body() != null ? r.body().string() : "{}");
        }
    }

    private static JsonNode put(String path, Object body) throws IOException {
        RequestBody rb = RequestBody.create(MAPPER.writeValueAsString(body), JSON);
        Request req = new Request.Builder()
            .url(BASE + path)
            .header("Authorization", "Basic " + Session.get().getBasicToken())
            .put(rb).build();
        try (Response r = HTTP.newCall(req).execute()) {
            return MAPPER.readTree(r.body() != null ? r.body().string() : "{}");
        }
    }

    private static JsonNode delete(String path) throws IOException {
        Request req = new Request.Builder()
            .url(BASE + path)
            .header("Authorization", "Basic " + Session.get().getBasicToken())
            .delete().build();
        try (Response r = HTTP.newCall(req).execute()) {
            return MAPPER.readTree(r.body() != null ? r.body().string() : "{}");
        }
    }

    private static JsonNode postMultipart(String path, Map<String,String> params, File file, String fileField) throws IOException {
        MultipartBody.Builder mb = new MultipartBody.Builder().setType(MultipartBody.FORM);
        params.forEach(mb::addFormDataPart);
        mb.addFormDataPart(fileField, file.getName(),
            RequestBody.create(file, MediaType.parse("application/octet-stream")));
        Request req = new Request.Builder()
            .url(BASE + path)
            .header("Authorization", "Basic " + Session.get().getBasicToken())
            .post(mb.build()).build();
        try (Response r = HTTP.newCall(req).execute()) {
            return MAPPER.readTree(r.body() != null ? r.body().string() : "{}");
        }
    }

    public static boolean isBackendReachable() {
        try {
            Request req = new Request.Builder()
                .url(BASE.replace("/api", "") + "/actuator/health")
                .get().build();
            try (Response r = HTTP.newCall(req).execute()) {
                return r.code() < 500;
            }
        } catch (Exception e) { return false; }
    }

    private static String encode(String s) {
        try { return java.net.URLEncoder.encode(s, "UTF-8"); } catch (Exception e) { return s; }
    }

    private ApiClient() {}
}
