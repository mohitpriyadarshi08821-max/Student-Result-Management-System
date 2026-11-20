package result.database;

import result.dao.InMemoryDataStore;
import result.model.Result;
import result.model.Student;
import result.model.Subject;
import result.ui.MainApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Universal Data Synchronization Bridge connecting Desktop App (Native JVM + MySQL)
 * and Web App (CheerpJ WebAssembly + Node.js server) so that student data, subjects,
 * and exam marks are 100% identical and synchronized bi-directionally in real-time.
 */
public class DataSyncBridge {

    public interface SyncListener {
        void onDataSynchronized();
    }

    private static final String DATA_FILE_NAME = "srms_data.json";
    private static final List<SyncListener> syncListeners = new CopyOnWriteArrayList<>();

    private static volatile boolean initialSyncDone = false;
    private static volatile long lastKnownDataHash = 0L;
    private static volatile long lastKnownDiskTimestamp = 0L;
    private static volatile boolean pollerStarted = false;

    public static void addSyncListener(SyncListener listener) {
        if (listener != null && !syncListeners.contains(listener)) {
            syncListeners.add(listener);
        }
    }

    public static void removeSyncListener(SyncListener listener) {
        if (listener != null) {
            syncListeners.remove(listener);
        }
    }

    private static void notifyListeners() {
        for (SyncListener listener : syncListeners) {
            try {
                listener.onDataSynchronized();
            } catch (Throwable ignored) {}
        }
    }

    /**
     * Entry point on application startup.
     */
    public static synchronized void initializeSync() {
        if (initialSyncDone) return;

        if (MainApplication.isBrowserEnvironment()) {
            // Web browser mode: fetch srms_data.json in background so main thread starts immediately
            new Thread(() -> {
                try {
                    if (!loadFromDisk()) {
                        loadFromWebServer();
                    }
                } catch (Throwable ignored) {}
            }, "SRMS-WebInitSync").start();
        } else {
            // Desktop mode:
            // 1. Verify schema tables
            try {
                DatabaseInitializer.initializeDatabase();
            } catch (Throwable ignored) {}
            // 2. Synchronize desktop MySQL database and disk snapshots
            syncDesktopEnvironment();
        }

        initialSyncDone = true;
        startPollerDaemon();
    }

    private static synchronized void startPollerDaemon() {
        if (pollerStarted) return;
        pollerStarted = true;

        Thread poller = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(2000);
                    boolean changed = pullLatestData();
                    if (changed) {
                        notifyListeners();
                    }
                } catch (InterruptedException e) {
                    break;
                } catch (Throwable ignored) {}
            }
        }, "SRMS-LiveSyncPoller");
        poller.setDaemon(true);
        poller.start();
    }

    private static void syncDesktopEnvironment() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            int count = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM students")) {
                if (rs.next()) count = rs.getInt(1);
            }

            if (count > 0) {
                // Live MySQL records exist: export canonical snapshot to disk
                exportMySQLToDisk(conn);
            } else {
                // MySQL is empty: load reference snapshot from disk into InMemoryDataStore and populate MySQL
                if (loadFromDisk()) {
                    syncDiskToMySQL(conn);
                } else {
                    exportInMemoryToDisk();
                    syncDiskToMySQL(conn);
                }
            }
        } catch (Throwable t) {
            // MySQL unavailable: load from disk file into InMemoryDataStore
            loadFromDisk();
        }
    }

    /**
     * Actively pulls latest data from server (in browser mode) or disk/MySQL (in desktop mode).
     * Returns true if data changed and views need to refresh.
     */
    public static synchronized boolean pullLatestData() {
        if (MainApplication.isBrowserEnvironment()) {
            return loadFromWebServer();
        } else {
            // Desktop mode: check if web server or external process modified srms_data.json
            File f = findCandidateDiskFile();
            if (f != null && f.exists() && f.lastModified() > lastKnownDiskTimestamp) {
                lastKnownDiskTimestamp = f.lastModified();
                boolean loaded = loadFromDisk();
                if (loaded) {
                    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                        syncDiskToMySQL(conn);
                    } catch (Throwable ignored) {}
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Reads all data from MySQL and writes to web/srms_data.json and srms_data.json.
     */
    public static synchronized void exportMySQLToDisk(Connection conn) {
        try {
            List<Student> students = new ArrayList<>();
            List<Subject> subjects = new ArrayList<>();
            List<Result> results = new ArrayList<>();

            // 1. Students
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT student_id, roll_number, first_name, last_name, email, course, semester, gender, dob FROM students ORDER BY student_id ASC")) {
                while (rs.next()) {
                    Date dob = rs.getDate("dob");
                    students.add(new Student(
                            rs.getInt("student_id"),
                            rs.getString("roll_number"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("email"),
                            rs.getString("course"),
                            rs.getInt("semester"),
                            rs.getString("gender"),
                            dob != null ? dob.toLocalDate() : null
                    ));
                }
            }

            // 2. Subjects
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT subject_id, subject_code, subject_name, max_marks, pass_marks, credits FROM subjects ORDER BY subject_id ASC")) {
                while (rs.next()) {
                    int credits = 4;
                    try { credits = rs.getInt("credits"); } catch (Throwable ignored) {}
                    if (credits <= 0) credits = 4;
                    subjects.add(new Subject(
                            rs.getInt("subject_id"),
                            rs.getString("subject_code"),
                            rs.getString("subject_name"),
                            rs.getInt("max_marks"),
                            rs.getInt("pass_marks"),
                            credits
                    ));
                }
            }

            // 3. Results
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT result_id, student_id, subject_id, marks_obtained, grade FROM results ORDER BY result_id ASC")) {
                while (rs.next()) {
                    results.add(new Result(
                            rs.getInt("result_id"),
                            rs.getInt("student_id"),
                            rs.getInt("subject_id"),
                            rs.getDouble("marks_obtained"),
                            rs.getString("grade")
                    ));
                }
            }

            String json = serializeToJson(students, subjects, results);
            lastKnownDataHash = json.hashCode();
            writeJsonToDisk(json);

            // Update InMemoryDataStore snapshot
            InMemoryDataStore.getInstance().loadSnapshot(students, subjects, results);
        } catch (Throwable t) {
            System.err.println("[WARN] DataSyncBridge export notice: " + t.getMessage());
        }
    }

    public static synchronized void exportInMemoryToDisk() {
        InMemoryDataStore ds = InMemoryDataStore.getInstance();
        String json = serializeToJson(ds.getAllStudents(), ds.getAllSubjects(), ds.getAllResults());
        lastKnownDataHash = json.hashCode();
        writeJsonToDisk(json);
    }

    private static void writeJsonToDisk(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        File[] targetFiles = new File[]{
                new File("web/" + DATA_FILE_NAME),
                new File(DATA_FILE_NAME)
        };
        for (File f : targetFiles) {
            try {
                File parent = f.getParentFile();
                if (parent != null && !parent.exists()) parent.mkdirs();
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(bytes);
                }
                lastKnownDiskTimestamp = Math.max(lastKnownDiskTimestamp, f.lastModified());
            } catch (Throwable ignored) {}
        }
    }

    private static File findCandidateDiskFile() {
        File[] candidateFiles = new File[]{
                new File("web/" + DATA_FILE_NAME),
                new File(DATA_FILE_NAME),
                new File("/app/" + DATA_FILE_NAME),
                new File("/app/web/" + DATA_FILE_NAME)
        };
        for (File f : candidateFiles) {
            if (f.exists() && f.isFile() && f.length() > 0) {
                return f;
            }
        }
        return null;
    }

    /**
     * Loads student data from disk file into InMemoryDataStore.
     */
    public static synchronized boolean loadFromDisk() {
        File f = findCandidateDiskFile();
        if (f != null && f.exists() && f.isFile() && f.length() > 0) {
            try (FileInputStream fis = new FileInputStream(f)) {
                String json = readStreamToString(fis);
                long hash = json.hashCode();
                if (hash != lastKnownDataHash) {
                    lastKnownDataHash = hash;
                    lastKnownDiskTimestamp = f.lastModified();
                    return applyJsonToInMemory(json);
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Loads student data from local HTTP web server or static host in browser mode.
     */
    public static synchronized boolean loadFromWebServer() {
        String origin = System.getProperty("app.origin", "http://localhost:8080");
        if (origin.endsWith("/")) {
            origin = origin.substring(0, origin.length() - 1);
        }
        String[] urls = new String[]{
                origin + "/srms_data.json?t=" + System.currentTimeMillis(),
                origin + "/app/srms_data.json?t=" + System.currentTimeMillis(),
                origin + "/api/data?t=" + System.currentTimeMillis()
        };
        for (String urlStr : urls) {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(1500);
                conn.setReadTimeout(2000);
                conn.setUseCaches(false);
                conn.setRequestProperty("Cache-Control", "no-cache");
                if (conn.getResponseCode() == 200) {
                    try (InputStream is = conn.getInputStream()) {
                        String json = readStreamToString(is);
                        long hash = json.hashCode();
                        if (hash != lastKnownDataHash) {
                            lastKnownDataHash = hash;
                            return applyJsonToInMemory(json);
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    /**
     * Synchronizes a change to disk and notifies server / MySQL.
     */
    public static void notifyDataChanged() {
        if (MainApplication.isBrowserEnvironment()) {
            // Browser WebAssembly mode: send update to server via background thread
            new Thread(() -> {
                try {
                    InMemoryDataStore ds = InMemoryDataStore.getInstance();
                    String json = serializeToJson(ds.getAllStudents(), ds.getAllSubjects(), ds.getAllResults());
                    lastKnownDataHash = json.hashCode();

                    String origin = System.getProperty("app.origin", "http://localhost:8080");
                    if (origin.endsWith("/")) {
                        origin = origin.substring(0, origin.length() - 1);
                    }
                    URL url = new URL(origin + "/api/sync");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setConnectTimeout(1000);
                    conn.setReadTimeout(1500);
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/json");
                    try (OutputStream os = conn.getOutputStream()) {
                        os.write(json.getBytes(StandardCharsets.UTF_8));
                    }
                    conn.getResponseCode();
                } catch (Throwable ignored) {}
            }, "SRMS-WebSync").start();
        } else {
            // Desktop mode: write to disk and propagate to MySQL
            new Thread(() -> {
                try {
                    exportInMemoryToDisk();
                    try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
                        syncDiskToMySQL(conn);
                    } catch (Throwable ignored) {}
                } catch (Throwable ignored) {}
            }, "SRMS-DesktopSync").start();
        }
    }

    /**
     * Synchronizes memory / disk snapshot into MySQL, correctly handling insertions, updates,
     * and deletions of students, curriculum subjects, and examination results.
     */
    public static synchronized void syncDiskToMySQL(Connection conn) {
        try {
            InMemoryDataStore ds = InMemoryDataStore.getInstance();
            List<Student> students = ds.getAllStudents();
            List<Subject> subjects = ds.getAllSubjects();
            List<Result> results = ds.getAllResults();

            conn.setAutoCommit(false);

            // 1. Sync subjects:
            // Remove subjects deleted in snapshot
            Set<Integer> validSubIds = new HashSet<>();
            for (Subject s : subjects) validSubIds.add(s.getSubjectId());
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT subject_id FROM subjects")) {
                List<Integer> toDelete = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    if (!validSubIds.contains(id)) toDelete.add(id);
                }
                for (int id : toDelete) {
                    try (PreparedStatement del = conn.prepareStatement("DELETE FROM subjects WHERE subject_id = ?")) {
                        del.setInt(1, id);
                        del.executeUpdate();
                    }
                }
            }
            // Upsert subjects
            String sqlSub = "INSERT INTO subjects (subject_id, subject_code, subject_name, max_marks, pass_marks, credits) " +
                    "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE subject_name = VALUES(subject_name), " +
                    "max_marks = VALUES(max_marks), pass_marks = VALUES(pass_marks), credits = VALUES(credits)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlSub)) {
                for (Subject s : subjects) {
                    stmt.setInt(1, s.getSubjectId());
                    stmt.setString(2, s.getSubjectCode());
                    stmt.setString(3, s.getSubjectName());
                    stmt.setInt(4, s.getMaxMarks());
                    stmt.setInt(5, s.getPassMarks());
                    stmt.setInt(6, s.getCredits());
                    stmt.executeUpdate();
                }
            }

            // 2. Sync students:
            // Remove students deleted in snapshot
            Set<Integer> validStuIds = new HashSet<>();
            for (Student s : students) validStuIds.add(s.getStudentId());
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT student_id FROM students")) {
                List<Integer> toDelete = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    if (!validStuIds.contains(id)) toDelete.add(id);
                }
                for (int id : toDelete) {
                    try (PreparedStatement del = conn.prepareStatement("DELETE FROM students WHERE student_id = ?")) {
                        del.setInt(1, id);
                        del.executeUpdate();
                    }
                }
            }
            // Upsert students
            String sqlStu = "INSERT INTO students (student_id, roll_number, first_name, last_name, email, course, semester, gender, dob) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE roll_number = VALUES(roll_number), first_name = VALUES(first_name), " +
                    "last_name = VALUES(last_name), email = VALUES(email), course = VALUES(course), " +
                    "semester = VALUES(semester), gender = VALUES(gender), dob = VALUES(dob)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlStu)) {
                for (Student s : students) {
                    stmt.setInt(1, s.getStudentId());
                    stmt.setString(2, s.getRollNumber());
                    stmt.setString(3, s.getFirstName());
                    stmt.setString(4, s.getLastName());
                    stmt.setString(5, s.getEmail());
                    stmt.setString(6, s.getCourse());
                    stmt.setInt(7, s.getSemester());
                    stmt.setString(8, s.getGender());
                    if (s.getDateOfBirth() != null) {
                        stmt.setDate(9, Date.valueOf(s.getDateOfBirth()));
                    } else {
                        stmt.setDate(9, null);
                    }
                    stmt.executeUpdate();
                }
            }

            // 3. Sync results:
            // Remove results deleted in snapshot
            Set<Integer> validResIds = new HashSet<>();
            for (Result r : results) validResIds.add(r.getResultId());
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery("SELECT result_id FROM results")) {
                List<Integer> toDelete = new ArrayList<>();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    if (!validResIds.contains(id)) toDelete.add(id);
                }
                for (int id : toDelete) {
                    try (PreparedStatement del = conn.prepareStatement("DELETE FROM results WHERE result_id = ?")) {
                        del.setInt(1, id);
                        del.executeUpdate();
                    }
                }
            }
            // Upsert results
            String sqlRes = "INSERT INTO results (result_id, student_id, subject_id, marks_obtained, grade) " +
                    "VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE marks_obtained = VALUES(marks_obtained), grade = VALUES(grade)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlRes)) {
                for (Result r : results) {
                    stmt.setInt(1, r.getResultId());
                    stmt.setInt(2, r.getStudentId());
                    stmt.setInt(3, r.getSubjectId());
                    stmt.setDouble(4, r.getMarksObtained());
                    stmt.setString(5, r.getGrade());
                    stmt.executeUpdate();
                }
            }

            conn.commit();
            exportMySQLToDisk(conn);
        } catch (Throwable t) {
            try { conn.rollback(); } catch (Throwable ignored) {}
            System.err.println("[WARN] syncDiskToMySQL notice: " + t.getMessage());
        }
    }

    // ==================== JSON Serialization & Snapshot Parser ====================

    public static String serializeToJson(List<Student> students, List<Subject> subjects, List<Result> results) {
        StringBuilder sb = new StringBuilder(8192);
        sb.append("{\n");

        // Students array
        sb.append("  \"students\": [\n");
        for (int i = 0; i < students.size(); i++) {
            Student s = students.get(i);
            sb.append("    {");
            sb.append("\"studentId\":").append(s.getStudentId()).append(",");
            sb.append("\"rollNumber\":\"").append(escapeJson(s.getRollNumber())).append("\",");
            sb.append("\"firstName\":\"").append(escapeJson(s.getFirstName())).append("\",");
            sb.append("\"lastName\":\"").append(escapeJson(s.getLastName())).append("\",");
            sb.append("\"email\":\"").append(escapeJson(s.getEmail())).append("\",");
            sb.append("\"course\":\"").append(escapeJson(s.getCourse())).append("\",");
            sb.append("\"semester\":").append(s.getSemester()).append(",");
            sb.append("\"gender\":\"").append(escapeJson(s.getGender())).append("\",");
            sb.append("\"dob\":\"").append(s.getDateOfBirth() != null ? s.getDateOfBirth().toString() : "").append("\"");
            sb.append("}").append(i < students.size() - 1 ? "," : "").append("\n");
        }
        sb.append("  ],\n");

        // Subjects array
        sb.append("  \"subjects\": [\n");
        for (int i = 0; i < subjects.size(); i++) {
            Subject s = subjects.get(i);
            sb.append("    {");
            sb.append("\"subjectId\":").append(s.getSubjectId()).append(",");
            sb.append("\"subjectCode\":\"").append(escapeJson(s.getSubjectCode())).append("\",");
            sb.append("\"subjectName\":\"").append(escapeJson(s.getSubjectName())).append("\",");
            sb.append("\"maxMarks\":").append(s.getMaxMarks()).append(",");
            sb.append("\"passMarks\":").append(s.getPassMarks()).append(",");
            sb.append("\"credits\":").append(s.getCredits());
            sb.append("}").append(i < subjects.size() - 1 ? "," : "").append("\n");
        }
        sb.append("  ],\n");

        // Results array
        sb.append("  \"results\": [\n");
        for (int i = 0; i < results.size(); i++) {
            Result r = results.get(i);
            sb.append("    {");
            sb.append("\"resultId\":").append(r.getResultId()).append(",");
            sb.append("\"studentId\":").append(r.getStudentId()).append(",");
            sb.append("\"subjectId\":").append(r.getSubjectId()).append(",");
            sb.append("\"marksObtained\":").append(r.getMarksObtained()).append(",");
            sb.append("\"grade\":\"").append(escapeJson(r.getGrade())).append("\"");
            sb.append("}").append(i < results.size() - 1 ? "," : "").append("\n");
        }
        sb.append("  ]\n");

        sb.append("}");
        return sb.toString();
    }

    public static boolean applyJsonToInMemory(String json) {
        if (json == null || json.trim().isEmpty()) return false;
        try {
            List<Student> studentsList = new ArrayList<>();
            List<Subject> subjectsList = new ArrayList<>();
            List<Result> resultsList = new ArrayList<>();

            // Parse students
            List<String> studentObjects = extractJsonArrayObjects(json, "students");
            for (String obj : studentObjects) {
                int id = getIntField(obj, "studentId");
                String roll = getStringField(obj, "rollNumber");
                String first = getStringField(obj, "firstName");
                String last = getStringField(obj, "lastName");
                String email = getStringField(obj, "email");
                String course = getStringField(obj, "course");
                int sem = getIntField(obj, "semester");
                String gender = getStringField(obj, "gender");
                String dobStr = getStringField(obj, "dob");
                LocalDate dob = null;
                if (!dobStr.isEmpty()) {
                    try { dob = LocalDate.parse(dobStr); } catch (Throwable ignored) {}
                }
                studentsList.add(new Student(id, roll, first, last, email, course, sem, gender, dob));
            }

            // Parse subjects
            List<String> subjectObjects = extractJsonArrayObjects(json, "subjects");
            for (String obj : subjectObjects) {
                int id = getIntField(obj, "subjectId");
                String code = getStringField(obj, "subjectCode");
                String name = getStringField(obj, "subjectName");
                int max = getIntField(obj, "maxMarks");
                int pass = getIntField(obj, "passMarks");
                int credits = getIntField(obj, "credits");
                if (credits <= 0) credits = 4;
                subjectsList.add(new Subject(id, code, name, max, pass, credits));
            }

            // Parse results
            List<String> resultObjects = extractJsonArrayObjects(json, "results");
            for (String obj : resultObjects) {
                int id = getIntField(obj, "resultId");
                int stuId = getIntField(obj, "studentId");
                int subId = getIntField(obj, "subjectId");
                double marks = getDoubleField(obj, "marksObtained");
                String grade = getStringField(obj, "grade");
                resultsList.add(new Result(id, stuId, subId, marks, grade));
            }

            InMemoryDataStore.getInstance().loadSnapshot(studentsList, subjectsList, resultsList);
            return true;
        } catch (Throwable t) {
            System.err.println("[WARN] applyJsonToInMemory notice: " + t.getMessage());
            return false;
        }
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String readStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static List<String> extractJsonArrayObjects(String json, String arrayKey) {
        List<String> list = new ArrayList<>();
        int keyIdx = json.indexOf("\"" + arrayKey + "\"");
        if (keyIdx == -1) return list;
        int startBracket = json.indexOf('[', keyIdx);
        if (startBracket == -1) return list;

        int depth = 0;
        int objStart = -1;
        for (int i = startBracket + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (depth == 0) objStart = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && objStart != -1) {
                    list.add(json.substring(objStart, i + 1));
                    objStart = -1;
                }
            } else if (c == ']' && depth == 0) {
                break;
            }
        }
        return list;
    }

    private static String getStringField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return "";
        int colon = json.indexOf(':', idx + key.length());
        if (colon == -1) return "";
        int quoteStart = json.indexOf('"', colon);
        if (quoteStart == -1) return "";
        int quoteEnd = json.indexOf('"', quoteStart + 1);
        while (quoteEnd != -1 && json.charAt(quoteEnd - 1) == '\\') {
            quoteEnd = json.indexOf('"', quoteEnd + 1);
        }
        if (quoteEnd == -1) return "";
        return json.substring(quoteStart + 1, quoteEnd).replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static int getIntField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return 0;
        int colon = json.indexOf(':', idx + key.length());
        if (colon == -1) return 0;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Integer.parseInt(json.substring(start, end).trim());
        } catch (Throwable ignored) {
            return 0;
        }
    }

    private static double getDoubleField(String json, String field) {
        String key = "\"" + field + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return 0.0;
        int colon = json.indexOf(':', idx + key.length());
        if (colon == -1) return 0.0;
        int start = colon + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
            end++;
        }
        try {
            return Double.parseDouble(json.substring(start, end).trim());
        } catch (Throwable ignored) {
            return 0.0;
        }
    }
}
