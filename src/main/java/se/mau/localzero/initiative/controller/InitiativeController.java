package se.mau.localzero.initiative.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import se.mau.localzero.domain.*;
import se.mau.localzero.initiative.dto.InitiativeDto;
import se.mau.localzero.initiative.dto.PostDto;
import se.mau.localzero.initiative.repository.InitiativeRepository;
import se.mau.localzero.initiative.service.CommentLikeService;
import se.mau.localzero.initiative.service.CommentService;
import se.mau.localzero.initiative.service.InitiativeCommentService;
import se.mau.localzero.initiative.service.InitiativeLikeService;
import se.mau.localzero.initiative.service.InitiativeService;
import se.mau.localzero.initiative.service.LikeService;
import se.mau.localzero.initiative.service.PostService;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.messaging.mediator.NotificationMediator;
import se.mau.localzero.messaging.repository.NotificationRepository;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/initiatives")
public class InitiativeController {
    private final InitiativeService initiativeService;
    private final PostService postService;
    private final UserRepository userRepository;
    private final CommentService commentService;
    private final LikeService likeService;
    private final InitiativeLikeService initiativeLikeService;
    private final CommentLikeService commentLikeService;
    private final InitiativeCommentService initiativeCommentService;
    private final InitiativeRepository initiativeRepository;
    private final NotificationMediator notificationMediator;
    private final NotificationRepository notificationRepository;

    public InitiativeController(InitiativeService initiativeService,
                                PostService postService,
                                UserRepository userRepository,
                                CommentService commentService,
                                LikeService likeService,
                                InitiativeLikeService initiativeLikeService,
                                CommentLikeService commentLikeService,
                                InitiativeCommentService initiativeCommentService,
                                InitiativeRepository initiativeRepository,
                                NotificationMediator notificationMediator,
                                NotificationRepository notificationRepository) {
        this.initiativeService = initiativeService;
        this.postService = postService;
        this.userRepository = userRepository;
        this.commentService = commentService;
        this.likeService = likeService;
        this.initiativeLikeService = initiativeLikeService;
        this.commentLikeService = commentLikeService;
        this.initiativeCommentService = initiativeCommentService;
        this.initiativeRepository = initiativeRepository;
        this.notificationMediator = notificationMediator;
        this.notificationRepository = notificationRepository;
    }

    //shows the page for creating a new initiative
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("initiativeDto", new InitiativeDto());
        return "create-initiative";
    }


    //handles the form submission for creating a new initiative, sends the data to InitiativeService and redirects the user

    @PostMapping("/create")
    public String createInitiative(@ModelAttribute("initiativeDto") InitiativeDto dto,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            //fetch the user from the database using the username from the authentication principal
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            //send DTO, user and community to service
            initiativeService.saveNewInitiative(dto, currentUser, currentUser.getCommunity());

            return "redirect:/initiatives?success";

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/create?error=" + e.getMessage();
        }
    }

    @GetMapping
    public String listInitiatives(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            if (currentUser != null) {
                List<Initiative> visibleList = initiativeRepository.findVisibleInitiatives(currentUser.getCommunity().getId());
                model.addAttribute("initiatives", visibleList);
                Set<Long> likedInitiativeIds = visibleList.stream()
                        .filter(i -> i.getLikes().stream().anyMatch(u -> u.getId().equals(currentUser.getId())))
                        .map(Initiative::getId)
                        .collect(Collectors.toSet());
                model.addAttribute("likedInitiativeIds", likedInitiativeIds);
                return "initiative-list";
            }
        }
        model.addAttribute("initiatives", initiativeRepository.findAll());
        return "initiative-list";
    }

    @PostMapping("/{id}/toggle-visibility")
    @ResponseBody
    public ResponseEntity<?> toggleVisibility(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        try {
            //get initiatives
            Initiative initiative = initiativeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Initiative not found"));

            //check owner
            if (!initiative.getCreatedBy().getUsername().equals(userDetails.getUsername())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You are not the owner of this initiative"));
            }

            //change status
            Visibility current = initiative.getVisibility();
            Visibility next = (current == Visibility.PUBLIC) ? Visibility.PUBLIC : Visibility.PUBLIC;
            initiative.setVisibility(next);
            initiativeRepository.save(initiative);

            return ResponseEntity.ok(Map.of(
                    "succes", true,
                    "newVisibility", next,
                    "message", "Visibility changed" + next
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public String viewInitiative(@PathVariable Long id, Model model,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        Initiative initiative = initiativeService.getAllInitiatives().stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        List<Post> posts = postService.getPostsByInitiative(id);
        model.addAttribute("initiative", initiative);
        model.addAttribute("posts", posts);
        model.addAttribute("postDto", new PostDto());
        model.addAttribute("initiativeLikeCount", initiativeLikeService.countLikes(id));
        model.addAttribute("initiativeComments", initiativeCommentService.getCommentsByInitiative(id));

        if (userDetails != null) {
            User currentUser = userRepository.findByUsername(userDetails.getUsername()).orElse(null);
            model.addAttribute("currentUser", currentUser);
            if (currentUser != null) {
                Set<Long> likedPostIds = posts.stream()
                        .filter(p -> likeService.hasUserLikedPost(p.getId(), currentUser.getId()))
                        .map(Post::getId)
                        .collect(Collectors.toSet());
                model.addAttribute("likedPostIds", likedPostIds);
                model.addAttribute("userLikedInitiative", initiativeLikeService.hasUserLiked(id, currentUser.getId()));

                Set<Long> allCommentIds = posts.stream()
                        .flatMap(p -> p.getComments().stream())
                        .map(Comment::getId)
                        .collect(Collectors.toSet());
                if (!allCommentIds.isEmpty()) {
                    model.addAttribute("commentLikeCounts", commentLikeService.countLikesByCommentIds(allCommentIds));
                    model.addAttribute("likedCommentIds", commentLikeService.findLikedCommentIdsByUser(currentUser.getId()));
                } else {
                    model.addAttribute("commentLikeCounts", new HashMap<Long, Long>());
                    model.addAttribute("likedCommentIds", Set.of());
                }
            }
        }

        return "initiative-detail";
    }

    @PostMapping("/{id}/posts")
    public String createPost(@PathVariable Long id,
                             @ModelAttribute("postDto") PostDto dto,
                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            postService.createPost(dto, currentUser, id);
            return "redirect:/initiatives/" + id + "?posted";

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/" + id + "?error=" + errorMessage;
        }
    }

    @PostMapping("/{initiativeId}/posts/{postId}/comments")
    public String addComment(@PathVariable Long initiativeId,
                             @PathVariable Long postId,
                             @RequestParam("content") String content,
                             @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            commentService.addComment(postId, currentUser, content);
            return "redirect:/initiatives/" + initiativeId + "?commented";
        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/" + initiativeId + "?error=" + errorMessage;
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteInitiative(@PathVariable Long id,
                                   @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            initiativeService.deleteInitiative(id, currentUser);
            return "redirect:/initiatives?success";
        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives?error=" + errorMessage;
        }
    }

    @PostMapping("/{id}/toggle-official")
    public String toggleOfficial(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            initiativeService.toggleOfficialStatus(id, currentUser);
            return "redirect:/initiatives?success";
        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives?error=" + errorMessage;
        }
    }

    @PostMapping("/{id}/like")
    @ResponseBody
    public ResponseEntity<?> toggleLikeAjax(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Initiative initiative = initiativeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Initiative not found"));

            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean alreadyLiked = initiative.getLikes().stream()
                    .anyMatch(u -> u.getId().equals(user.getId()));
            boolean isLiked;

            if (alreadyLiked) {
                initiative.removeLike(user);
                isLiked = false;
            } else {
                initiative.addLike(user);
                isLiked = true;

                if (!initiative.getCreatedBy().getId().equals(user.getId())) {
                    String title = user.getUsername() + " liked your initiative";
                    String message = user.getUsername() + " liked '" + initiative.getTitle() + "'";

                    Notification notification = new Notification(
                            title,
                            message,
                            initiative.getCreatedBy(),
                            NotificationEntityType.INITIATIVE,
                            initiative.getId()
                    );

                    notificationRepository.save(notification);
                }

            }

            initiativeRepository.save(initiative);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "newLikeCount", initiative.getLikes().size(),
                    "isLiked", isLiked
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @PostMapping(value = "/{id}/join", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> toggleParticipationAjax(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Initiative initiative = initiativeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Initiative not found"));
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            boolean isParticipating;

            if (initiative.getParticipants().contains(user)) {
                initiative.removeParticipant(user);
                isParticipating = false;
            } else {
                initiative.addParticipant(user);
                isParticipating = true;

                if (!initiative.getCreatedBy().getId().equals(user.getId())) {
                    String title = user.getUsername() + " joined your initiative";
                    String message = user.getUsername() + " has joined '" + initiative.getTitle() + "'";

                    Notification notification = new Notification(
                            title,
                            message,
                            initiative.getCreatedBy(),
                            NotificationEntityType.INITIATIVE,
                            initiative.getId()
                    );

                    notificationRepository.save(notification);
                }
            }

            initiativeRepository.save(initiative);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "newParticipantCount", initiative.getParticipants().size(),
                    "isParticipating", isParticipating
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));

        }
    }

    @PostMapping("/{id}/join")
    public String joinInitiative(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            initiativeService.joinInitiative(id, currentUser);
            return "redirect:/initiatives?joined";

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives?error=" + errorMessage;
        }
    }

    @PostMapping("/{id}/leave")
    public String leaveInitiative(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            initiativeService.leaveInitiative(id, currentUser);
            return "redirect:/initiatives?left";

        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives?error=" + errorMessage;
        }
    }

    @PostMapping("/{id}/comments")
    public String addInitiativeComment(@PathVariable Long id,
                                       @RequestParam("content") String content,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            initiativeCommentService.addComment(id, currentUser, content);
            return "redirect:/initiatives/" + id + "?commented";
        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/" + id + "?error=" + errorMessage;
        }
    }

    @PostMapping("/{id}/likes")
    public String toggleInitiativeLike(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            initiativeLikeService.toggleLike(id, currentUser);
            return "redirect:/initiatives/" + id + "?liked";
        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/" + id + "?error=" + errorMessage;
        }
    }

    @PostMapping("/{initiativeId}/posts/{postId}/likes")
    public String togglePostLike(@PathVariable Long initiativeId,
                                 @PathVariable Long postId,
                                 @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            likeService.toggleLike(postId, currentUser);
            return "redirect:/initiatives/" + initiativeId + "?liked";
        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/" + initiativeId + "?error=" + errorMessage;
        }
    }

    @PostMapping("/{initiativeId}/posts/{postId}/comments/{commentId}/likes")
    public String toggleCommentLike(@PathVariable Long initiativeId,
                                    @PathVariable Long postId,
                                    @PathVariable Long commentId,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            commentLikeService.toggleLike(commentId, currentUser);
            return "redirect:/initiatives/" + initiativeId + "?liked";
        } catch (Exception e) {
            String errorMessage = URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            return "redirect:/initiatives/" + initiativeId + "?error=" + errorMessage;
        }
    }
}
