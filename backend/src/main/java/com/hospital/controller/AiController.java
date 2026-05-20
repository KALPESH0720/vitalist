package com.hospital.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.model.*;
import com.hospital.service.ServiceFacade;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";
    private static String MODEL = "llama3.2";

    private final ServiceFacade facade;
    private final RestTemplate  restTemplate;
    private final ObjectMapper  mapper;

    public AiController(ServiceFacade facade) {
        this.facade       = facade;
        this.restTemplate = new RestTemplate();
        this.mapper       = new ObjectMapper();
    }

    // -- Settings & Health ----------------------------------------------------

    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(@RequestBody Map<String, String> body) {
        if (body.containsKey("model") && !body.get("model").isBlank())
            MODEL = body.get("model").trim();
        return ResponseEntity.ok(Map.of("status", "updated", "model", MODEL));
    }

    @GetMapping("/settings")
    public ResponseEntity<Map<String, Object>> getSettings() {
        return ResponseEntity.ok(Map.of(
            "model", MODEL,
            "ollamaUrl", OLLAMA_URL,
            "ollamaRunning", isOllamaRunning(),
            "availableModels", List.of(
                "llama3.2", "llama3.1", "llama3",
                "mistral", "phi3", "gemma2",
                "llava", "llama3.2-vision"
            )
        ));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean running = isOllamaRunning();
        return ResponseEntity.ok(Map.of(
            "ollama", running ? "RUNNING" : "OFFLINE",
            "model",  MODEL,
            "message", running
                ? "Ollama is running. AI features available."
                : "Ollama offline. Run: ollama serve   then: ollama pull " + MODEL
        ));
    }

    // -- CaseTwin: Clinical profile extraction --------------------------------

    @PostMapping("/casetwin/extract-profile")
    public ResponseEntity<Map<String, Object>> extractProfile(@RequestBody Map<String, Object> body) {
        String notes     = body.getOrDefault("clinicalNotes", "").toString();
        Long   patientId = parseLongSafe(body.get("patientId"));
        String prompt = "Parse these clinical notes into a JSON CaseProfile with fields: "
            + "demographics, chief_complaint, vitals, history, impression, assessment, plan, "
            + "profile_completeness (0-100). Return ONLY valid JSON.\n\nNotes: " + notes;
        String result = callOllama(prompt);
        logAi(AiLog.Feature.CASE_TWIN, patientId, "Profile extraction", result);
        return ResponseEntity.ok(Map.of("success", true, "profile", result, "feature", "CaseTwin"));
    }

    // -- CaseTwin: CXR annotation ---------------------------------------------

    @PostMapping("/casetwin/annotate-cxr")
    public ResponseEntity<Map<String, Object>> annotateCXR(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "context", defaultValue = "") String ctx,
            @RequestParam(value = "patientId", required = false) Long patientId) {
        String prompt = "Analyse this chest X-ray. Context: " + ctx
            + ". Return ONLY JSON with: primary_finding, bounding_region, "
            + "abnormalities (array), imaging_quality, clinical_summary.";
        String result;
        try { result = callOllamaWithImage(prompt, image.getBytes()); }
        catch (Exception e) { result = "Image error: " + e.getMessage(); }
        logAi(AiLog.Feature.CASE_TWIN, patientId, "CXR annotation", result);
        return ResponseEntity.ok(Map.of("success", true, "annotation", result, "feature", "CaseTwin"));
    }

    // -- CaseTwin: Compare cases ----------------------------------------------

    @PostMapping("/casetwin/compare-cases")
    public ResponseEntity<Map<String, Object>> compareCases(@RequestBody Map<String, Object> body) {
        Long patientId = parseLongSafe(body.get("patientId"));
        String prompt = "Compare these two clinical cases. Return ONLY JSON with: "
            + "imaging_similarities (array), key_differences (array), "
            + "progression_risk (HIGH/MEDIUM/LOW), treatment_implications, "
            + "clinical_recommendations (array).\n\nCurrent: "
            + body.getOrDefault("currentCase", "")
            + "\n\nHistorical Twin: " + body.getOrDefault("historicalTwin", "");
        String result = callOllama(prompt);
        logAi(AiLog.Feature.CASE_TWIN, patientId, "Case comparison", result);
        return ResponseEntity.ok(Map.of("success", true, "comparison", result, "feature", "CaseTwin"));
    }

    // -- CaseTwin: Generate referral ------------------------------------------

    @PostMapping("/casetwin/generate-referral")
    public ResponseEntity<Map<String, Object>> generateReferral(@RequestBody Map<String, Object> body) {
        Long patientId = parseLongSafe(body.get("patientId"));
        String prompt = "Write a professional clinical referral letter. Include: patient summary, "
            + "reason for referral, key findings, urgency, requested evaluation.\n\n"
            + "Patient: " + body.getOrDefault("patientProfile", "")
            + "\nSpecialist: " + body.getOrDefault("specialist", "Specialist");
        String result = callOllama(prompt);
        logAi(AiLog.Feature.CASE_TWIN, patientId, "Referral", result);
        return ResponseEntity.ok(Map.of("success", true, "referralMemo", result, "feature", "CaseTwin"));
    }

    // -- UniRad3s: SPOT -------------------------------------------------------

    @PostMapping("/unirad3s/spot")
    public ResponseEntity<Map<String, Object>> spot(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "modality", defaultValue = "FLAIR") String modality,
            @RequestParam(value = "patientId", required = false) Long patientId) {
        String prompt = "Analyse this " + modality + " neuroimaging slice. "
            + "Return ONLY JSON: {\"diagnosis\":\"TUMOR or NORMAL\","
            + "\"confidence_score\":0.0,\"anomaly_present\":true,"
            + "\"most_evident_region\":\"\",\"priority\":\"HIGH/MEDIUM/LOW\","
            + "\"findings\":\"\",\"recommended_modality\":\"\"}";
        String result;
        try { result = callOllamaWithImage(prompt, image.getBytes()); }
        catch (Exception e) { result = "Error: " + e.getMessage(); }
        logAi(AiLog.Feature.XRAY_ANALYSIS, patientId, "SPOT-" + modality, result);
        return ResponseEntity.ok(Map.of("success", true, "spotResult", result, "feature", "UniRad3s-SPOT"));
    }

    // -- UniRad3s: SEGMENT ----------------------------------------------------

    @PostMapping("/unirad3s/segment")
    public ResponseEntity<Map<String, Object>> segment(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "lesionType", defaultValue = "") String lesionType,
            @RequestParam(value = "region",     defaultValue = "") String region,
            @RequestParam(value = "patientId",  required = false) Long patientId) {
        String prompt = "Identify lesion boundaries in this image. Lesion: " + lesionType
            + ", Region: " + region + ". Return ONLY JSON: {\"lesion_detected\":true,"
            + "\"estimated_location\":\"\",\"bounding_box_guidance\":\"\","
            + "\"estimated_size\":\"small/medium/large\",\"shape\":\"\","
            + "\"margin\":\"well-defined/ill-defined\","
            + "\"segmentation_confidence\":\"HIGH/MEDIUM/LOW\",\"volume_estimate\":\"\"}";
        String result;
        try { result = callOllamaWithImage(prompt, image.getBytes()); }
        catch (Exception e) { result = "Error: " + e.getMessage(); }
        logAi(AiLog.Feature.XRAY_ANALYSIS, patientId, "SEGMENT-" + lesionType, result);
        return ResponseEntity.ok(Map.of("success", true, "segmentResult", result, "feature", "UniRad3s-SEGMENT"));
    }

    // -- UniRad3s: SIMPLIFY clinical ------------------------------------------

    @PostMapping("/unirad3s/simplify/clinical")
    public ResponseEntity<Map<String, Object>> simplifyClinical(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "findings", defaultValue = "") String findings,
            @RequestParam(value = "context",  defaultValue = "") String context,
            @RequestParam(value = "patientId", required = false) Long patientId) {
        String prompt = "Generate a structured clinical radiology report. Findings: " + findings
            + ". Context: " + context
            + ". Return ONLY JSON: {\"report_type\":\"\",\"technique\":\"\","
            + "\"clinical_indication\":\"\",\"findings\":{\"primary\":\"\","
            + "\"secondary\":\"\",\"other\":\"\"},\"impression\":\"\","
            + "\"recommendation\":\"\","
            + "\"reporting_note\":\"AI-Assisted. Requires Radiologist Verification.\"}";
        String result;
        try { result = callOllamaWithImage(prompt, image.getBytes()); }
        catch (Exception e) { result = "Error: " + e.getMessage(); }
        logAi(AiLog.Feature.XRAY_ANALYSIS, patientId, "SIMPLIFY-clinical", result);
        return ResponseEntity.ok(Map.of("success", true, "clinicalReport", result, "feature", "UniRad3s-SIMPLIFY"));
    }

    // -- UniRad3s: SIMPLIFY patient summary -----------------------------------

    @PostMapping("/unirad3s/simplify/patient")
    public ResponseEntity<Map<String, Object>> simplifyPatient(@RequestBody Map<String, Object> body) {
        Long   patientId = parseLongSafe(body.get("patientId"));
        String report    = body.getOrDefault("clinicalReport", "").toString();
        String prompt = "Convert this clinical report into a clear patient-friendly summary. "
            + "Report: " + report
            + ". Return ONLY JSON: {\"plain_summary\":\"\",\"what_was_found\":\"\","
            + "\"what_it_means\":\"\",\"next_steps\":[],\"questions_to_ask_doctor\":[],"
            + "\"reassurance\":\"\"}";
        String result = callOllama(prompt);
        logAi(AiLog.Feature.XRAY_ANALYSIS, patientId, "SIMPLIFY-patient", result);
        return ResponseEntity.ok(Map.of("success", true, "patientSummary", result, "feature", "UniRad3s-PatientSummary"));
    }

    // -- Ollama core helpers --------------------------------------------------

    private String callOllama(String prompt) {
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> req = new HashMap<>();
            req.put("model",  MODEL);
            req.put("prompt", prompt);
            req.put("stream", false);
            req.put("options", Map.of("temperature", 0.1, "num_predict", 1024));
            ResponseEntity<String> resp = restTemplate.postForEntity(
                OLLAMA_URL, new HttpEntity<>(req, h), String.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode node = mapper.readTree(resp.getBody());
                return node.path("response").asText("No response");
            }
            return "Ollama error: " + resp.getStatusCode();
        } catch (Exception e) {
            return "Ollama offline or error: " + e.getMessage()
                + "\nFix: run  ollama serve  then  ollama pull " + MODEL;
        }
    }

    private String callOllamaWithImage(String prompt, byte[] imageBytes) {
        try {
            String b64 = Base64.getEncoder().encodeToString(imageBytes);
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            Map<String, Object> req = new HashMap<>();
            req.put("model",  MODEL);
            req.put("prompt", prompt);
            req.put("images", List.of(b64));
            req.put("stream", false);
            req.put("options", Map.of("temperature", 0.1, "num_predict", 1024));
            ResponseEntity<String> resp = restTemplate.postForEntity(
                OLLAMA_URL, new HttpEntity<>(req, h), String.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                JsonNode node = mapper.readTree(resp.getBody());
                return node.path("response").asText("No response");
            }
            return "Ollama error: " + resp.getStatusCode();
        } catch (Exception e) {
            // fallback to text-only if vision not supported
            return callOllama(prompt + "\n[Note: vision model required for image analysis. "
                + "Pull with: ollama pull llava  or  ollama pull llama3.2-vision]");
        }
    }

    private boolean isOllamaRunning() {
        try {
            restTemplate.getForEntity("http://localhost:11434/api/tags", String.class);
            return true;
        } catch (Exception e) { return false; }
    }

    // -- AI log ---------------------------------------------------------------

    private void logAi(AiLog.Feature feature, Long patientId, String summary, String result) {
        try {
            AiLog log = new AiLog();
            log.setFeature(feature);
            log.setInputSummary(summary);
            log.setResult(result);
            if (patientId != null)
                facade.findPatientById(patientId).ifPresent(log::setPatient);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null)
                facade.findUserByUsername(auth.getName()).ifPresent(log::setPerformedBy);
            facade.saveAiLog(log);
        } catch (Exception ignored) {}
    }

    private static Long parseLongSafe(Object val) {
        if (val == null) return null;
        String s = val.toString().trim();
        if (s.isBlank()) return null;
        try { return Long.parseLong(s); } catch (NumberFormatException e) { return null; }
    }
}
