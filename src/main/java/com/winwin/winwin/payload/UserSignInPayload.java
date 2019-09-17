package com.winwin.winwin.payload;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindKhatik
 * @version 1.0
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class UserSignInPayload {
	private String userName;
	private String password;
	private String newPassword;
	private Boolean isNewUser;
	private String accessToken;
	private String refreshToken;
	private String confirmationCode;
	
	//Added for Elastic Search Users
	private String role;
	private String fullName;
}
