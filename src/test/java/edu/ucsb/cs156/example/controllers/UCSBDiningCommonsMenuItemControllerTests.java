package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/ucsbdiningcommonsmenuitem/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsbdiningcommonsmenuitem/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/ucsbdiningcommonsmenuitem/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menu_items() throws Exception {
    // Arrange
    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("DLG")
            .name("Pancakes")
            .station("Breakfast")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedItems = new ArrayList<>();
    expectedItems.add(item1);

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedItems);

    // Act
    MvcResult response =
        mockMvc
            .perform(get("/api/ucsbdiningcommonsmenuitem/all"))
            .andExpect(status().isOk())
            .andReturn();

    // Assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_a_new_menu_item() throws Exception {
    // Arrange
    UCSBDiningCommonsMenuItem item1 =
        UCSBDiningCommonsMenuItem.builder()
            .diningCommonsCode("DLG")
            .name("Pancakes")
            .station("Breakfast")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(eq(item1))).thenReturn(item1);

    // Act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsbdiningcommonsmenuitem/post")
                    .param("diningCommonsCode", "DLG")
                    .param("name", "Pancakes")
                    .param("station", "Breakfast")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // Assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(eq(item1));
    String expectedJson = mapper.writeValueAsString(item1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
