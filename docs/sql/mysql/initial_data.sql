-- default oauth 2.0 client
-- Note: bcrypt('changeme', 12) => '$2a$12$MkYsT5igDXSDgRwyDVz1B.93h8F81E4GZJd/spy/1vhjM4CJgeed.'
INSERT INTO oauth_client_details (client_id, client_secret, scope, authorized_grant_types, additional_information, autoapprove)
VALUES ('democlient', '$2a$12$MkYsT5igDXSDgRwyDVz1B.93h8F81E4GZJd/spy/1vhjM4CJgeed.', 'profile', 'authorization_code', '{}', 'true');

-- authentication methods
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('INIT', 1, FALSE, NULL, NULL, FALSE, NULL, FALSE, FALSE, NULL);
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('USER_ID_ASSIGN', 2, FALSE, NULL, NULL, FALSE, NULL, FALSE, FALSE, NULL);
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('USERNAME_PASSWORD_AUTH', 3, FALSE, NULL, NULL, TRUE, 5, TRUE, FALSE, 'method.usernamePassword');
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('SHOW_OPERATION_DETAIL', 4, FALSE, NULL, NULL, FALSE, NULL, TRUE, FALSE, 'method.showOperationDetail');
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('POWERAUTH_TOKEN', 5, TRUE, 1, FALSE, TRUE, 5, TRUE, TRUE, 'method.powerauthToken');
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('SMS_KEY', 6, FALSE, NULL, NULL, TRUE, 5, TRUE, FALSE, 'method.smsKey');
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('CONSENT', 7, FALSE, NULL, NULL, TRUE, 5, TRUE, FALSE, 'method.consent');
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('LOGIN_SCA', 8, FALSE, NULL, NULL, TRUE, 5, TRUE, TRUE, 'method.loginSca');
INSERT INTO ns_auth_method (auth_method, order_number, check_user_prefs, user_prefs_column, user_prefs_default, check_auth_fails, max_auth_fails, has_user_interface, has_mobile_token, display_name_key)
VALUES ('APPROVAL_SCA', 9, FALSE, NULL, NULL, TRUE, 5, TRUE, TRUE, 'method.approvalSca');

-- operation configuration
INSERT INTO ns_operation_config (operation_name, template_version, template_id, mobile_token_mode) VALUES ('login', 'A', 2, '{"type":"2FA","variants":["possession_knowledge","possession_biometry"]}');
INSERT INTO ns_operation_config (operation_name, template_version, template_id, mobile_token_mode) VALUES ('login_sca', 'A', 2, '{"type":"2FA","variants":["possession_knowledge","possession_biometry"]}');
INSERT INTO ns_operation_config (operation_name, template_version, template_id, mobile_token_mode) VALUES ('authorize_payment', 'A', 1, '{"type":"2FA","variants":["possession_knowledge","possession_biometry"]}');
INSERT INTO ns_operation_config (operation_name, template_version, template_id, mobile_token_mode) VALUES ('authorize_payment_sca', 'A', 1, '{"type":"2FA","variants":["possession_knowledge","possession_biometry"]}');

-- organization configuration
INSERT INTO ns_organization (organization_id, display_name_key, is_default, order_number) VALUES ('DEFAULT', null, TRUE, 1);

-- login - init operation -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (1, 'login', 'CREATE', NULL, NULL, 1, 'USER_ID_ASSIGN', 'CONTINUE');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (2, 'login', 'CREATE', NULL, NULL, 2, 'USERNAME_PASSWORD_AUTH', 'CONTINUE');

-- login - update operation - CONFIRMED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (3, 'login', 'UPDATE', 'USER_ID_ASSIGN', 'CONFIRMED', 1, 'CONSENT', 'CONTINUE');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (4, 'login', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'CONFIRMED', 1, 'CONSENT', 'CONTINUE');

-- login - update operation - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (5, 'login', 'UPDATE', 'USER_ID_ASSIGN', 'CANCELED', 1, NULL, 'FAILED');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (6, 'login', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'CANCELED', 1, NULL, 'FAILED');

-- login - update operation - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (8, 'login', 'UPDATE', 'USER_ID_ASSIGN', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (9, 'login', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- login - update operation - AUTH_FAILED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (11, 'login', 'UPDATE', 'USER_ID_ASSIGN', 'AUTH_FAILED', 1, 'USER_ID_ASSIGN', 'CONTINUE');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (12, 'login', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'AUTH_FAILED', 1, 'USERNAME_PASSWORD_AUTH', 'CONTINUE');

-- login - update operation (consent) - CONFIRMED -> DONE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (13, 'login', 'UPDATE', 'CONSENT', 'CONFIRMED', 1, NULL, 'DONE');

-- login - update operation (consent) - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (14, 'login', 'UPDATE', 'CONSENT', 'CANCELED', 1, NULL, 'FAILED');

-- login - update operation (consent) - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (15, 'login', 'UPDATE', 'CONSENT', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- login - update operation (consent) - AUTH_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (16, 'login', 'UPDATE', 'CONSENT', 'AUTH_FAILED', 1, 'CONSENT', 'CONTINUE');

-- authorize_payment - init operation -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (17, 'authorize_payment', 'CREATE', NULL, NULL, 1, 'USER_ID_ASSIGN', 'CONTINUE');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (18, 'authorize_payment', 'CREATE', NULL, NULL, 2, 'USERNAME_PASSWORD_AUTH', 'CONTINUE');

-- authorize_payment - update operation (login) - CONFIRMED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (19, 'authorize_payment', 'UPDATE', 'USER_ID_ASSIGN', 'CONFIRMED', 1, 'POWERAUTH_TOKEN', 'CONTINUE');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (20, 'authorize_payment', 'UPDATE', 'USER_ID_ASSIGN', 'CONFIRMED', 2, 'SMS_KEY', 'CONTINUE');

INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (21, 'authorize_payment', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'CONFIRMED', 1, 'POWERAUTH_TOKEN', 'CONTINUE');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (22, 'authorize_payment', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'CONFIRMED', 2, 'SMS_KEY', 'CONTINUE');

-- authorize_payment - update operation (login) - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (23, 'authorize_payment', 'UPDATE', 'USER_ID_ASSIGN', 'CANCELED', 1, NULL, 'FAILED');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (24, 'authorize_payment', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'CANCELED', 1, NULL, 'FAILED');

-- authorize_payment - update operation (login) - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (25, 'authorize_payment', 'UPDATE', 'USER_ID_ASSIGN', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (26, 'authorize_payment', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- authorize_payment - update operation (login) - AUTH_FAILED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (27, 'authorize_payment', 'UPDATE', 'USER_ID_ASSIGN', 'AUTH_FAILED', 1, 'USER_ID_ASSIGN', 'CONTINUE');
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (28, 'authorize_payment', 'UPDATE', 'USERNAME_PASSWORD_AUTH', 'AUTH_FAILED', 1, 'USERNAME_PASSWORD_AUTH', 'CONTINUE');

-- authorize_payment - update operation (authorize using mobile token) - CONFIRMED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (29, 'authorize_payment', 'UPDATE', 'POWERAUTH_TOKEN', 'CONFIRMED', 1, 'CONSENT', 'CONTINUE');

-- authorize_payment - update operation (authorize using mobile token) - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (30, 'authorize_payment', 'UPDATE', 'POWERAUTH_TOKEN', 'CANCELED', 1, NULL, 'FAILED');

-- authorize_payment - update operation (authorize using mobile token) - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (31, 'authorize_payment', 'UPDATE', 'POWERAUTH_TOKEN', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- authorize_payment - update operation (authorize using mobile token) - AUTH_FAILED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (32, 'authorize_payment', 'UPDATE', 'POWERAUTH_TOKEN', 'AUTH_FAILED', 1, 'POWERAUTH_TOKEN', 'CONTINUE');

-- authorize_payment - update operation (authorize using sms key) - CONFIRMED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (33, 'authorize_payment', 'UPDATE', 'SMS_KEY', 'CONFIRMED', 1, 'CONSENT', 'CONTINUE');

-- authorize_payment - update operation (authorize using sms key) - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (34, 'authorize_payment', 'UPDATE', 'SMS_KEY', 'CANCELED', 1, NULL, 'FAILED');

-- authorize_payment - update operation (authorize using sms key) - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (35, 'authorize_payment', 'UPDATE', 'SMS_KEY', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- authorize_payment - update operation (authorize using sms key) - AUTH_FAILED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (36, 'authorize_payment', 'UPDATE', 'SMS_KEY', 'AUTH_FAILED', 1, 'SMS_KEY', 'CONTINUE');

-- authorize_payment - update operation (consent) - CONFIRMED -> DONE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (37, 'authorize_payment', 'UPDATE', 'CONSENT', 'CONFIRMED', 1, NULL, 'DONE');

-- authorize_payment - update operation (consent) - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (38, 'authorize_payment', 'UPDATE', 'CONSENT', 'CANCELED', 1, NULL, 'FAILED');

-- authorize_payment - update operation (consent) - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (39, 'authorize_payment', 'UPDATE', 'CONSENT', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- authorize_payment - update operation (consent) - AUTH_FAILED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (40, 'authorize_payment', 'UPDATE', 'CONSENT', 'AUTH_FAILED', 1, 'CONSENT', 'CONTINUE');

-- 2FA login - init operation -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (41, 'login_sca', 'CREATE', NULL, NULL, 1, 'LOGIN_SCA', 'CONTINUE');

-- 2FA login - update operation - CONFIRMED -> DONE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (42, 'login_sca', 'UPDATE', 'LOGIN_SCA', 'CONFIRMED', 1, NULL, 'DONE');

-- 2FA login - update operation - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (43, 'login_sca', 'UPDATE', 'LOGIN_SCA', 'CANCELED', 1, NULL, 'FAILED');

-- 2FA login - update operation - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (44, 'login_sca', 'UPDATE', 'LOGIN_SCA', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- 2FA login - update operation - AUTH_FAILED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (45, 'login_sca', 'UPDATE', 'LOGIN_SCA', 'AUTH_FAILED', 1, 'LOGIN_SCA', 'CONTINUE');

-- authorize_payment_sca - init operation -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (46, 'authorize_payment_sca', 'CREATE', NULL, NULL, 1, 'LOGIN_SCA', 'CONTINUE');

-- authorize_payment_sca - update operation (login) - CONFIRMED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (47, 'authorize_payment_sca', 'UPDATE', 'LOGIN_SCA', 'CONFIRMED', 1, 'APPROVAL_SCA', 'CONTINUE');

-- authorize_payment_sca - update operation (login) - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (48, 'authorize_payment_sca', 'UPDATE', 'LOGIN_SCA', 'CANCELED', 1, NULL, 'FAILED');

-- authorize_payment_sca - update operation (login) - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (49, 'authorize_payment_sca', 'UPDATE', 'LOGIN_SCA', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- authorize_payment_sca - update operation (login) - AUTH_FAILED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (50, 'authorize_payment_sca', 'UPDATE', 'LOGIN_SCA', 'AUTH_FAILED', 1, 'LOGIN_SCA', 'CONTINUE');

-- authorize_payment_sca - update operation (approval) - CONFIRMED -> DONE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (51, 'authorize_payment_sca', 'UPDATE', 'APPROVAL_SCA', 'CONFIRMED', 1, NULL, 'DONE');

-- authorize_payment_sca - update operation (approval) - CANCELED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (52, 'authorize_payment_sca', 'UPDATE', 'APPROVAL_SCA', 'CANCELED', 1, NULL, 'FAILED');

-- authorize_payment_sca - update operation (approval) - AUTH_METHOD_FAILED -> FAILED
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (53, 'authorize_payment_sca', 'UPDATE', 'APPROVAL_SCA', 'AUTH_METHOD_FAILED', 1, NULL, 'FAILED');

-- authorize_payment_sca - update operation (approval) - AUTH_FAILED -> CONTINUE
INSERT INTO ns_step_definition (step_definition_id, operation_name, operation_type, request_auth_method, request_auth_step_result, response_priority, response_auth_method, response_result)
VALUES (54, 'authorize_payment_sca', 'UPDATE', 'APPROVAL_SCA', 'AUTH_FAILED', 1, 'APPROVAL_SCA', 'CONTINUE');
