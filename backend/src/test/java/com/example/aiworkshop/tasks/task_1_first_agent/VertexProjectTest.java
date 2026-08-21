package com.example.aiworkshop.tasks.task_1_first_agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import com.example.aiworkshop.tasks.task_1_first_agent.agent.VertexAiProperties;
import com.google.auth.oauth2.GoogleCredentials;
import org.junit.jupiter.api.Test;

/**
 * Which project the workshop bills against when nobody has said.
 *
 * <p>A participant who has signed in to Claude Code already has credentials naming a project.
 * Asking them to export it again is asking them to tell the machine something it knows, and getting
 * it wrong costs them PERMISSION_DENIED on a project literally called "unspecified" as the first
 * thing that happens on the day.
 */
class VertexProjectTest {

    @Test
    void anExplicitProjectIsUsedAsGiven() {
        assertThat(propertiesFor("my-project").projectId()).isEqualTo("my-project");
    }

    @Test
    void theUnsetPlaceholderFallsBackToTheCredentialsOnThisMachine() {
        assumeThat(applicationDefaultCredentialsExist())
                .as("needs `gcloud auth application-default login` to have been run")
                .isTrue();

        assertThat(propertiesFor("unspecified").projectId())
                .isNotEqualTo("unspecified")
                .isNotBlank();
    }

    @Test
    void soDoesABlankOne() {
        assumeThat(applicationDefaultCredentialsExist()).isTrue();

        assertThat(propertiesFor("  ").projectId()).isNotBlank().isNotEqualTo("  ");
    }

    private static VertexAiProperties propertiesFor(String project) {
        return new VertexAiProperties(
                project, "europe-west4", "gemini-2.5-flash", 0.2f, 16384, 3, false, false);
    }

    private static boolean applicationDefaultCredentialsExist() {
        try {
            return GoogleCredentials.getApplicationDefault().getQuotaProjectId() != null;
        } catch (Exception none) {
            return false;
        }
    }
}
