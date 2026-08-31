package com.hokyozu.kyofuse.infrastructure.client;

public record DeviceInfo(
        String browser,
        String operatingSystem,
        String deviceType,
        String summary
) {
    public static DeviceInfo unknown() {
        return new DeviceInfo("Navegador desconhecido", "Sistema desconhecido", "Desconhecido", "Dispositivo desconhecido");
    }
}
