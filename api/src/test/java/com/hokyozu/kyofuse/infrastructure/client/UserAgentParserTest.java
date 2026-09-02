package com.hokyozu.kyofuse.infrastructure.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserAgentParserTest {

    private UserAgentParser parser;

    @BeforeEach
    void setUp() {
        parser = new UserAgentParser();
    }

    @Test
    void parseHandlesNullAndBlank() {
        DeviceInfo nullResult = parser.parse(null);
        assertThat(nullResult.browser()).isEqualTo("Navegador desconhecido");
        assertThat(nullResult.summary()).isEqualTo("Dispositivo desconhecido");

        DeviceInfo blankResult = parser.parse("   ");
        assertThat(blankResult.browser()).isEqualTo("Navegador desconhecido");
    }

    @Test
    void parseWindowsChrome() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36";
        DeviceInfo info = parser.parse(ua);

        assertThat(info.browser()).isEqualTo("Chrome");
        assertThat(info.operatingSystem()).isEqualTo("Windows");
        assertThat(info.deviceType()).isEqualTo("Computador");
        assertThat(info.summary()).isEqualTo("Chrome no Windows");
    }

    @Test
    void parseMacSafari() {
        String ua = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Safari/605.1.15";
        DeviceInfo info = parser.parse(ua);

        assertThat(info.browser()).isEqualTo("Safari");
        assertThat(info.operatingSystem()).isEqualTo("macOS");
        assertThat(info.deviceType()).isEqualTo("Computador");
        assertThat(info.summary()).isEqualTo("Safari no macOS");
    }

    @Test
    void parseIPhoneSafari() {
        String ua = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1";
        DeviceInfo info = parser.parse(ua);

        assertThat(info.browser()).isEqualTo("Safari");
        assertThat(info.operatingSystem()).isEqualTo("iOS (iPhone)");
        assertThat(info.deviceType()).isEqualTo("Celular");
        assertThat(info.summary()).isEqualTo("Safari no iOS (iPhone)");
    }

    @Test
    void parseAndroidChrome() {
        String ua = "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.88 Mobile Safari/537.36";
        DeviceInfo info = parser.parse(ua);

        assertThat(info.browser()).isEqualTo("Chrome");
        assertThat(info.operatingSystem()).isEqualTo("Android");
        assertThat(info.deviceType()).isEqualTo("Celular");
        assertThat(info.summary()).isEqualTo("Chrome no Android");
    }

    @Test
    void parseEdgeWindows() {
        String ua = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36 Edg/128.0.2739.42";
        DeviceInfo info = parser.parse(ua);

        assertThat(info.browser()).isEqualTo("Microsoft Edge");
        assertThat(info.operatingSystem()).isEqualTo("Windows");
        assertThat(info.summary()).isEqualTo("Microsoft Edge no Windows");
    }

    @Test
    void parseLinuxFirefox() {
        String ua = "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:129.0) Gecko/20100101 Firefox/129.0";
        DeviceInfo info = parser.parse(ua);

        assertThat(info.browser()).isEqualTo("Firefox");
        assertThat(info.operatingSystem()).isEqualTo("Linux");
        assertThat(info.summary()).isEqualTo("Firefox no Linux");
    }

    @Test
    void parsePostmanAndInsomniaAndCurl() {
        assertThat(parser.parse("PostmanRuntime/7.39.0").summary()).isEqualTo("Postman");
        assertThat(parser.parse("insomnia/9.3.2").summary()).isEqualTo("Insomnia");
        assertThat(parser.parse("curl/8.5.0").summary()).isEqualTo("cURL");
    }
}
