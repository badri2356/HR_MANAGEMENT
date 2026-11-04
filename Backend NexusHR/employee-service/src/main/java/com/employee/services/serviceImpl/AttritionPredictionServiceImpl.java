package com.employee.services.serviceImpl;

import com.employee.domain.enums.RiskLevel;
import com.employee.dtos.response.AiAttritionPrediction;
import com.employee.dtos.response.AttritionInsightDto;
import com.employee.dtos.response.AttritionPrediction;
import com.employee.services.service.AttritionPredictionService;
import com.employee.services.service.InsightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AttritionPredictionServiceImpl implements AttritionPredictionService {

    private static final String SYSTEM_PROMPT = """
            You are an HR analytics assistant explaining an employee's attrition risk.
            The risk level has ALREADY been decided by the system. Do not change it or suggest another level.
            Use ONLY the data provided. Do not invent facts, names or numbers.
            Give at most 3 key drivers, each one short sentence based on the data.
            Give one practical, respectful action HR can take.
            If the risk is LOW with no warning signs, say there is nothing concerning.
            Rewrite the warning signs in plain, friendly language for an HR manager; do not just copy them.
            """;

    private final InsightService insightService;
    private final ChatClient chatClient;

    public AttritionPredictionServiceImpl(InsightService insightService, ChatClient chatClient) {
        this.insightService = insightService;
        this.chatClient = chatClient;
    }

    @Override
    public AttritionPrediction predict(Long employeeId, Authentication authentication) {

        AttritionInsightDto rules = insightService.getAttritionForEmployee(employeeId, authentication);
        try {
            AiAttritionPrediction ai = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(describe(rules))
                    .call()
                    .entity(AiAttritionPrediction.class);

            if (ai == null || ai.reasons() == null ||
                    ai.reasons().isEmpty() || ai.recommendation() == null || ai.recommendation().isBlank()) {
                throw new IllegalStateException("Incomplete AI answer");
            }

            return new AttritionPrediction(
                    rules.employeeId(),
                    rules.employeeName(),
                    rules.riskLevel(),                          // always the rules' level
                    ai.reasons().stream().limit(3).toList(),
                    ai.recommendation(),
                    "AI");
        } catch (Exception e) {
            log.warn("AI prediction unavailable for employee {}: {}", employeeId, e.getMessage(), e);

            return new AttritionPrediction(
                    rules.employeeId(),
                    rules.employeeName(),
                    rules.riskLevel(),
                    rules.reasons(),                            // the rules' own reasons
                    defaultAction(rules.riskLevel()),
                    "RULES");
        }
    }

    private String describe(AttritionInsightDto r) {
        return """
                Employee: %s (%s department)
                Risk level (already decided): %s, score %d out of 100
                Attendance rate in recent weeks: %s
                Latest performance review rating: %s
                Average goal progress: %s
                Sick/casual leave days in the last 90 days: %d
                Months since joining: %d
                Warning signs found by the system: %s
                """.formatted(
                r.employeeName(),
                r.departmentName() == null ? "no" : r.departmentName(),
                r.riskLevel(), r.score(),
                r.attendanceRate() == null ? "no data" : r.attendanceRate() + "%",
                r.latestRating() == null ? "not reviewed yet" : r.latestRating() + " out of 5",
                r.goalProgress() == null ? "no goals" : r.goalProgress() + "%",
                r.unplannedLeaveDays(),
                r.tenureMonths(),
                String.join("; ", r.reasons()));
    }

    private String defaultAction(RiskLevel level) {
        return switch (level) {
            case HIGH -> "Schedule a one-to-one this week to understand concerns and agree on support.";
            case MEDIUM -> "Check in during the next one-to-one and review workload and goals.";
            case LOW -> "No action needed; continue regular check-ins.";
        };
    }
}
