package com.winwin.winwin.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDeleteUserResult;
import com.amazonaws.services.cognitoidp.model.AdminDisableUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminDisableUserResult;
import com.amazonaws.services.cognitoidp.model.AdminEnableUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminResetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeRequest;
import com.amazonaws.services.cognitoidp.model.AdminRespondToAuthChallengeResult;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesRequest;
import com.amazonaws.services.cognitoidp.model.AdminUpdateUserAttributesResult;
import com.amazonaws.services.cognitoidp.model.AttributeType;
import com.amazonaws.services.cognitoidp.model.AuthFlowType;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.cognitoidp.model.ChallengeNameType;
import com.amazonaws.services.cognitoidp.model.ChangePasswordRequest;
import com.amazonaws.services.cognitoidp.model.ChangePasswordResult;
import com.amazonaws.services.cognitoidp.model.CodeDeliveryFailureException;
import com.amazonaws.services.cognitoidp.model.CodeMismatchException;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.ExpiredCodeException;
import com.amazonaws.services.cognitoidp.model.GetUserRequest;
import com.amazonaws.services.cognitoidp.model.GetUserResult;
import com.amazonaws.services.cognitoidp.model.InternalErrorException;
import com.amazonaws.services.cognitoidp.model.InvalidEmailRoleAccessPolicyException;
import com.amazonaws.services.cognitoidp.model.InvalidParameterException;
import com.amazonaws.services.cognitoidp.model.InvalidPasswordException;
import com.amazonaws.services.cognitoidp.model.InvalidUserPoolConfigurationException;
import com.amazonaws.services.cognitoidp.model.LimitExceededException;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.NotAuthorizedException;
import com.amazonaws.services.cognitoidp.model.PasswordResetRequiredException;
import com.amazonaws.services.cognitoidp.model.ResendConfirmationCodeRequest;
import com.amazonaws.services.cognitoidp.model.ResourceNotFoundException;
import com.amazonaws.services.cognitoidp.model.TooManyFailedAttemptsException;
import com.amazonaws.services.cognitoidp.model.TooManyRequestsException;
import com.amazonaws.services.cognitoidp.model.UnsupportedUserStateException;
import com.amazonaws.services.cognitoidp.model.UserNotConfirmedException;
import com.amazonaws.services.cognitoidp.model.UserNotFoundException;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.amazonaws.services.cognitoidp.model.UsernameExistsException;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.constants.UserConstants;
import com.winwin.winwin.exception.ExceptionResponse;
import com.winwin.winwin.exception.UserException;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.payload.UserSignInPayload;
import com.winwin.winwin.service.KibanaUserService;
import com.winwin.winwin.service.UserService;

/**
 * @author ArvindKhatik
 * @version 1.0
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	protected CustomMessageSource customMessageSource;

	@Autowired
	private KibanaUserService kibanaUserService;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);

	private static final String USER_NAME = "USERNAME";
	private static final String PASS_WORD = "PASSWORD";
	private static final String REFRESH_TOKEN = "REFRESH_TOKEN";
	private static final String NEW_PASS_WORD_REQUIRED = "NEW_PASSWORD_REQUIRED";
	private static final String NEW_PASS_WORD = "NEW_PASSWORD";

	private EnvironmentVariableCredentialsProvider envCredentialsProvider = new EnvironmentVariableCredentialsProvider();

	/**
	 * The below method createUser creates new user in AWS COGNITO with
	 * email_verified =true and custom attributes and accepts a UserPayload and
	 * requires Environment Variables such as
	 * AWS_COGNITO_USER_POOL_ID,AWS_REGION The new user will get an welcome
	 * email with Temporary password (here for COGNITO the user status will be
	 * FORCE_CHANGE_PASSWORD
	 */
	@Override
	public void createUser(UserPayload payload, ExceptionResponse response) throws UserException {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail())
				.withUserAttributes(new AttributeType().withName("custom:role").withValue(payload.getRole()),
						new AttributeType().withName("custom:team").withValue(payload.getTeam()),
						new AttributeType().withName("custom:isActive").withValue(payload.getIsActive()),
						new AttributeType().withName("picture").withValue(payload.getImageUrl()),
						new AttributeType().withName("name").withValue(payload.getUserDisplayName()),
						new AttributeType().withName("email").withValue(payload.getEmail()),
						new AttributeType().withName("email_verified").withValue("true"))
				.withDesiredDeliveryMediums(DeliveryMediumType.EMAIL).withForceAliasCreation(Boolean.FALSE);

		try {
			cognitoClient.adminCreateUser(cognitoRequest);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException | UsernameExistsException
				| InvalidPasswordException | NotAuthorizedException | TooManyRequestsException
				| UnsupportedUserStateException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();

	}

	/**
	 * The below method createKibanaUser creates new user with default role as
	 * 'Reader' in AWS COGNITO with email_verified =true and custom attributes
	 * and accepts a UserPayload and requires Environment Variables such as
	 * AWS_COGNITO_USER_POOL_ID,AWS_REGION The new user will get an welcome
	 * email with Temporary password (here for COGNITO the user status will be
	 * FORCE_CHANGE_PASSWORD
	 */
	@Override
	public void createKibanaUser(UserPayload payload, ExceptionResponse response) throws UserException {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail())
				.withUserAttributes(new AttributeType().withName("custom:role").withValue(UserConstants.ROLE_READER),
						new AttributeType().withName("custom:isActive").withValue("true"),
						new AttributeType().withName("name").withValue(payload.getUserDisplayName()),
						new AttributeType().withName("email").withValue(payload.getEmail()),
						new AttributeType().withName("email_verified").withValue("true"))
				.withDesiredDeliveryMediums(DeliveryMediumType.EMAIL).withForceAliasCreation(Boolean.FALSE);

		try {
			cognitoClient.adminCreateUser(cognitoRequest);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException | UsernameExistsException
				| InvalidPasswordException | NotAuthorizedException | TooManyRequestsException
				| UnsupportedUserStateException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();

	}

	/**
	 * The below method resendUserInvitation resends the user invitation for
	 * newly created users in AWS COGNITO with user status as
	 * FORCE_CHANGE_PASSWORD i.e. case 'a': when user didn't receive welcome
	 * email with temp password? if the user didn't get email then the admin
	 * again need to create the same user (here for cognito the user status will
	 * be FORCE_CHANGE_PASSWORD)
	 * 
	 * case 'b': What will happen when the user receives an welcome email? Once
	 * the user will get the welcome email, he need to login with the received
	 * credentials and once he try to login with temp password he will have to
	 * pass temp password as old password as well as new proposed password.
	 */
	@SuppressWarnings("unused")
	@Override
	public void resendUserInvitation(UserPayload payload, ExceptionResponse response) throws UserException {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail())
				.withUserAttributes(new AttributeType().withName("custom:role").withValue(payload.getRole()),
						new AttributeType().withName("custom:team").withValue(payload.getTeam()),
						new AttributeType().withName("custom:isActive").withValue(payload.getIsActive()),
						new AttributeType().withName("picture").withValue(payload.getImageUrl()),
						new AttributeType().withName("name").withValue(payload.getUserDisplayName()),
						new AttributeType().withName("email").withValue(payload.getEmail()),
						new AttributeType().withName("email_verified").withValue("true"))
				.withDesiredDeliveryMediums(DeliveryMediumType.EMAIL).withForceAliasCreation(Boolean.FALSE);

		/*
		 * Added cognitoRequest.setMessageAction("RESEND"); if user is new i.e.
		 * user status as force_change_password and hit the create user again
		 * then setting cognito request with messageAction as RESEND will
		 * resends the welcome message again with extending account user
		 * expiration limit
		 */
		cognitoRequest.setMessageAction("RESEND");

		try {
			cognitoClient.adminCreateUser(cognitoRequest);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException | UsernameExistsException
				| InvalidPasswordException | NotAuthorizedException | TooManyRequestsException
				| UnsupportedUserStateException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();

	}

	/**
	 * The below method getUserInfo return the user info according to the passed
	 * userName.
	 */
	@Override
	public UserPayload getUserInfo(String userName, ExceptionResponse response) {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		AdminGetUserRequest cognitoGetUserRequest = new AdminGetUserRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(userName);
		UserPayload payload = null;
		try {
			AdminGetUserResult userResult = cognitoClient.adminGetUser(cognitoGetUserRequest);
			List<AttributeType> userAttributes = userResult.getUserAttributes();
			if (null != userAttributes) {
				payload = new UserPayload();
				for (AttributeType attribute : userAttributes) {
					if (attribute.getName().equals("custom:role")) {
						payload.setRole(attribute.getValue());
					} else if (attribute.getName().equals("custom:team")) {
						payload.setTeam(attribute.getValue());
					} else if (attribute.getName().equals("custom:isActive")) {
						payload.setIsActive(attribute.getValue());
					} else if (attribute.getName().equals("picture")) {
						payload.setImageUrl(attribute.getValue());
					} else if (attribute.getName().equals("name")) {
						payload.setUserDisplayName(attribute.getValue());
					} else if (attribute.getName().equals("email")) {
						payload.setEmail(attribute.getValue());
					}
				}

			}
		} catch (ResourceNotFoundException | InvalidParameterException | TooManyRequestsException
				| NotAuthorizedException | UserNotFoundException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		cognitoClient.shutdown();

		return payload;
	}

	/**
	 * The below method updateUserInfo update the user info according to the
	 * passed UserPayload. Update internal user in Elastic Search DB when user's
	 * role is updated in COGNITO User Pool: AWS_COGNITO_USER_POOL_ID
	 */
	@Override
	public void updateUserInfo(UserPayload payload, ExceptionResponse response) {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		AdminUpdateUserAttributesRequest cognitoUpdateUserRequest = new AdminUpdateUserAttributesRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail());

		List<AttributeType> userAttributes = new ArrayList<AttributeType>();
		if (!StringUtils.isEmpty(payload.getUserDisplayName())) {
			AttributeType attribute = new AttributeType();
			attribute.setName("name");
			attribute.setValue(payload.getUserDisplayName());
			userAttributes.add(attribute);
		}

		if (!StringUtils.isEmpty(payload.getTeam())) {
			AttributeType attribute = new AttributeType();
			attribute.setName("custom:team");
			attribute.setValue(payload.getTeam());
			userAttributes.add(attribute);
		}
		if (!StringUtils.isEmpty(payload.getRole())) {
			AttributeType attribute = new AttributeType();
			attribute.setName("custom:role");
			attribute.setValue(payload.getRole());
			userAttributes.add(attribute);
		}
		if (!StringUtils.isEmpty(payload.getIsActive())) {
			AttributeType attribute = new AttributeType();
			attribute.setName("custom:isActive");
			attribute.setValue(payload.getIsActive());
			userAttributes.add(attribute);
		}
		if (!StringUtils.isEmpty(payload.getImageUrl())) {
			AttributeType attribute = new AttributeType();
			attribute.setName("picture");
			attribute.setValue(payload.getImageUrl());
			userAttributes.add(attribute);
		}
		cognitoUpdateUserRequest.withUserAttributes(userAttributes);
		try {
			AdminUpdateUserAttributesResult userResult = cognitoClient
					.adminUpdateUserAttributes(cognitoUpdateUserRequest);

			// Update internal user in Elastic Search when user's role is
			// updated in COGNITO User Pool: AWS_COGNITO_USER_POOL_ID
			if (null != userResult) {
				if (!StringUtils.isEmpty(payload.getRole())) {
					// call getInternalKibanaUserRole to get user role
					UserSignInPayload userSignInPayload = new UserSignInPayload();
					userSignInPayload.setUserName(payload.getEmail());
					userSignInPayload.setRole(payload.getRole());
					userSignInPayload.setFullName(payload.getUserDisplayName());
					if (!(StringUtils.isEmpty(userSignInPayload.getUserName()))
							&& !(StringUtils.isEmpty(userSignInPayload.getRole()))) {
						// call KIBANA service method to update password of
						// internal user
						kibanaUserService.createInternalKibanaUser(userSignInPayload, response);
					}
				}
			} // end of if
		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException | NotAuthorizedException
				| TooManyRequestsException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();

	}

	/**
	 * The below method getUserList returns the list of users whose AWS Status
	 * is CONFIRMED.
	 */
	@Override
	public List<UserPayload> getUserList(ExceptionResponse response) {
		List<UserPayload> payloadList = new ArrayList<>();
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		ListUsersRequest cognitoGetListUserRequest = new ListUsersRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID"));
		try {
			ListUsersResult userResult = cognitoClient.listUsers(cognitoGetListUserRequest);
			if (null != userResult) {
				List<UserType> usersList = userResult.getUsers();

				if (null != usersList) {
					for (UserType user : usersList) {
						List<AttributeType> userAttributes = user.getAttributes();
						UserPayload payload = new UserPayload();
						// check user enabled / disabled status
						String isActive = user.getEnabled().toString();

						for (AttributeType attribute : userAttributes) {
							if (attribute.getName().equals("custom:role")) {
								payload.setRole(attribute.getValue());
							} else if (attribute.getName().equals("custom:team")) {
								payload.setTeam(attribute.getValue());
							} else if (attribute.getName().equals("picture")) {
								payload.setImageUrl(attribute.getValue());
							} else if (attribute.getName().equals("name")) {
								payload.setUserDisplayName(attribute.getValue());
							} else if (attribute.getName().equals("email")) {
								payload.setEmail(attribute.getValue());
							}
							payload.setIsActive(isActive);
						}
						payloadList.add(payload);
					}
				}
			}

		} catch (ResourceNotFoundException | InvalidParameterException | TooManyRequestsException
				| NotAuthorizedException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		cognitoClient.shutdown();

		return payloadList;
	}

	/**
	 * The below method getUserStatus return the user status in COGNITO
	 * according to the passed userName.
	 */
	@Override
	public String getUserStatus(String userName, ExceptionResponse response) {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		AdminGetUserRequest cognitoGetUserRequest = new AdminGetUserRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(userName);

		String userStatus = null;
		try {
			AdminGetUserResult userResult = cognitoClient.adminGetUser(cognitoGetUserRequest);
			if (null != userResult) {
				userStatus = userResult.getUserStatus();
			}
		} catch (ResourceNotFoundException | InvalidParameterException | TooManyRequestsException
				| NotAuthorizedException | UserNotFoundException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setException(e);
			response.setStatusCode(HttpStatus.BAD_REQUEST);

		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setException(e);
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

		}
		cognitoClient.shutdown();

		return userStatus;
	}

	/**
	 * The below method userSignIn accepts UserSignInPayload and checks whether
	 * 
	 * case a: if the user is trying to login with temporary password generated
	 * by AWS through createUser method, i.e. user will get temporary password
	 * via new user invitation mail and returns the accessToken if the
	 * credentials are valid.
	 * 
	 * case b: if the user is confirmed in AWS cognito and trying to login with
	 * permanent password than returns the accessToken if the credentials are
	 * valid.
	 * 
	 * case c: if the user is already logged in and have REFRESH_TOKEN than
	 * returns the new accessToken which will valid until the session expires
	 * after given time.
	 */
	@Override
	public AuthenticationResultType userSignIn(UserSignInPayload payload, ExceptionResponse response) {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();

		final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
		final Map<String, String> authParams = new HashMap<>();

		if (!StringUtils.isEmpty(payload.getRefreshToken())) {
			authParams.put(REFRESH_TOKEN, payload.getRefreshToken());
			authRequest.withAuthFlow(AuthFlowType.REFRESH_TOKEN_AUTH);
		} else {
			authParams.put(USER_NAME, payload.getUserName());
			authParams.put(PASS_WORD, payload.getPassword());
			authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH);
		}

		authRequest.withClientId(System.getenv("AWS_COGNITO_CLIENT_ID"))
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withAuthParameters(authParams);

		AuthenticationResultType authenticationResult = null;
		try {
			// The method adminInitiateAuth returns a challenge as
			// NEW_PASSWORD_REQUIRED if the user is trying to login with
			// temporary
			// password
			AdminInitiateAuthResult result = cognitoClient.adminInitiateAuth(authRequest);

			if (!StringUtils.isEmpty(result.getChallengeName())) {
				if (NEW_PASS_WORD_REQUIRED.equals(result.getChallengeName())) {
					if (null == payload.getNewPassword()) {
						LOGGER.info(customMessageSource.getMessage("org.user.exception.signIn"));
						throw new UserException(customMessageSource.getMessage("org.user.exception.signIn"));
					} else {
						final Map<String, String> challengeResponses = new HashMap<>();
						challengeResponses.put(USER_NAME, payload.getUserName());
						challengeResponses.put(PASS_WORD, payload.getPassword());
						challengeResponses.put(NEW_PASS_WORD, payload.getNewPassword());

						final AdminRespondToAuthChallengeRequest request = new AdminRespondToAuthChallengeRequest();
						request.withChallengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
								.withChallengeResponses(challengeResponses)
								.withClientId(System.getenv("AWS_COGNITO_CLIENT_ID"))
								.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID"))
								.withSession(result.getSession());

						AdminRespondToAuthChallengeResult resultChallenge = cognitoClient
								.adminRespondToAuthChallenge(request);
						authenticationResult = resultChallenge.getAuthenticationResult();

						// Create internal user in Elastic Search when the new
						// user is
						// confirmed in COGNITO
						if (null != authenticationResult) {
							// call getLoggedInUser to get user role
							UserPayload userPayload = getLoggedInUser(authenticationResult.getAccessToken(), response);
							if (null != userPayload && !(StringUtils.isEmpty(userPayload.getRole()))) {
								payload.setRole(userPayload.getRole());
								payload.setFullName(userPayload.getUserDisplayName());
								// call kibana service method to create new
								// internal user
								kibanaUserService.createInternalKibanaUser(payload, response);
							}
						} // end of if
					}
				}
			} else {
				authenticationResult = result.getAuthenticationResult();
			}
		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException
				| InvalidUserPoolConfigurationException | InvalidPasswordException | NotAuthorizedException
				| TooManyRequestsException | UserNotConfirmedException | PasswordResetRequiredException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();

		return authenticationResult;
	}

	/**
	 * The below method resetUserPassword accepts UserPayload and reset the
	 * Existing User Credentials. The user will get an confirmation code through
	 * email..(here for cognito the user status will become CONFIRMED to
	 * RESET_REQUIRED)
	 * 
	 * case 'a': when user didn't receive confirmation code on email? if the
	 * user didn't get email then the user again need to call the same method
	 * resetUserPassword( here for cognito the user status will remain as
	 * RESET_REQUIRED)
	 * 
	 * case 'b': What will happen when the user receives an email with
	 * confirmation code? Once the user will get the email with confirmation
	 * code, he need to pass confirmation code and new proposed password.
	 * 
	 * After the success for the above case 2.b user can login with new
	 * credentials.( here for cognito the user status will become RESET_REQUIRED
	 * to CONFIRMED)
	 */
	@Override
	public void resetUserPassword(UserPayload payload, ExceptionResponse response) {

		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		AdminResetUserPasswordRequest cognitoRequest = new AdminResetUserPasswordRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail());

		try {
			cognitoClient.adminResetUserPassword(cognitoRequest);
		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException | NotAuthorizedException
				| TooManyRequestsException | LimitExceededException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();
	}

	/**
	 * The below method confirmResetPassword is the next step of
	 * resetUserPassword which accepts confimationCode in UserSignInPayload.
	 * Once the user will get the email with confirmation code, he need to pass
	 * confirmation code and new proposed password.
	 * 
	 * After the success user can login with new credentials.( here for cognito
	 * the user status will become RESET_REQUIRED to CONFIRMED) and The Proposed
	 * Password will also get updated in Elastic Search Internal User DB.
	 * 
	 */
	@Override
	public void confirmResetPassword(UserSignInPayload payload, ExceptionResponse response) {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		ConfirmForgotPasswordRequest cognitoRequest = new ConfirmForgotPasswordRequest()
				.withClientId(System.getenv("AWS_COGNITO_CLIENT_ID"))
				.withConfirmationCode(payload.getConfirmationCode()).withPassword(payload.getNewPassword())
				.withUsername(payload.getUserName());

		try {
			ConfirmForgotPasswordResult conForgotUserPassResult = cognitoClient.confirmForgotPassword(cognitoRequest);

			// Create internal user in KIBANA after user is confirmed in COGNITO
			// with new password
			if (null != conForgotUserPassResult) {
				// call KIBANA service method to update password of internal
				// user
				kibanaUserService.changePasswordForInternalKibanaUser(payload, response);
			} // end of if

		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException
				| TooManyFailedAttemptsException | InvalidPasswordException | NotAuthorizedException
				| TooManyRequestsException | UserNotConfirmedException | LimitExceededException | CodeMismatchException
				| ExpiredCodeException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();
	}

	/**
	 * The below method resendConfirmationCode resends the confirmation
	 */
	@Override
	public void resendConfirmationCode(UserPayload payload, ExceptionResponse response) {

		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		ResendConfirmationCodeRequest cognitoRequest = new ResendConfirmationCodeRequest()
				.withClientId(System.getenv("AWS_COGNITO_CLIENT_ID")).withUsername(payload.getEmail());
		try {
			cognitoClient.resendConfirmationCode(cognitoRequest);
		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException
				| CodeDeliveryFailureException | InvalidPasswordException | NotAuthorizedException
				| TooManyRequestsException | InvalidEmailRoleAccessPolicyException | LimitExceededException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();
	}

	/**
	 * The below method changePassword accepts the accessToken in
	 * UserSignInPayload and changes password for User in
	 * AWS_COGNITO_USER_POOL_ID. The method also update password in Elastic
	 * Search Internal User DB after successfully updated in
	 * AWS_COGNITO_USER_POOL_ID.
	 */
	@Override
	public void changePassword(UserSignInPayload payload, ExceptionResponse response) {

		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		ChangePasswordRequest cognitoRequest = new ChangePasswordRequest().withAccessToken(payload.getAccessToken())
				.withPreviousPassword(payload.getPassword()).withProposedPassword(payload.getNewPassword());

		try {
			ChangePasswordResult changePassResult = cognitoClient.changePassword(cognitoRequest);
			// Create internal user in Elastic Search user's password updated
			// with
			// Proposed Password
			if (null != changePassResult) {

				// Get Logged In User details to set userName and role
				UserPayload userPayload = getLoggedInUser(payload.getAccessToken(), response);

				if (null != userPayload && !(StringUtils.isEmpty(userPayload.getEmail()))) {
					payload.setUserName(userPayload.getEmail());

					// call KIBANA service method to update password of internal
					// user
					kibanaUserService.changePasswordForInternalKibanaUser(payload, response);
				}
			} // end of if
		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException
				| InvalidPasswordException | NotAuthorizedException | TooManyRequestsException
				| UserNotConfirmedException | LimitExceededException | PasswordResetRequiredException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}
		cognitoClient.shutdown();
	}

	/**
	 * The below method deleteUser accepts the UserName in UserPayload and
	 * delete the user from COGNITO User Pool : AWS_COGNITO_USER_POOL_ID. After
	 * the successful deletion of user from COGNITO it will also get deleted
	 * from Elastic Search Internal User DB.
	 */
	@Override
	public void deleteUser(UserPayload payload, ExceptionResponse response) {

		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		AdminDeleteUserRequest cognitoRequest = new AdminDeleteUserRequest()
				.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail());
		try {
			AdminDeleteUserResult delPassResult = cognitoClient.adminDeleteUser(cognitoRequest);
			// Delete internal user from Elastic Search DB when user is deleted
			// from AWS_COGNITO_USER_POOL_ID
			if (null != delPassResult) {
				// call getInternalKibanaUserRole to get user role
				UserSignInPayload userSignInPayload = new UserSignInPayload();
				userSignInPayload.setUserName(payload.getEmail());

				// call KIBANA service method to delete internal
				// user from Elastic Search DB
				kibanaUserService.deleteInternalKibanaUser(userSignInPayload, response);
			} // end of if
			cognitoClient.shutdown();
		} catch (ResourceNotFoundException | InvalidParameterException | TooManyRequestsException
				| NotAuthorizedException | UserNotFoundException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		cognitoClient.shutdown();
	}

	/**
	 * The below method getLoggedInUser accepts the accessToken and returns the
	 * current logged in user information
	 */
	@Override
	public UserPayload getLoggedInUser(String accessToken, ExceptionResponse response) {

		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		GetUserRequest cognitoRequest = new GetUserRequest().withAccessToken(accessToken);
		UserPayload payload = null;

		try {
			GetUserResult getUserResult = cognitoClient.getUser(cognitoRequest);
			if (null != getUserResult) {
				List<AttributeType> userAttributes = getUserResult.getUserAttributes();
				if (null != userAttributes) {
					payload = new UserPayload();
					for (AttributeType attribute : userAttributes) {
						if (attribute.getName().equals("custom:role")) {
							payload.setRole(attribute.getValue());
						} else if (attribute.getName().equals("custom:team")) {
							payload.setTeam(attribute.getValue());
						} else if (attribute.getName().equals("custom:isActive")) {
							payload.setIsActive(attribute.getValue());
						} else if (attribute.getName().equals("picture")) {
							payload.setImageUrl(attribute.getValue());
						} else if (attribute.getName().equals("name")) {
							payload.setUserDisplayName(attribute.getValue());
						} else if (attribute.getName().equals("email")) {
							payload.setEmail(attribute.getValue());
						}
					}
				}
			}
			cognitoClient.shutdown();

		} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException | NotAuthorizedException
				| TooManyRequestsException | UserNotConfirmedException | PasswordResetRequiredException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		} catch (InternalErrorException e) {
			response.setErrorMessage(e.getErrorMessage());
			response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			response.setErrorMessage(e.getMessage());
			response.setStatusCode(HttpStatus.BAD_REQUEST);
		}

		return payload;
	}

	/**
	 * Enable User in available in AWS_COGNITO_USER_POOL_ID Reset User Password
	 * Once User is Enabled in AWS_COGNITO_USER_POOL_ID to support enable /
	 * disable feature in Elastic Search Internal User Management
	 * 
	 * @param payloadList
	 * @param response
	 */
	@Override
	public void enableUser(List<String> userNames, ExceptionResponse response) {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		for (String userName : userNames) {
			AdminEnableUserRequest adminEnableUserRequest = new AdminEnableUserRequest()
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(userName);
			try {
				cognitoClient.adminEnableUser(adminEnableUserRequest);
				// Call resetUserPassword Flow Once User is Enabled In
				// AWS_COGNITO_USER_POOL_ID
				// Added this flow to support enable / disable feature Elastic
				// Search Internal User Management
				UserPayload payload = new UserPayload();
				payload.setEmail(userName);
				resetUserPassword(payload, response);
			} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException
					| NotAuthorizedException | TooManyRequestsException e) {
				response.setErrorMessage(e.getErrorMessage());
				response.setStatusCode(HttpStatus.BAD_REQUEST);
			} catch (InternalErrorException e) {
				response.setErrorMessage(e.getErrorMessage());
				response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (Exception e) {
				response.setErrorMessage(e.getMessage());
				response.setStatusCode(HttpStatus.BAD_REQUEST);
			}
			cognitoClient.shutdown();

		}

	}

	/**
	 * Disable User from the mentioned AWS_COGNITO_USER_POOL_ID Delete the user
	 * from Elastic Search Internal DB once it is disabled in
	 * AWS_COGNITO_USER_POOL_ID
	 * 
	 * @param payloadList
	 * @param response
	 */
	@Override
	public void disableUser(List<String> userNames, ExceptionResponse response) {
		AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
		for (String userName : userNames) {
			AdminDisableUserRequest adminDisableUserRequest = new AdminDisableUserRequest()
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(userName);
			try {
				AdminDisableUserResult adminDisableUserResult = cognitoClient.adminDisableUser(adminDisableUserRequest);
				// Delete internal user from Elastic Search DB when user is
				// disabled
				// in AWS_COGNITO_USER_POOL_ID
				// with Proposed Password
				if (null != adminDisableUserResult) {
					// call getInternalKibanaUserRole to get user role
					UserSignInPayload userSignInPayload = new UserSignInPayload();
					userSignInPayload.setUserName(userName);

					// call KIBANA service method to delete internal
					// user from Elastic Search DB
					kibanaUserService.deleteInternalKibanaUser(userSignInPayload, response);
				} // end of if
			} catch (ResourceNotFoundException | InvalidParameterException | UserNotFoundException
					| NotAuthorizedException | TooManyRequestsException e) {
				response.setErrorMessage(e.getErrorMessage());
				response.setStatusCode(HttpStatus.BAD_REQUEST);
			} catch (InternalErrorException e) {
				response.setErrorMessage(e.getErrorMessage());
				response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			} catch (Exception e) {
				response.setErrorMessage(e.getMessage());
				response.setStatusCode(HttpStatus.BAD_REQUEST);
			}
			cognitoClient.shutdown();

		}

	}

	private AWSCognitoIdentityProvider getAmazonCognitoIdentityClient() {
		return AWSCognitoIdentityProviderClientBuilder.standard().withRegion(System.getenv("AWS_REGION"))
				.withCredentials(envCredentialsProvider).build();
	}

	@Override
	public UserPayload getCurrentUserDetails() {
		UserPayload user = null;
		if (null != SecurityContextHolder.getContext() && null != SecurityContextHolder.getContext().getAuthentication()
				&& null != SecurityContextHolder.getContext().getAuthentication().getDetails()) {
			user = (UserPayload) SecurityContextHolder.getContext().getAuthentication().getDetails();

		}
		return user;
	}
}
