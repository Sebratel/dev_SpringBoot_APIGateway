package br.com.sebratel.bff.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QrCodeOutputDTO {
    private String encryptedData;
    private String qrCodeImage;
}
