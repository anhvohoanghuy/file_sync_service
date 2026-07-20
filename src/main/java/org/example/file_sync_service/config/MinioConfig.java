package org.example.file_sync_service.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Khởi tạo MinioClient bean và đảm bảo bucket mặc định tồn tại khi ứng dụng khởi động.
 */
@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    private static final Logger log = LoggerFactory.getLogger(MinioConfig.class);

    private final MinioProperties properties;

    public MinioConfig(MinioProperties properties) {
        this.properties = properties;
    }

    @Bean
    public MinioClient minioClient() {
        MinioClient client = MinioClient.builder()
                .endpoint(properties.getEndpoint())
                .credentials(properties.getAccessKey(), properties.getSecretKey())
                .build();

        if (properties.isAutoCreateBucket()) {
            ensureBucketExists(client, properties.getBucket());
        }
        return client;
    }

    private void ensureBucketExists(MinioClient client, String bucket) {
        try {
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                client.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                log.info("Đã tạo bucket MinIO: {}", bucket);
            } else {
                log.info("Bucket MinIO đã tồn tại: {}", bucket);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Không thể khởi tạo bucket MinIO: " + bucket, e);
        }
    }
}
