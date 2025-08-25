package com.dasa.splitspends.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.dasa.splitspends.dto.expense.CreateExpenseRequest;
import com.dasa.splitspends.entity.Expense;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.security.AuthorizationService;
import com.dasa.splitspends.service.ExpenseService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ExpenseController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
class ExpenseControllerTest {

    // Test constants
    private static final String DINNER_DESCRIPTION = "Dinner";
    private static final String LUNCH_DESCRIPTION = "Lunch";
    private static final BigDecimal DINNER_AMOUNT = BigDecimal.valueOf(100);
    private static final BigDecimal LUNCH_AMOUNT = BigDecimal.valueOf(50);

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private AuthorizationService authorizationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateExpenseWithEqualSplits() throws Exception {
        // Mock the current user
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setName("Test User");
        currentUser.setEmail("test@example.com");

        Mockito.when(authorizationService.getCurrentUser()).thenReturn(currentUser);

        CreateExpenseRequest request = new CreateExpenseRequest();
        request.setPaidByUserId(1L);
        request.setDescription(DINNER_DESCRIPTION);
        request.setAmount(DINNER_AMOUNT);
        request.setParticipantUserIds(List.of(1L, 2L));
        request.setCategory(Expense.ExpenseCategory.FOOD_DRINKS.name());

        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription(DINNER_DESCRIPTION);
        expense.setAmount(DINNER_AMOUNT);
        expense.setDate(LocalDateTime.now());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());

        Mockito.when(expenseService.createExpenseWithEqualSplits(Mockito.anyLong(), Mockito.anyLong(),
                Mockito.anyString(), Mockito.any(), Mockito.anyList(), Mockito.any())).thenReturn(expense);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/expenses/group/1/equal-split")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(result -> System.out.println("Response: " + result.getResponse().getContentAsString()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.description").value("Dinner"));
    }

    @Test
    void testGetGroupExpenses() throws Exception {
        Expense expense = new Expense();
        expense.setId(1L);
        expense.setDescription(LUNCH_DESCRIPTION);
        expense.setAmount(LUNCH_AMOUNT);
        expense.setDate(LocalDateTime.now());
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
        var page = new PageImpl<>(List.of(expense), PageRequest.of(0, 10), 1);

        Mockito.when(expenseService.getGroupExpenses(Mockito.eq(1L), Mockito.any())).thenReturn(page);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/expenses/group/1")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].description").value("Lunch"));
    }
}