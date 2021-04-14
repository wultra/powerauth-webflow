/*
 * Copyright 2021 Wultra s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.getlime.security.powerauth.app.nextstep;

import io.getlime.security.powerauth.lib.nextstep.client.NextStepClientException;
import io.getlime.security.powerauth.lib.nextstep.model.entity.OtpGenerationParam;
import io.getlime.security.powerauth.lib.nextstep.model.entity.enumeration.OtpStatus;
import io.getlime.security.powerauth.lib.nextstep.model.enumeration.OtpGenerationAlgorithm;
import io.getlime.security.powerauth.lib.nextstep.model.request.CreateOtpPolicyRequest;
import io.getlime.security.powerauth.lib.nextstep.model.request.UpdateOtpDefinitionRequest;
import io.getlime.security.powerauth.lib.nextstep.model.response.*;
import org.junit.Before;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * Next Step credential tests.
 *
 * @author Roman Strobl, roman.strobl@wulta.com
 */
public class NextStepOtpTest extends NextStepTest {

    @Before
    public void setUp() throws Exception {
        nextStepClient = nextStepClientFactory.createNextStepClient("http://localhost:" + port);
        nextStepTestConfiguration.configure(nextStepClient);
    }

    @Test
    public void testGenerateOtpDataDigest1() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        OtpGenerationAlgorithm algorithm = OtpGenerationAlgorithm.OTP_DATA_DIGEST;
        OtpGenerationParam genParam = new OtpGenerationParam();
        int length = 8;
        String data = "test_data";
        updateOtpDefinition(name, length, algorithm, genParam);
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP_GENERATION", null, data).getResponseObject();
        assertNotNull(r1.getOtpValue());
        assertNotNull(r1.getOtpId());
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        String value = r1.getOtpValue();
        assertTrue(value.matches("[0-9]{8}"));
    }

    @Test
    public void testGenerateOtpDataDigest2() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        OtpGenerationAlgorithm algorithm = OtpGenerationAlgorithm.OTP_DATA_DIGEST;
        OtpGenerationParam genParam = new OtpGenerationParam();
        int length = 4;
        String data = "test_data";
        updateOtpDefinition(name, length, algorithm, genParam);
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP_GENERATION", null, data).getResponseObject();
        assertNotNull(r1.getOtpValue());
        assertNotNull(r1.getOtpId());
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        String value = r1.getOtpValue();
        assertTrue(value.matches("[0-9]{4}"));
    }

    @Test
    public void testGenerateOtpRandomDigitGroups() throws NextStepClientException {
        String name = UUID.randomUUID().toString();
        OtpGenerationAlgorithm algorithm = OtpGenerationAlgorithm.OTP_RANDOM_DIGIT_GROUPS;
        OtpGenerationParam genParam = new OtpGenerationParam();
        genParam.setGroupSize(2);
        int length = 2 * new SecureRandom().nextInt(10) + 2;
        updateOtpDefinition(name, length, algorithm, genParam);
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP_GENERATION", null, null).getResponseObject();
        assertNotNull(r1.getOtpValue());
        assertNotNull(r1.getOtpId());
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        String value = r1.getOtpValue();
        assertTrue(value.matches("[0-9]{" + length + "}"));
        Set<String> groups = new LinkedHashSet<>();
        for (int i = 0; i < length / 2; i++) {
            String c1 = String.valueOf(value.charAt(i * 2));
            String c2 = String.valueOf(value.charAt(i * 2 + 1));
            groups.add(c1 + c2);
        }
        assertEquals(length / 2, groups.size());
    }

    @Test
    public void testOtpLifecycleNoOperation() throws NextStepClientException {
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, "test_data").getResponseObject();
        assertNotNull(r1.getOtpId());
        assertNotNull(r1.getOtpValue());
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        assertEquals("TEST_OTP", r1.getOtpName());
        assertEquals("test_user_1", r1.getUserId());
        GetOtpDetailResponse r2 = nextStepClient.getOtpDetail(r1.getOtpId(), null).getResponseObject();
        assertNotNull(r2.getOtpDetail().getOtpId());
        assertNotNull(r2.getOtpDetail().getOtpValue());
        assertEquals(OtpStatus.ACTIVE, r2.getOtpDetail().getOtpStatus());
        assertEquals("TEST_OTP", r2.getOtpDetail().getOtpName());
        assertEquals("test_user_1", r2.getOtpDetail().getUserId());
        DeleteOtpResponse r3 = nextStepClient.deleteOtp(r1.getOtpId(), null).getResponseObject();
        assertEquals(OtpStatus.REMOVED, r3.getOtpStatus());
        GetOtpDetailResponse r4 = nextStepClient.getOtpDetail(r1.getOtpId(), null).getResponseObject();
        assertNotNull(r4.getOtpDetail().getOtpId());
        assertNotNull(r4.getOtpDetail().getOtpValue());
        assertEquals(OtpStatus.REMOVED, r4.getOtpDetail().getOtpStatus());
        assertEquals("TEST_OTP", r4.getOtpDetail().getOtpName());
        assertEquals("test_user_1", r4.getOtpDetail().getUserId());
    }

    @Test
    public void testOtpLifecycleWithOperation() throws NextStepClientException {
        CreateOperationResponse r0 = nextStepClient.createOperation("auth_otp", "test_data", null).getResponseObject();
        String operationId = r0.getOperationId();
        CreateOtpResponse r1 = nextStepClient.createOtp("test_user_1", "TEST_OTP", null, null, operationId).getResponseObject();
        assertNotNull(r1.getOtpId());
        assertNotNull(r1.getOtpValue());
        assertEquals(OtpStatus.ACTIVE, r1.getOtpStatus());
        assertEquals("TEST_OTP", r1.getOtpName());
        assertEquals("test_user_1", r1.getUserId());
        GetOtpDetailResponse r2 = nextStepClient.getOtpDetail(null, operationId).getResponseObject();
        assertNotNull(r2.getOtpDetail().getOtpId());
        assertNotNull(r2.getOtpDetail().getOtpValue());
        assertEquals(OtpStatus.ACTIVE, r2.getOtpDetail().getOtpStatus());
        assertEquals("TEST_OTP", r2.getOtpDetail().getOtpName());
        assertEquals("test_user_1", r2.getOtpDetail().getUserId());
        assertEquals(operationId, r2.getOperationId());
        GetOtpListResponse r3 = nextStepClient.getOtpList(operationId, false).getResponseObject();
        assertEquals(operationId, r3.getOperationId());
        assertEquals(1, r3.getOtpDetails().size());
        assertEquals(OtpStatus.ACTIVE, r3.getOtpDetails().get(0).getOtpStatus());
        DeleteOtpResponse r4 = nextStepClient.deleteOtp(null, operationId).getResponseObject();
        assertEquals(OtpStatus.REMOVED, r4.getOtpStatus());
        GetOtpDetailResponse r5 = nextStepClient.getOtpDetail(null, operationId).getResponseObject();
        assertNotNull(r5.getOtpDetail().getOtpId());
        assertNotNull(r5.getOtpDetail().getOtpValue());
        assertEquals(OtpStatus.REMOVED, r5.getOtpDetail().getOtpStatus());
        assertEquals("TEST_OTP", r5.getOtpDetail().getOtpName());
        assertEquals("test_user_1", r5.getOtpDetail().getUserId());
        GetOtpListResponse r6 = nextStepClient.getOtpList(operationId, false).getResponseObject();
        assertEquals(operationId, r6.getOperationId());
        assertTrue(r6.getOtpDetails().isEmpty());
        GetOtpListResponse r7 = nextStepClient.getOtpList(operationId, true).getResponseObject();
        assertEquals(operationId, r7.getOperationId());
        assertEquals(1, r7.getOtpDetails().size());
        assertEquals(OtpStatus.REMOVED, r7.getOtpDetails().get(0).getOtpStatus());
    }

    private void updateOtpDefinition(String name, int length, OtpGenerationAlgorithm algorithm, OtpGenerationParam genParam) throws NextStepClientException {
        // Create credential policy
        createOtpPolicy(name, length, algorithm, genParam);
        UpdateOtpDefinitionRequest otpDefinitionRequest = new UpdateOtpDefinitionRequest();
        otpDefinitionRequest.setOtpDefinitionName("TEST_OTP_GENERATION");
        otpDefinitionRequest.setApplicationName("TEST_APP");
        otpDefinitionRequest.setOtpPolicyName(name);
        nextStepClient.updateOtpDefinition(otpDefinitionRequest);
    }

    private void createOtpPolicy(String name, int length, OtpGenerationAlgorithm algorithm, OtpGenerationParam genParam) throws NextStepClientException {
        CreateOtpPolicyRequest otpPolicyRequest = new CreateOtpPolicyRequest();
        otpPolicyRequest.setOtpPolicyName(name);
        otpPolicyRequest.setAttemptLimit(3);
        otpPolicyRequest.setLength(length);
        otpPolicyRequest.setGenAlgorithm(algorithm);
        otpPolicyRequest.setGenParam(genParam);
        nextStepClient.createOtpPolicy(otpPolicyRequest);
    }

}