package com.ainjob;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AtsApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void qualifiedBackend_criteriaOnly() throws Exception {
        mockMvc.perform(get("/api/v1/ats/qualified")
                        .param("companyId", "1")
                        .param("positionCode", "BACKEND")
                        .param("minYears", "5")
                        .param("skillKeys", "JAVA", "SPRINGBOOT", "AWS")
                        .param("requireCsRelatedBachelor", "true")
                        .param("degreeSortOrder", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].applicantName", containsInAnyOrder("이서윤", "한예진", "문지후")))
                .andExpect(jsonPath("$[0].stageSortOrder", notNullValue()))
                .andExpect(jsonPath("$[0].statusCode", notNullValue()));
    }

    @Test
    void qualifiedBackend_withStageStatusClassification() throws Exception {
        mockMvc.perform(get("/api/v1/ats/qualified")
                        .param("companyId", "1")
                        .param("positionCode", "BACKEND")
                        .param("minYears", "5")
                        .param("skillKeys", "JAVA", "SPRINGBOOT", "AWS")
                        .param("stageSortOrder", "3")
                        .param("statusCode", "PASSED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].statusCode", everyItem(is("PASSED"))));
    }

    @Test
    void qualifiedFrontend_criteriaOnly() throws Exception {
        mockMvc.perform(get("/api/v1/ats/qualified")
                        .param("companyId", "1")
                        .param("positionCode", "FRONTEND")
                        .param("minYears", "3")
                        .param("skillKeys", "REACT", "NEXTJS", "TYPESCRIPT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].applicantName", containsInAnyOrder("박지예", "강소희")));
    }

    @Test
    void qualifiedBackend_shortParams_stillWorks() throws Exception {
        mockMvc.perform(get("/api/v1/ats/qualified")
                        .param("companyId", "1")
                        .param("positionCode", "BACKEND"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    void qualifiedBackend_higherMinYears_filtersMore() throws Exception {
        mockMvc.perform(get("/api/v1/ats/qualified")
                        .param("companyId", "1")
                        .param("positionCode", "BACKEND")
                        .param("minYears", "11")
                        .param("skillKeys", "JAVA", "SPRINGBOOT", "AWS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void qualifiedBackend_interviewPending_emptyIfNotCriteriaMatch() throws Exception {
        mockMvc.perform(get("/api/v1/ats/qualified")
                        .param("companyId", "1")
                        .param("positionCode", "BACKEND")
                        .param("stageSortOrder", "2")
                        .param("statusCode", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void applications_byStageAndStatus() throws Exception {
        mockMvc.perform(get("/api/v1/ats/applications")
                        .param("companyId", "1")
                        .param("stageSortOrder", "2")
                        .param("statusCode", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].applicantName", is("김똘똘")));
    }

    @Test
    void applications_byStageOnly() throws Exception {
        mockMvc.perform(get("/api/v1/ats/applications")
                        .param("companyId", "1")
                        .param("stageSortOrder", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))));
    }

    @Test
    void applications_byStatusOnly() throws Exception {
        mockMvc.perform(get("/api/v1/ats/applications")
                        .param("companyId", "1")
                        .param("statusCode", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].statusCode", everyItem(is("PENDING"))));
    }

    @Test
    void missingCompanyId_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/ats/qualified")
                        .param("positionCode", "BACKEND"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code", is("MISSING_PARAM")));
    }
}
