package com.winwin.winwin.payload;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author ArvindKhatik
 *
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserSignInResponsePayload {
	AuthenticationResultType authResult;
	UserPayload userDetails;
}