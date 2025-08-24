package com.dasa.splitspends.controller;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.dasa.splitspends.dto.group.GroupRequest;
import com.dasa.splitspends.entity.Group;
import com.dasa.splitspends.service.GroupService;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GroupControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GroupService groupService;

    @InjectMocks
    private GroupController groupController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(groupController).build();
    }

    @Test
    void testCreateGroup() throws Exception {
        GroupRequest request = new GroupRequest();
        request.setName("Test Group");
        request.setDescription("A test group");
        request.setCreatedByUserId(1L);
        request.setPrivacyLevel(Group.PrivacyLevel.PUBLIC);
        request.setDefaultCurrency("USD");

        Group group = new Group();
        group.setId(1L);
        group.setName("Test Group");

        Mockito.when(
                groupService.createGroup(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(group);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/groups")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Group"));
    }

    @Test
    void testUpdateGroup() throws Exception {
        GroupRequest request = new GroupRequest();
        request.setName("Updated Group");
        request.setDescription("Updated description");
        request.setPrivacyLevel(Group.PrivacyLevel.PRIVATE);
        request.setDefaultCurrency("EUR");
        request.setUpdatedByUserId(2L);

        Group group = new Group();
        group.setId(1L);
        group.setName("Updated Group");

        Mockito.when(groupService.updateGroup(Mockito.eq(1L), Mockito.any(), Mockito.any(), Mockito.any(),
                Mockito.any(), Mockito.any()))
                .thenReturn(group);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/groups/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated Group"));
    }

    @Test
    void testDeleteGroup() throws Exception {
        Mockito.doNothing().when(groupService).deleteGroup(1L, 2L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/groups/1")
                .param("deletedByUserId", "2"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void testGetGroup() throws Exception {
        Group group = new Group();
        group.setId(1L);
        group.setName("Test Group");
        Mockito.when(groupService.getGroupById(1L)).thenReturn(group);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/groups/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test Group"));
    }

    @Test
    void testSearchPublicGroups() throws Exception {
        Mockito.when(groupService.searchPublicGroups("test")).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/groups/search")
                .param("query", "test"))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
