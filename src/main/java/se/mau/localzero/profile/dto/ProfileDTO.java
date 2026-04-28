package se.mau.localzero.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDTO {
	private Long id;
	private String username;
	private String email;
	private int postCount;
	private int initiativeCount;
	private int createdInitiativeCount;
}
