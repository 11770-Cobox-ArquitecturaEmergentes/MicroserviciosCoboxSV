package org.upc.mobilebffservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.upc.mobilebffservice.mobile.infrastructure.storage.EvidenceStorageService;

@SpringBootTest
class MobileBffServiceApplicationTests {

    @MockBean
    private EvidenceStorageService evidenceStorageService;

    @Test
    void contextLoads() {
    }
}
