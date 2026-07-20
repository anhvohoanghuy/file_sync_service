package org.example.file_sync_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình kết nối MinIO, nạp từ các key có tiền tố "minio" trong application.properties.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** Địa chỉ endpoint của MinIO, ví dụ http://localhost:9100 */
    private String endpoint = "http://localhost:9100";

    /** Access key (tài khoản) truy cập MinIO */
    private String accessKey;

    /** Secret key (mật khẩu) truy cập MinIO */
    private String secretKey;

    /** Bucket mặc định dùng để lưu file */
    private String bucket;

    /** Tự động tạo bucket khi khởi động nếu chưa tồn tại */
    private boolean autoCreateBucket = true;
}
