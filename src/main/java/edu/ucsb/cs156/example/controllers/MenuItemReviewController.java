package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for menuitemreview */
@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  /**
   * List all Menu Item reviews
   *
   * @return an iterable of MenuItemReview
   */
  @Operation(summary = "List all Menu Item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReviews() {
    Iterable<MenuItemReview> menuItemReviews = menuItemReviewRepository.findAll();
    return menuItemReviews;
  }

  /**
   * Create a new Menu Item Review
   *
   * @param itemId the menu item id
   * @param reviewerEmail the email of the reviewer
   * @param stars the number of stars (0-5) for the review
   * @param dateReviewed the date of the review
   * @param comments any additional comments for the review
   * @return the saved Menu Item Review
   */
  @Operation(summary = "Create a new Menu Item Review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId") @RequestParam Long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(name = "comments") @RequestParam String comments,
      @Parameter(
              name = "dateReviewed",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed)
      throws JsonProcessingException {

    log.info("dateReviewed={}", dateReviewed);
    MenuItemReview menuItemReview = new MenuItemReview();
    menuItemReview.setItemId(itemId);
    menuItemReview.setReviewerEmail(reviewerEmail);
    menuItemReview.setStars(stars);
    menuItemReview.setDateReviewed(dateReviewed);
    menuItemReview.setComments(comments);

    MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);

    return savedMenuItemReview;
  }

  /**
   * Get a single MenuItemReview by id
   *
   * @param id the id of the MenuItemReview
   * @return a MenuItemReview
   */
  @Operation(summary = "Get a single menu item review")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReview getById(@Parameter(name = "id") @RequestParam Long id) {
    MenuItemReview menuItemReview =
        menuItemReviewRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReview.class, id));

    return menuItemReview;
  }
}
