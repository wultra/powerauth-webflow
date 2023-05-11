/*
 * PowerAuth Web Flow and related software components
 * Copyright (C) 2022 Wultra s.r.o.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.getlime.security.powerauth.app.tppengine.service;

import io.getlime.security.powerauth.app.tppengine.configuration.TppEngineConfiguration;
import io.getlime.security.powerauth.app.tppengine.errorhandling.exception.TppNotFoundException;
import io.getlime.security.powerauth.app.tppengine.model.entity.TppInfo;
import io.getlime.security.powerauth.app.tppengine.repository.TppAppDetailRepository;
import io.getlime.security.powerauth.app.tppengine.repository.TppRepository;
import io.getlime.security.powerauth.app.tppengine.repository.model.entity.TppEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testing of {@link TppService}
 *
 * @author Lukas Lukovsky, lukas.lukovsky@wultra.com
 */
@ExtendWith(MockitoExtension.class)
public class TppServiceTest {

    public static final String TPP_LICENSE = "tppLicense";

    @Mock
    private TppRepository tppRepository;

    private TppService tppService;

    @BeforeEach
    void init() {
        tppService = new TppService(
                tppRepository,
                Mockito.mock(TppAppDetailRepository.class),
                Mockito.mock(TppEngineConfiguration.class),
                Mockito.mock(RegisteredClientRepository.class),
                Mockito.mock(JdbcTemplate.class)
        );
    }

    @Test
    void testBlockTpp() throws Exception {
        TppEntity tppEntity = new TppEntity();
        tppEntity.setBlocked(false);
        Mockito.when(tppRepository.findFirstByTppLicense(TPP_LICENSE)).thenReturn(Optional.of(tppEntity));

        TppInfo tppInfo = tppService.blockTpp(TPP_LICENSE);
        assertTrue(tppInfo.isBlocked(), "TPP should be blocked");
    }

    @Test
    void testBlockTppWhenAlreadyBlocked() throws Exception {
        TppEntity tppEntity = new TppEntity();
        tppEntity.setBlocked(true);
        Mockito.when(tppRepository.findFirstByTppLicense(TPP_LICENSE)).thenReturn(Optional.of(tppEntity));

        TppInfo tppInfo = tppService.blockTpp(TPP_LICENSE);
        assertTrue(tppInfo.isBlocked(), "TPP should be blocked");
    }

    @Test
    void testBlockTppWhenNotFound() {
        Mockito.when(tppRepository.findFirstByTppLicense(TPP_LICENSE)).thenReturn(Optional.empty());

        assertThrows(TppNotFoundException.class, () ->
                tppService.blockTpp(TPP_LICENSE));
    }

    @Test
    void testUnblockTpp() throws Exception {
        TppEntity tppEntity = new TppEntity();
        tppEntity.setBlocked(true);
        Mockito.when(tppRepository.findFirstByTppLicense(TPP_LICENSE)).thenReturn(Optional.of(tppEntity));

        TppInfo tppInfo = tppService.unblockTpp(TPP_LICENSE);
        assertFalse(tppInfo.isBlocked(), "TPP should not be blocked");
    }

    @Test
    void testUnblockTppWhenAlreadyUnblocked() throws Exception {
        TppEntity tppEntity = new TppEntity();
        tppEntity.setBlocked(false);
        Mockito.when(tppRepository.findFirstByTppLicense(TPP_LICENSE)).thenReturn(Optional.of(tppEntity));

        TppInfo tppInfo = tppService.unblockTpp(TPP_LICENSE);
        assertFalse(tppInfo.isBlocked(), "TPP should not be blocked");
    }

    @Test
    void testUnblockTppWhenNotFound() {
        Mockito.when(tppRepository.findFirstByTppLicense(TPP_LICENSE)).thenReturn(Optional.empty());

        assertThrows(TppNotFoundException.class, () ->
                tppService.unblockTpp(TPP_LICENSE));
    }

}
