package com.mspring.mproject.mbatch.config;

import com.mspring.mproject.mbatch.model.entity.TransactionRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.stereotype.Component;

import java.io.FileWriter;
import java.io.IOException;

@Component
public class TransactionSkipListener implements SkipListener<TransactionRecord, TransactionRecord> {

    private static final Logger log = LoggerFactory.getLogger(TransactionSkipListener.class);
    private static final String ERROR_FILE = "transaction_errors.csv";

    // BẮT LỖI KHI ĐỌC (VÍ DỤ: NumberFormatException)
    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("SKIPPED IN READ: {}", t.getMessage());
        writeError("UNKNOWN_ID_IN_READ", t);
    }

    // BẮT LỖI KHI XỬ LÝ (VÍ DỤ: Số dư âm)
    @Override
    public void onSkipInProcess(TransactionRecord item, Throwable t) {
        log.warn("SKIPPED IN PROCESS: ID {} - {}", item.getTransactionId(), t.getMessage());
        writeError(String.valueOf(item.getTransactionId()), t);
    }

    // BẮT LỖI KHI GHI (VÍ DỤ: Lỗi số quá lẻ)
    @Override
    public void onSkipInWrite(TransactionRecord item, Throwable t) {
        log.warn("SKIPPED IN WRITE: ID {} - {}", item.getTransactionId(), t.getMessage());
        writeError(String.valueOf(item.getTransactionId()), t);
    }

    // TÁCH HÀM GHI LỖI RA CHO SẠCH
    private void writeError(String transactionId, Throwable t) {
        try (FileWriter writer = new FileWriter(ERROR_FILE, true)) {
            if (new java.io.File(ERROR_FILE).length() == 0) {
                writer.append("Transaction_ID,Error_Message\n");
            }

            // Lấy thông điệp lỗi, xóa dấu phẩy và xuống dòng
            String message = (t.getMessage() != null) ? t.getMessage().replace(",", "").replace("\n", " ") : "Unknown Error";

            writer.append(String.format("%s,%s\n",
                    transactionId,
                    message
            ));
        } catch (IOException e) {
            log.error("CRITICAL: Cannot write skip listener error to file: {}", e.getMessage());
        }
    }
}