package com.example.aiworkshop.tasks.task_1_first_agent.agent;

import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "vertex-ai")
public record VertexAiProperties(
        String project,
        String location,
        String modelName,
        @DefaultValue("gemini-2.5-flash-lite") String cheaperModelName,
        @DefaultValue("0.2") Float temperature,
        @DefaultValue("1024") Integer maxOutputTokens,
        @DefaultValue("3") Integer maxRetries,
        @DefaultValue("false") Boolean logRequests,
        @DefaultValue("false") Boolean logResponses) {

    private static final String NOT_SET = "unspecified";

    /**
     * Which Google Cloud project to bill and authorise against.
     *
     * <p>Normally GOOGLE_CLOUD_PROJECT, but the point of this method is the claim where nobody set
     * it. Signing in to Claude Code leaves Application Default Credentials on the machine, and
     * those credentials already name a project — so asking a participant to export it as well is
     * asking them to repeat something their laptop already knows.
     *
     * <p>Falling through to "unspecified" gives PERMISSION_DENIED on a project of that name, which
     * is a puzzling first thing to hit on the morning of a workshop.
     */
    public String projectId() {
        if (project != null && !project.isBlank() && !NOT_SET.equals(project)) {
            return project;
        }
        return projectFromApplicationDefaultCredentials().orElse(project);
    }

    private static java.util.Optional<String> projectFromApplicationDefaultCredentials() {
        try {
            String quotaProject = GoogleCredentials.getApplicationDefault().getQuotaProjectId();
            return java.util.Optional.ofNullable(quotaProject).filter(id -> !id.isBlank());
        } catch (IOException noCredentialsOnThisMachine) {
            return java.util.Optional.empty();
        }
    }
}
