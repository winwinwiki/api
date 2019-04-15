package com.winwin.winwin.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClientBuilder;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminCreateUserResult;
import com.amazonaws.services.cognitoidp.model.AdminGetUserRequest;
import com.amazonaws.services.cognitoidp.model.AdminGetUserResult;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthRequest;
import com.amazonaws.services.cognitoidp.model.AdminInitiateAuthResult;
import com.amazonaws.services.cognitoidp.model.AdminResetUserPasswordRequest;
import com.amazonaws.services.cognitoidp.model.AdminResetUserPasswordResult;
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
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordRequest;
import com.amazonaws.services.cognitoidp.model.ConfirmForgotPasswordResult;
import com.amazonaws.services.cognitoidp.model.DeliveryMediumType;
import com.amazonaws.services.cognitoidp.model.ListUsersRequest;
import com.amazonaws.services.cognitoidp.model.ListUsersResult;
import com.amazonaws.services.cognitoidp.model.ResendConfirmationCodeRequest;
import com.amazonaws.services.cognitoidp.model.ResendConfirmationCodeResult;
import com.amazonaws.services.cognitoidp.model.UserType;
import com.winwin.winwin.Logger.CustomMessageSource;
import com.winwin.winwin.exception.UserException;
import com.winwin.winwin.payload.UserPayload;
import com.winwin.winwin.payload.UserSignInPayload;

/**
 * @author ArvindKhatik
 *
 */
@Component
public class UserService implements IUserService {

	@Autowired
	protected CustomMessageSource customMessageSource;

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	private static final String USER_NAME = "USERNAME";
	private static final String PASS_WORD = "PASSWORD";
	private static final String NEW_PASS_WORD_REQUIRED = "NEW_PASSWORD_REQUIRED";
	private static final String NEW_PASS_WORD = "NEW_PASSWORD";

	ClasspathPropertiesFileCredentialsProvider propertiesFileCredentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
	EnvironmentVariableCredentialsProvider envCredentialsProvider = new EnvironmentVariableCredentialsProvider();

	@SuppressWarnings("unused")
	@Override
	public void createUser(UserPayload payload) throws UserException {

		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			AdminCreateUserRequest cognitoRequest = new AdminCreateUserRequest()
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail())
					.withUserAttributes(new AttributeType().withName("custom:role").withValue(payload.getRole()),
							new AttributeType().withName("custom:team").withValue(payload.getTeam()),
							new AttributeType().withName("picture").withValue(payload.getImageUrl()),
							new AttributeType().withName("name").withValue(payload.getUserDisplayName()),
							new AttributeType().withName("email").withValue(payload.getEmail()),
							new AttributeType().withName("email_verified").withValue("true"))
					.withDesiredDeliveryMediums(DeliveryMediumType.EMAIL).withForceAliasCreation(Boolean.FALSE);

			AdminCreateUserResult createUserResult = cognitoClient.adminCreateUser(cognitoRequest);

			cognitoClient.shutdown();

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.created"), e);
			throw new UserException(e);
		}

	}

	@Override
	public UserPayload getUserInfo(String userName) throws UserException {
		UserPayload payload = new UserPayload();
		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			AdminGetUserRequest cognitoGetUserRequest = new AdminGetUserRequest()
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(userName);

			AdminGetUserResult userResult = cognitoClient.adminGetUser(cognitoGetUserRequest);

			List<AttributeType> userAttributes = userResult.getUserAttributes();
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
			}
			cognitoClient.shutdown();

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.info"), e);
			throw new UserException(e);
		}

		return payload;
	}

	@Override
	public void updateUserInfo(UserPayload payload) throws UserException {
		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			AdminUpdateUserAttributesRequest cognitoUpdateUserRequest = new AdminUpdateUserAttributesRequest()
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail());

			AttributeType attribute = null;
			List<AttributeType> userAttributes = new ArrayList<AttributeType>();

			if (!StringUtils.isEmpty(payload.getUserDisplayName())) {
				attribute = new AttributeType();
				attribute.setName("name");
				attribute.setValue(payload.getUserDisplayName());
				userAttributes.add(attribute);
			}

			if (!StringUtils.isEmpty(payload.getTeam())) {
				attribute = new AttributeType();
				attribute.setName("custom:team");
				attribute.setValue(payload.getTeam());
				userAttributes.add(attribute);
			}

			if (!StringUtils.isEmpty(payload.getRole())) {
				attribute = new AttributeType();
				attribute.setName("custom:role");
				attribute.setValue(payload.getRole());
				userAttributes.add(attribute);
			}

			if (!StringUtils.isEmpty(payload.getImageUrl())) {
				attribute = new AttributeType();
				attribute.setName("picture");
				attribute.setValue(payload.getImageUrl());
				userAttributes.add(attribute);
			}

			cognitoUpdateUserRequest.withUserAttributes(userAttributes);

			@SuppressWarnings("unused")
			AdminUpdateUserAttributesResult userResult = cognitoClient
					.adminUpdateUserAttributes(cognitoUpdateUserRequest);

			cognitoClient.shutdown();

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.info.update"), e);
			throw new UserException(e);
		}

	}

	@Override
	public List<UserPayload> getUserList() throws UserException {
		List<UserPayload> payloadList = new ArrayList<UserPayload>();
		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			ListUsersRequest cognitoGetListUserRequest = new ListUsersRequest()
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID"));

			ListUsersResult userResult = cognitoClient.listUsers(cognitoGetListUserRequest);

			List<UserType> usersList = userResult.getUsers();

			for (UserType user : usersList) {
				List<AttributeType> userAttributes = user.getAttributes();
				for (AttributeType attribute : userAttributes) {
					UserPayload payload = new UserPayload();
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

					payloadList.add(payload);
				}
			}

			cognitoClient.shutdown();

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.info"), e);
			throw new UserException(e);
		}

		return payloadList;
	}

	@Override
	public String getUserStatus(String userName) throws UserException {
		String userStatus = null;
		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			AdminGetUserRequest cognitoGetUserRequest = new AdminGetUserRequest()
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(userName);

			AdminGetUserResult userResult = cognitoClient.adminGetUser(cognitoGetUserRequest);
			userStatus = userResult.getUserStatus();

			cognitoClient.shutdown();

		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.info"), e);
			throw new UserException(e);
		}

		return userStatus;
	}

	@Override
	public AuthenticationResultType userSignIn(UserSignInPayload payload) throws UserException {
		AuthenticationResultType authenticationResult = null;
		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();

			final Map<String, String> authParams = new HashMap<>();
			authParams.put(USER_NAME, payload.getUserName());
			authParams.put(PASS_WORD, payload.getPassword());

			final AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest();
			authRequest.withAuthFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
					.withClientId(System.getenv("AWS_COGNITO_CLIENT_ID"))
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withAuthParameters(authParams);

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
					}
				} else {
					LOGGER.info(customMessageSource.getMessage("org.user.error.signIn"));
					throw new UserException(customMessageSource.getMessage("org.user.error.signIn"));
				}
			} else {
				authenticationResult = result.getAuthenticationResult();
			}
			cognitoClient.shutdown();

		} catch (Exception e) {
			throw new UserException(e);
		}

		return authenticationResult;

	}

	@Override
	public void resetUserPassword(UserPayload payload) throws UserException {

		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			AdminResetUserPasswordRequest cognitoRequest = new AdminResetUserPasswordRequest()
					.withUserPoolId(System.getenv("AWS_COGNITO_USER_POOL_ID")).withUsername(payload.getEmail());

			@SuppressWarnings("unused")
			AdminResetUserPasswordResult resetUserPassResult = cognitoClient.adminResetUserPassword(cognitoRequest);
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.created"), e);
			throw new UserException(e);
		}

	}

	@Override
	public void confirmResetPassword(UserSignInPayload payload) throws UserException {

		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			ConfirmForgotPasswordRequest cognitoRequest = new ConfirmForgotPasswordRequest()
					.withClientId(System.getenv("AWS_COGNITO_CLIENT_ID"))
					.withConfirmationCode(payload.getConfirmationCode()).withPassword(payload.getNewPassword())
					.withUsername(payload.getUserName());

			@SuppressWarnings("unused")
			ConfirmForgotPasswordResult conForgotUserPassResult = cognitoClient.confirmForgotPassword(cognitoRequest);
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.created"), e);
			throw new UserException(e);
		}

	}

	@Override
	public void resendConfirmationCode(UserPayload payload) throws UserException {

		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			ResendConfirmationCodeRequest cognitoRequest = new ResendConfirmationCodeRequest()
					.withClientId(System.getenv("AWS_COGNITO_CLIENT_ID")).withUsername(payload.getEmail());

			@SuppressWarnings("unused")
			ResendConfirmationCodeResult confirmCodeResult = cognitoClient.resendConfirmationCode(cognitoRequest);
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.created"), e);
			throw new UserException(e);
		}

	}

	@Override
	public void changePassword(UserSignInPayload payload) throws UserException {

		try {
			AWSCognitoIdentityProvider cognitoClient = getAmazonCognitoIdentityClient();
			ChangePasswordRequest cognitoRequest = new ChangePasswordRequest().withAccessToken(payload.getAccessToken())
					.withPreviousPassword(payload.getPassword()).withProposedPassword(payload.getNewPassword());

			ChangePasswordResult changePassResult = cognitoClient.changePassword(cognitoRequest);
			System.out.println(changePassResult);
		} catch (Exception e) {
			LOGGER.error(customMessageSource.getMessage("org.user.exception.created"), e);
			throw new UserException(e);
		}

	}

	public AWSCognitoIdentityProvider getAmazonCognitoIdentityClient() {
		return AWSCognitoIdentityProviderClientBuilder.standard().withRegion(System.getenv("AWS_REGION"))
				.withCredentials(envCredentialsProvider).build();

	}

}
