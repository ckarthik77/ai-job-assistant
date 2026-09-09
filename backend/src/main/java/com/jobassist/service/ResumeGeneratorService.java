package com.jobassist.service;

import com.jobassist.model.Project;
import com.jobassist.model.Resume;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for generating professional PDF resumes
 * Ensures single-page layout with proper formatting
 */
@Service
@Slf4j
public class ResumeGeneratorService {

    // Page settings
    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
    private static final float USABLE_WIDTH = PAGE_WIDTH - (2 * MARGIN);

    // Font settings
    private static final PDFont FONT_BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont FONT_REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont FONT_ITALIC = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

    private static final float FONT_SIZE_NAME = 18;
    private static final float FONT_SIZE_SECTION = 12;
    private static final float FONT_SIZE_NORMAL = 10;
    private static final float FONT_SIZE_SMALL = 9;

    private static final float LINE_SPACING = 14;
    private static final float SECTION_SPACING = 18;

    /**
     * Generate enhanced resume PDF
     */
    public byte[] generateEnhancedResume(
            Resume originalResume,
            List<Project> addedProjects,
            List<String> addedKeywords
    ) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float yPosition = PAGE_HEIGHT - MARGIN;

                // Header (Name and Contact)
                yPosition = addHeader(contentStream, originalResume, yPosition);
                yPosition -= SECTION_SPACING;

                // Professional Summary (if space allows)
                yPosition = addSummary(contentStream, originalResume, addedKeywords, yPosition);
                yPosition -= SECTION_SPACING;

                // Skills Section
                yPosition = addSkillsSection(contentStream, originalResume, addedKeywords, yPosition);
                yPosition -= SECTION_SPACING;

                // Experience Section
                yPosition = addExperienceSection(contentStream, originalResume, yPosition);
                yPosition -= SECTION_SPACING;

                // Projects Section (added projects in XYZ format)
                if (!addedProjects.isEmpty() && yPosition > 150) {
                    yPosition = addProjectsSection(contentStream, addedProjects, yPosition);
                    yPosition -= SECTION_SPACING;
                }

                // Education Section
                if (yPosition > 100) {
                    addEducationSection(contentStream, originalResume, yPosition);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            document.save(outputStream);
            log.info("Generated enhanced resume PDF: {} bytes", outputStream.size());
            return outputStream.toByteArray();
        }
    }

    /**
     * Add header with name and contact info
     */
    private float addHeader(PDPageContentStream contentStream, Resume resume, float yPosition) throws IOException {
        // Extract name from resume (fallback to "Professional")
        String name = extractName(resume.getOriginalContent());
        String email = resume.getUserEmail() != null ? resume.getUserEmail() : "email@example.com";

        // Name (centered, bold, large)
        contentStream.beginText();
        contentStream.setFont(FONT_BOLD, FONT_SIZE_NAME);
        float nameWidth = FONT_BOLD.getStringWidth(name) / 1000 * FONT_SIZE_NAME;
        contentStream.newLineAtOffset((PAGE_WIDTH - nameWidth) / 2, yPosition);
        contentStream.showText(name);
        contentStream.endText();
        yPosition -= LINE_SPACING * 1.5f;

        // Contact info (centered, smaller)
        contentStream.beginText();
        contentStream.setFont(FONT_REGULAR, FONT_SIZE_SMALL);
        float emailWidth = FONT_REGULAR.getStringWidth(email) / 1000 * FONT_SIZE_SMALL;
        contentStream.newLineAtOffset((PAGE_WIDTH - emailWidth) / 2, yPosition);
        contentStream.showText(email);
        contentStream.endText();
        yPosition -= LINE_SPACING;

        // Separator line
        contentStream.moveTo(MARGIN, yPosition);
        contentStream.lineTo(PAGE_WIDTH - MARGIN, yPosition);
        contentStream.stroke();
        yPosition -= LINE_SPACING;

        return yPosition;
    }

    /**
     * Add professional summary with keywords
     */
    private float addSummary(PDPageContentStream contentStream, Resume resume,
                            List<String> addedKeywords, float yPosition) throws IOException {
        // Skip if not enough space
        if (yPosition < 200) return yPosition;

        contentStream.beginText();
        contentStream.setFont(FONT_BOLD, FONT_SIZE_SECTION);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("PROFESSIONAL SUMMARY");
        contentStream.endText();
        yPosition -= LINE_SPACING * 1.2f;

        String summary = String.format(
                "Results-driven professional with expertise in %s. Proven track record of delivering " +
                "high-quality solutions and driving impactful results.",
                String.join(", ", addedKeywords.subList(0, Math.min(5, addedKeywords.size())))
        );

        yPosition = addWrappedText(contentStream, summary, MARGIN, yPosition, USABLE_WIDTH, FONT_REGULAR, FONT_SIZE_NORMAL);
        return yPosition;
    }

    /**
     * Add skills section
     */
    private float addSkillsSection(PDPageContentStream contentStream, Resume resume,
                                  List<String> addedKeywords, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_BOLD, FONT_SIZE_SECTION);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("TECHNICAL SKILLS");
        contentStream.endText();
        yPosition -= LINE_SPACING * 1.2f;

        // Combine original skills with added keywords
        List<String> allSkills = new ArrayList<>(resume.getSkills());
        allSkills.addAll(addedKeywords);
        String skillsText = String.join(" • ", allSkills.stream().distinct().limit(30).toList());

        yPosition = addWrappedText(contentStream, skillsText, MARGIN, yPosition, USABLE_WIDTH, FONT_REGULAR, FONT_SIZE_NORMAL);
        return yPosition;
    }

    /**
     * Add experience section
     */
    private float addExperienceSection(PDPageContentStream contentStream, Resume resume, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_BOLD, FONT_SIZE_SECTION);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("PROFESSIONAL EXPERIENCE");
        contentStream.endText();
        yPosition -= LINE_SPACING * 1.2f;

        // Add experience entries (limit to 2 for space)
        List<Map<String, Object>> experiences = resume.getExperience();
        for (int i = 0; i < Math.min(2, experiences.size()); i++) {
            Map<String, Object> exp = experiences.get(i);

            // Company and dates
            String company = exp.get("company").toString();
            String dates = exp.getOrDefault("dates", "").toString();

            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(company);
            contentStream.endText();

            if (!dates.isEmpty()) {
                float datesWidth = FONT_ITALIC.getStringWidth(dates) / 1000 * FONT_SIZE_SMALL;
                contentStream.beginText();
                contentStream.setFont(FONT_ITALIC, FONT_SIZE_SMALL);
                contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - datesWidth, yPosition);
                contentStream.showText(dates);
                contentStream.endText();
            }
            yPosition -= LINE_SPACING;

            // Title
            String title = exp.get("title").toString();
            contentStream.beginText();
            contentStream.setFont(FONT_ITALIC, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(title);
            contentStream.endText();
            yPosition -= LINE_SPACING * 1.2f;

            // Check remaining space
            if (yPosition < 150) break;
        }

        return yPosition;
    }

    /**
     * Add projects section with XYZ-formatted descriptions
     */
    private float addProjectsSection(PDPageContentStream contentStream, List<Project> projects, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_BOLD, FONT_SIZE_SECTION);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("KEY PROJECTS");
        contentStream.endText();
        yPosition -= LINE_SPACING * 1.2f;

        for (Project project : projects) {
            // Check space constraint
            if (yPosition < 120) break;

            // Project title
            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText("• " + project.getTitle());
            contentStream.endText();
            yPosition -= LINE_SPACING;

            // Project description (XYZ format)
            yPosition = addWrappedText(contentStream, project.getDescription(), MARGIN + 10, yPosition,
                    USABLE_WIDTH - 10, FONT_REGULAR, FONT_SIZE_SMALL);
            yPosition -= LINE_SPACING * 0.5f;
        }

        return yPosition;
    }

    /**
     * Add education section
     */
    private float addEducationSection(PDPageContentStream contentStream, Resume resume, float yPosition) throws IOException {
        contentStream.beginText();
        contentStream.setFont(FONT_BOLD, FONT_SIZE_SECTION);
        contentStream.newLineAtOffset(MARGIN, yPosition);
        contentStream.showText("EDUCATION");
        contentStream.endText();
        yPosition -= LINE_SPACING * 1.2f;

        List<Map<String, Object>> education = resume.getEducation();
        if (!education.isEmpty()) {
            Map<String, Object> edu = education.get(0);
            String degree = edu.get("degree").toString();
            String institution = edu.get("institution").toString();
            String year = edu.getOrDefault("year", "").toString();

            contentStream.beginText();
            contentStream.setFont(FONT_BOLD, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(degree);
            contentStream.endText();
            yPosition -= LINE_SPACING;

            String eduInfo = institution + (!year.isEmpty() ? " | " + year : "");
            contentStream.beginText();
            contentStream.setFont(FONT_REGULAR, FONT_SIZE_NORMAL);
            contentStream.newLineAtOffset(MARGIN, yPosition);
            contentStream.showText(eduInfo);
            contentStream.endText();
        }

        return yPosition;
    }

    /**
     * Add wrapped text that fits within the page width
     */
    private float addWrappedText(PDPageContentStream contentStream, String text, float x, float y,
                                float maxWidth, PDFont font, float fontSize) throws IOException {
        List<String> lines = wrapText(text, font, fontSize, maxWidth);

        for (String line : lines) {
            contentStream.beginText();
            contentStream.setFont(font, fontSize);
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(line);
            contentStream.endText();
            y -= LINE_SPACING;
        }

        return y;
    }

    /**
     * Wrap text to fit within specified width
     */
    private List<String> wrapText(String text, PDFont font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.length() == 0 ? word : currentLine + " " + word;
            float width = font.getStringWidth(testLine) / 1000 * fontSize;

            if (width > maxWidth && currentLine.length() > 0) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine = new StringBuilder(testLine);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    /**
     * Extract name from resume content
     */
    private String extractName(String resumeContent) {
        // Try to extract name from first line
        String[] lines = resumeContent.split("\n");
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.length() > 0 && firstLine.length() < 50 && !firstLine.toLowerCase().contains("resume")) {
                return firstLine;
            }
        }
        return "Professional Resume";
    }
}
