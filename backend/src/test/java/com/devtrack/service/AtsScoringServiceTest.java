package com.devtrack.service;

import com.devtrack.dto.AtsScoreResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtsScoringServiceTest {

    private final AtsScoringService service = new AtsScoringService();

    @Test
    void score_rewardsWellStructuredResumeText() {
        String goodResume = "SUMMARY\nBackend engineer with 4 years experience.\n" +
                "jane.doe@email.com | +1-555-123-4567\n" +
                "EXPERIENCE\nEngineered a Spring Boot and PostgreSQL platform, reducing latency by 30%. " +
                "Built REST APIs consumed by React frontend. Automated deployment with Docker and CI/CD.\n" +
                "EDUCATION\nB.S. Computer Science.\n" +
                "SKILLS\nJava, Spring Boot, PostgreSQL, Docker, AWS, React, Git.\n" +
                "PROJECTS\nBuilt an inventory management system used by 200 daily users.";

        AtsScoreResponse result = service.score(goodResume, 80);

        assertTrue(result.getOverallAtsScore() > 60);
    }

    @Test
    void score_penalizesSparseOrGarbledText() {
        String badResume = "x x x x x x x x x x";

        AtsScoreResponse result = service.score(badResume, 0);

        assertTrue(result.getOverallAtsScore() < 40);
    }
}
