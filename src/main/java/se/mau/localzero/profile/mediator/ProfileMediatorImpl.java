package se.mau.localzero.profile.mediator;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.mau.localzero.auth.repository.UserRepository;
import se.mau.localzero.domain.Initiative;
import se.mau.localzero.domain.User;
import se.mau.localzero.exception.UserNotFoundException;
import se.mau.localzero.initiative.repository.InitiativeRepository;
import se.mau.localzero.profile.dto.InitiativeSummaryDTO;
import se.mau.localzero.profile.dto.ProfileDTO;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfileMediatorImpl implements ProfileMediator {

    private final UserRepository userRepository;
    private final InitiativeRepository initiativeRepository;

    public ProfileMediatorImpl(UserRepository userRepository, InitiativeRepository initiativeRepository) {
        this.userRepository = userRepository;
        this.initiativeRepository = initiativeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        List<InitiativeSummaryDTO> created = user.getCreatedInitiatives().stream()

                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());

        List<InitiativeSummaryDTO> joined = user.getParticipatingInitiatives().stream()

                .sorted((a, b) -> b.getId().compareTo(a.getId()))
                .map(this::convertToSummaryDTO)
                .collect(Collectors.toList());

        return ProfileDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .postCount(user.getPosts().size())
                .initiativeCount(joined.size())
                .createdInitiativeCount(created.size())
                .createdInitiatives(created)
                .joinedInitiatives(joined)
                .build();
    }

    @Override
    @Transactional
    public void updateUserProfile(Long userId, ProfileDTO profileDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        
        user.setEmail(profileDTO.getEmail());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void userJoinInitiative(Long userId, Long initiativeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        user.joinInitiative(initiative);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void userLeaveInitiative(Long userId, Long initiativeId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        Initiative initiative = initiativeRepository.findById(initiativeId)
                .orElseThrow(() -> new RuntimeException("Initiative not found"));

        user.leaveInitiative(initiative);
        userRepository.save(user);
    }

    @Override
    public int getUserPostCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return user.getPosts().size();
    }

    @Override
    public int getUserInitiativeCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
        return user.getParticipatingInitiatives().size();
    }

    private InitiativeSummaryDTO convertToSummaryDTO(Initiative initiative) {
        return InitiativeSummaryDTO.builder()
                .id(initiative.getId())
                .title(initiative.getTitle())
                .description(initiative.getDescription())
                .startDate(initiative.getStartDate())
                .endDate(initiative.getEndDate())
                .category(initiative.getCategory())
                .build();
    }
}
