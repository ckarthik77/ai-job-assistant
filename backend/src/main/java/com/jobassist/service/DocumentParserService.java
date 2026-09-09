package com.jobassist.service;

import com.jobassist.model.Resume;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service for parsing resume documents (PDF and DOCX)
 * Extracts text content, skills, experience, and education
 */
@Service
@Slf4j
public class DocumentParserService {

    // Common technical skills and keywords to extract
    private static final Set<String> KNOWN_SKILLS = Set.of(
            // Programming Languages
            "Java", "Python", "JavaScript", "TypeScript", "C++", "C#", "Go", "Rust", "Ruby", "PHP",
            "Swift", "Kotlin", "Scala", "R", "MATLAB", "SQL", "HTML", "CSS",

            // Frameworks & Libraries
            "Spring Boot", "Spring", "React", "Angular", "Vue", "Node.js", "Express", "Django",
            "Flask", "FastAPI", "ASP.NET", ".NET", "Rails", "Laravel", "jQuery", "Bootstrap",
            "Tailwind", "Material-UI", "Redux", "Next.js", "Nuxt.js",

            // Databases
            "PostgreSQL", "MySQL", "MongoDB", "Redis", "Oracle", "SQL Server", "DynamoDB",
            "Cassandra", "Elasticsearch", "Neo4j", "MariaDB", "SQLite",

            // Cloud & DevOps
            "AWS", "Azure", "GCP", "Docker", "Kubernetes", "Jenkins", "GitLab CI", "GitHub Actions",
            "Terraform", "Ansible", "Chef", "Puppet", "CircleCI", "Travis CI",

            // Tools & Technologies
            "Git", "Maven", "Gradle", "npm", "Webpack", "Babel", "JUnit", "Jest", "Selenium",
            "Kafka", "RabbitMQ", "GraphQL", "REST API", "Microservices", "WebSocket",
            "OAuth2", "JWT", "SOAP", "gRPC",

            // Data & ML
            "TensorFlow", "PyTorch", "Keras", "scikit-learn", "pandas", "NumPy", "Spark",
            "Hadoop", "Airflow", "dbt", "Tableau", "Power BI", "Looker",

            // Mobile
            "React Native", "Flutter", "iOS", "Android", "Xamarin",

            // Other
            "Agile", "Scrum", "CI/CD", "TDD", "Microservices", "API", "Linux", "Unix",
            "Bash", "PowerShell", "Nginx", "Apache", "Tomcat"
    );

    /**
     * Parse resume from uploaded file
     */
    public Resume parseResume(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        String content;
        if (filename.toLowerCase().endsWith(".pdf")) {
            content = parsePDF(file.getInputStream());
        } else if (filename.toLowerCase().endsWith(".docx")) {
            content = parseDOCX(file.getInputStream());
        } else {
            throw new IllegalArgumentException("Unsupported file format. Only PDF and DOCX are supported.");
        }

        Resume resume = new Resume();
        resume.setOriginalContent(content);
        resume.setSkills(extractSkills(content));
        resume.setExperience(extractExperience(content));
        resume.setEducation(extractEducation(content));

        log.info("Parsed resume: {} skills found, {} experience entries, {} education entries",
                resume.getSkills().size(),
                resume.getExperience().size(),
                resume.getEducation().size());

        return resume;
    }

    /**
     * Parse PDF document
     */
    private String parsePDF(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * Parse DOCX document
     */
    private String parseDOCX(InputStream inputStream) throws IOException {
        try (XWPFDocument document = new XWPFDocument(inputStream)) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();
            return paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .collect(Collectors.joining("\n"));
        }
    }

    /**
     * Extract skills from resume text using known skill set and pattern matching
     */
    public List<String> extractSkills(String text) {
        Set<String> foundSkills = new HashSet<>();

        // Case-insensitive search for known skills
        for (String skill : KNOWN_SKILLS) {
            Pattern pattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                foundSkills.add(skill);
            }
        }

        // Look for skills section specifically
        Pattern skillsSection = Pattern.compile(
                "(?i)(skills|technical skills|core competencies):?\\s*([^\\n]+(?:\\n[^\\n]+)*?)(?=\\n\\n|\\n[A-Z]|$)",
                Pattern.MULTILINE
        );
        Matcher matcher = skillsSection.matcher(text);
        if (matcher.find()) {
            String skillsText = matcher.group(2);
            // Extract comma-separated or bullet-pointed skills
            String[] potentialSkills = skillsText.split("[,•\\-\\n]");
            for (String skill : potentialSkills) {
                String cleaned = skill.trim();
                if (cleaned.length() > 2 && cleaned.length() < 50) {
                    foundSkills.add(cleaned);
                }
            }
        }

        return new ArrayList<>(foundSkills);
    }

    /**
     * Extract work experience entries from resume text
     */
    private List<Map<String, Object>> extractExperience(String text) {
        List<Map<String, Object>> experiences = new ArrayList<>();

        // Look for experience section
        Pattern expSection = Pattern.compile(
                "(?i)(work experience|professional experience|experience|employment history):?\\s*([\\s\\S]*?)(?=\\n\\n[A-Z][a-z]+:|education|skills|$)",
                Pattern.MULTILINE
        );
        Matcher matcher = expSection.matcher(text);

        if (matcher.find()) {
            String experienceText = matcher.group(2);

            // Try to parse individual job entries (company, title, dates, description)
            Pattern jobPattern = Pattern.compile(
                    "([\\w\\s&,.]+?)\\s*[|\\-]?\\s*([\\w\\s]+?)\\s*[|\\-]?\\s*(\\d{4}\\s*[-–]\\s*(?:\\d{4}|Present))",
                    Pattern.MULTILINE
            );
            Matcher jobMatcher = jobPattern.matcher(experienceText);

            while (jobMatcher.find()) {
                Map<String, Object> experience = new HashMap<>();
                experience.put("company", jobMatcher.group(1).trim());
                experience.put("title", jobMatcher.group(2).trim());
                experience.put("dates", jobMatcher.group(3).trim());
                experiences.add(experience);
            }
        }

        // If no structured experience found, create a generic entry
        if (experiences.isEmpty()) {
            Map<String, Object> genericExp = new HashMap<>();
            genericExp.put("company", "Previous Experience");
            genericExp.put("title", "Various Roles");
            genericExp.put("dates", "");
            experiences.add(genericExp);
        }

        return experiences;
    }

    /**
     * Extract education entries from resume text
     */
    private List<Map<String, Object>> extractEducation(String text) {
        List<Map<String, Object>> educationList = new ArrayList<>();

        // Look for education section
        Pattern eduSection = Pattern.compile(
                "(?i)(education|academic background):?\\s*([\\s\\S]*?)(?=\\n\\n[A-Z][a-z]+:|skills|experience|$)",
                Pattern.MULTILINE
        );
        Matcher matcher = eduSection.matcher(text);

        if (matcher.find()) {
            String educationText = matcher.group(2);

            // Try to parse degree entries
            Pattern degreePattern = Pattern.compile(
                    "(Bachelor|Master|Ph\\.?D\\.?|MBA|B\\.?S\\.?|M\\.?S\\.?|B\\.?A\\.?|M\\.?A\\.?).*?([\\w\\s]+?)\\s*[|\\-]?\\s*(\\d{4})",
                    Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
            );
            Matcher degreeMatcher = degreePattern.matcher(educationText);

            while (degreeMatcher.find()) {
                Map<String, Object> education = new HashMap<>();
                education.put("degree", degreeMatcher.group(1).trim());
                education.put("institution", degreeMatcher.group(2).trim());
                education.put("year", degreeMatcher.group(3).trim());
                educationList.add(education);
            }
        }

        // If no education found, add placeholder
        if (educationList.isEmpty()) {
            Map<String, Object> genericEdu = new HashMap<>();
            genericEdu.put("degree", "Degree");
            genericEdu.put("institution", "University");
            genericEdu.put("year", "");
            educationList.add(genericEdu);
        }

        return educationList;
    }

    /**
     * Extract keywords from job description text
     */
    public List<String> extractJobKeywords(String jobDescription) {
        // Similar to skill extraction but more aggressive
        Set<String> keywords = new HashSet<>();

        // Extract known skills
        keywords.addAll(extractSkills(jobDescription));

        // Extract requirement-related keywords
        Pattern requirementPattern = Pattern.compile(
                "(?i)(required?|qualifications?|must have|essential):?\\s*([^\\n]+(?:\\n[^\\n]+)*?)(?=\\n\\n|\\n[A-Z]|$)",
                Pattern.MULTILINE
        );
        Matcher matcher = requirementPattern.matcher(jobDescription);
        if (matcher.find()) {
            String reqText = matcher.group(2);
            String[] parts = reqText.split("[,•\\-\\n]");
            for (String part : parts) {
                String cleaned = part.trim();
                if (cleaned.length() > 2 && cleaned.length() < 50) {
                    keywords.add(cleaned);
                }
            }
        }

        return new ArrayList<>(keywords);
    }
}
