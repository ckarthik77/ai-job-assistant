package com.jobassist.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for job description upload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDescriptionRequest {

    private String companyName;

    private String jobTitle;

    @NotBlank(message = "Job description text is required")
    private String descriptionText;
}
