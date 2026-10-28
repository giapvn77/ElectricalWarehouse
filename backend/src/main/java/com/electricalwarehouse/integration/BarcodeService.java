package com.electricalwarehouse.integration;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.oned.Code128Writer;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/** PNG barcode (Code 128) and QR code generation for labels. */
@Service
public class BarcodeService {

    public byte[] code128(String content, int width, int height) {
        return toPng(() -> new Code128Writer().encode(content, BarcodeFormat.CODE_128, width, height));
    }

    public byte[] qrCode(String content, int size) {
        return toPng(() -> new QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size,
                Map.of(EncodeHintType.CHARACTER_SET, "UTF-8", EncodeHintType.MARGIN, 1)));
    }

    private interface MatrixSupplier {
        BitMatrix get() throws WriterException;
    }

    private static byte[] toPng(MatrixSupplier supplier) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(supplier.get(), "PNG", out);
            return out.toByteArray();
        } catch (WriterException | IOException | IllegalArgumentException e) {
            throw new com.electricalwarehouse.exception.BusinessException("Không tạo được mã: " + e.getMessage());
        }
    }
}
