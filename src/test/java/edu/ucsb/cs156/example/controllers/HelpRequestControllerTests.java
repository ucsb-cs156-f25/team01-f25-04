package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockBean HelpRequestRepository helpRequestRepository;

  @MockBean UserRepository userRepository;

  // Tests for GET

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    // Expectation: 403 Forbidden for unauthenticated GET
    mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    // Expectation: 200 OK for authenticated GET
    mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().is(200));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_help_requests() throws Exception {

    // Setup: build entities expected to be saved; stub repository to echo them back
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    HelpRequest helpRequest1 =
        HelpRequest.builder()
            .requesterEmail("alice@ucsb.edu")
            .teamId("01")
            .tableOrBreakoutRoom("01")
            .requestTime(ldt1)
            .explanation("test alice")
            .solved(false)
            .build();

    LocalDateTime ldt2 = LocalDateTime.parse("2022-10-20T00:00:00");

    HelpRequest helpRequest2 =
        HelpRequest.builder()
            .requesterEmail("bob@ucsb.edu")
            .teamId("10")
            .tableOrBreakoutRoom("10")
            .requestTime(ldt2)
            .explanation("test bob")
            .solved(true)
            .build();

    ArrayList<HelpRequest> expectedDates = new ArrayList<>();
    expectedDates.addAll(Arrays.asList(helpRequest1, helpRequest2));

    when(helpRequestRepository.findAll()).thenReturn(expectedDates);

    // Action: USER requests GET /api/HelpRequest/all
    MvcResult response =
        mockMvc.perform(get("/api/HelpRequest/all")).andExpect(status().isOk()).andReturn();

    // Expectation: 403 Forbidden for unauthenticated request
    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedDates);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    // Expectation: 403 Forbidden for unauthenticated request
    mockMvc.perform(get("/api/HelpRequest?id=7")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // Setup: build new entity; repository should behave as if it has ID 7;
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    HelpRequest helpRequest1 =
        HelpRequest.builder()
            .requesterEmail("cgaucho@ucsb.edu")
            .teamId("04")
            .tableOrBreakoutRoom("04")
            .requestTime(ldt1)
            .explanation("test_unsolved")
            .solved(true)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(helpRequest1));

    // Action: USER requests GET /api/HelpRequest?id=7
    MvcResult response =
        mockMvc.perform(get("/api/HelpRequest?id=7")).andExpect(status().is(200)).andReturn();

    // Expectation: repository queried and returns enitity
    verify(helpRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(helpRequest1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_cannot_get_by_id_when_the_id_does_not_exist() throws Exception {
    // Setup: repository should behave as if ID 7 is missing; use 7L (long) to match method
    // signature
    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // Action: USER requests GET /api/HelpRequest?id=7
    MvcResult response =
        mockMvc.perform(get("/api/HelpRequest?id=7")).andExpect(status().is(404)).andReturn();

    // Expectation: repository queried once and returns 404 Not Found; error payload contains type
    // and message
    verify(helpRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }

  // Tests for POST

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    // Expectation: 403 Forbidden for unauthenticated POST
    mockMvc.perform(post("/api/HelpRequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    // Expectation: 403 Forbidden for non-ADMIN POST
    mockMvc.perform(post("/api/HelpRequest/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_unsolved_help_request() throws Exception {

    // Setup: build entity expected to be saved; stub repository to echo it back
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    HelpRequest helpRequest1 =
        HelpRequest.builder()
            .requesterEmail("cgaucho@ucsb.edu")
            .teamId("04")
            .tableOrBreakoutRoom("04")
            .requestTime(ldt1)
            .explanation("test_unsolved")
            .solved(false)
            .build();

    when(helpRequestRepository.save(eq(helpRequest1))).thenReturn(helpRequest1);

    // Action: ADMIN requests POST /api/HelpRequest/post?...; include CSRF for POST
    MvcResult response =
        mockMvc
            .perform(
                post("/api/HelpRequest/post?requesterEmail=cgaucho@ucsb.edu&teamId=04&tableOrBreakoutRoom=04&requestTime=2022-01-03T00:00:00&explanation=test_unsolved&solved=false")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // Expectation: one save; response body equals serialized entity
    verify(helpRequestRepository, times(1)).save(helpRequest1);
    String expectedJson = mapper.writeValueAsString(helpRequest1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_solved_help_request() throws Exception {

    // Setup: build entity expected to be saved; stub repository to echo it back
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    HelpRequest helpRequest1 =
        HelpRequest.builder()
            .requesterEmail("cgaucho@ucsb.edu")
            .teamId("04")
            .tableOrBreakoutRoom("04")
            .requestTime(ldt1)
            .explanation("test_solved")
            .solved(true)
            .build();

    when(helpRequestRepository.save(eq(helpRequest1))).thenReturn(helpRequest1);

    // Action: ADMIN requests POST /api/HelpRequest/post?...; include CSRF for POST
    MvcResult response =
        mockMvc
            .perform(
                post("/api/HelpRequest/post?requesterEmail=cgaucho@ucsb.edu&teamId=04&tableOrBreakoutRoom=04&requestTime=2022-01-03T00:00:00&explanation=test_solved&solved=true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // Expectation: one save; response body equals serialized entity
    verify(helpRequestRepository, times(1)).save(helpRequest1);
    String expectedJson = mapper.writeValueAsString(helpRequest1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for DELETE

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_help_request() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    HelpRequest helpRequest1 =
        HelpRequest.builder()
            .requesterEmail("cgaucho@ucsb.edu")
            .teamId("04")
            .tableOrBreakoutRoom("04")
            .requestTime(ldt1)
            .explanation("test_solved")
            .solved(true)
            .build();

    when(helpRequestRepository.findById(eq(15L))).thenReturn(Optional.of(helpRequest1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/HelpRequest?id=15").with(csrf()))
            .andExpect(status().is(200))
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(15L);
    verify(helpRequestRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_tries_to_delete_non_existant_help_request_and_gets_right_error_message()
      throws Exception {
    // arrange

    when(helpRequestRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/HelpRequest?id=15").with(csrf()))
            .andExpect(status().is(404))
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 15 not found", json.get("message"));
  }

  // Tests for PUT

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_help_request() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");
    LocalDateTime ldt2 = LocalDateTime.parse("2023-01-03T00:00:00");

    HelpRequest helpRequestOrig =
        HelpRequest.builder()
            .requesterEmail("alice@ucsb.edu")
            .teamId("01")
            .tableOrBreakoutRoom("01")
            .requestTime(ldt1)
            .explanation("test alice")
            .solved(false)
            .build();

    HelpRequest helpRequestEdited =
        HelpRequest.builder()
            .requesterEmail("bob@ucsb.edu")
            .teamId("10")
            .tableOrBreakoutRoom("10")
            .requestTime(ldt2)
            .explanation("test bob")
            .solved(true)
            .build();

    String requestBody = mapper.writeValueAsString(helpRequestEdited);

    when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.of(helpRequestOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/HelpRequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().is(200))
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(67L);
    verify(helpRequestRepository, times(1))
        .save(helpRequestEdited); // should be saved with correct user
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_help_request_that_does_not_exist() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    HelpRequest helpRequestEdited =
        HelpRequest.builder()
            .requesterEmail("bob@ucsb.edu")
            .teamId("10")
            .tableOrBreakoutRoom("10")
            .requestTime(ldt1)
            .explanation("test bob")
            .solved(true)
            .build();

    String requestBody = mapper.writeValueAsString(helpRequestEdited);

    when(helpRequestRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/HelpRequest?id=67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().is(404))
            .andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("HelpRequest with id 67 not found", json.get("message"));
  }
}
