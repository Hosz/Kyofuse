package com.hokyozu.kyofuse.infrastructure.security.totp;

import dev.samstevens.totp.code.DefaultCodeGenerator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TotpServiceTest {

    private final TotpService totpService = new TotpService();

    @Test
    void generateSecretProducesANonBlankBase32Secret() {
        String secret = totpService.generateSecret();

        assertThat(secret).isNotBlank();
        assertThat(totpService.generateSecret()).isNotEqualTo(secret);
    }

    @Test
    void buildOtpAuthUriIncludesLabelIssuerAndSecret() {
        String uri = totpService.buildOtpAuthUri("hideo", "JBSWY3DPEHPK3PXP");

        assertThat(uri).startsWith("otpauth://totp/");
        assertThat(uri).contains("hideo");
        assertThat(uri).contains("Kyofuse");
        assertThat(uri).contains("JBSWY3DPEHPK3PXP");
    }

    @Test
    void verifyCodeAcceptsACurrentlyValidCode() throws Exception {
        String secret = "JBSWY3DPEHPK3PXP";
        String validCode = new DefaultCodeGenerator().generate(secret, System.currentTimeMillis() / 1000 / 30);

        assertThat(totpService.verifyCode(secret, validCode)).isTrue();
    }

    @Test
    void verifyCodeRejectsAWrongCode() {
        assertThat(totpService.verifyCode("JBSWY3DPEHPK3PXP", "000000")).isFalse();
    }

    @Test
    void verifyCodeRejectsNullOrBlankInput() {
        assertThat(totpService.verifyCode("JBSWY3DPEHPK3PXP", null)).isFalse();
        assertThat(totpService.verifyCode("JBSWY3DPEHPK3PXP", " ")).isFalse();
        assertThat(totpService.verifyCode(null, "123456")).isFalse();
    }
}
