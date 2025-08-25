package com.dasa.splitspends.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.dasa.splitspends.dto.group.GroupRequest;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.entity.User;
import com.dasa.splitspends.security.AuthorizationService;
import com.dasa.splitspends.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = GroupController.class, excludeAutoConfiguration = {
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
})
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GroupService groupService;

    @MockBean
    private AuthorizationService authorizationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateGroup() throws Exception {
        // Mock the current user
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setName("Test User");
        currentUser.setEmail("test@example.com");

        Mockito.when(authorizationService.getCurrentUser()).thenReturn(currentUser);

        GroupRequest request = new GroupRequest();
        request.setName("Test Group");
        request.setDescription("A test group");
        request.setCreatedByUserId(1L); // Set the required field
        request.setDefaultCurrency("USD");

        Group group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        group.setDescription("A test group");
        group.setDefaultCurrency("USD");
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        Mockito.when(groupService.createGroup(Mockito.anyString(), Mockito.anyString(),
                Mockito.anyLong(), Mockito.any(), Mockito.anyString())).thenReturn(group);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Group"));
    }

    @Test
    void testGetUserGroups() throws Exception {
        // Mock authorization check
        Mockito.when(authorizationService.isCurrentUser(1L)).thenReturn(true);

        Group group = new Group();
        group.setId(1L);
        group.setName("User Group");
        group.setDescription("User's group");
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        List<Group> groups = List.of(group);

        Mockito.when(groupService.getUserGroups(Mockito.anyLong())).thenReturn(groups);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/groups/user/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].name").value("User Group"));
    }
}
