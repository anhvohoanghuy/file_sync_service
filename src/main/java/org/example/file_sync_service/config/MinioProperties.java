package org.example.file_sync_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Cấu hình kết nối MinIO, nạp từ các key có tiền tố "minio" trong application.properties.
 */
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /** Địa chỉ endpoint của MinIO, ví dụ http://localhost:9000 */
    private String endpoint = "http://localhost:9000";

    /** Access key (tài khoản) truy cập MinIO */
    private String accessKey;

    /** Secret key (mật khẩu) truy cập MinIO */
    private String secretKey;

    /** Bucket mặc định dùng để lưu file */
    private String bucket;

    /** Tự động tạo bucket khi khởi động nếu chưa tồn tại */
    private boolean autoCreateBucket = true;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public boolean isAutoCreateBucket() {
        return autoCreateBucket;
    }

    public void setAutoCreateBucket(boolean autoCreateBucket) {
        this.autoCreateBucket = autoCreateBucket;
    }
}
