package org.example.file_sync_service.config;

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: khởi động MinIO thật bằng Testcontainers và xác minh
 * MinioBucketInitializer tự tạo bucket khi ứng dụng khởi động.
 *
 * <p>Testcontainers 2.x chưa có module minio/junit-jupiter riêng nên dùng
 * GenericContainer và tự quản lý vòng đời container.
 */
@SpringBootTest
class MinioBucketInitializerIT {

    private static final int MINIO_PORT = 9000;
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";

    @SuppressWarnings("resource")
    static final GenericContainer<?> MINIO = new GenericContainer<>(
            DockerImageName.parse("minio/minio:latest"))
            .withExposedPorts(MINIO_PORT)
            .withEnv("MINIO_ROOT_USER", ACCESS_KEY)
            .withEnv("MINIO_ROOT_PASSWORD", SECRET_KEY)
            .withCommand("server", "/data")
            .waitingFor(Wait.forHttp("/minio/health/ready").forPort(MINIO_PORT));

    @BeforeAll
    static void startContainer() {
        MINIO.start();
    }

    @AfterAll
    static void stopContainer() {
        MINIO.stop();
    }

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.endpoint",
                () -> "http://" + MINIO.getHost() + ":" + MINIO.getMappedPort(MINIO_PORT));
        registry.add("minio.access-key", () -> ACCESS_KEY);
        registry.add("minio.secret-key", () -> SECRET_KEY);
        registry.add("minio.bucket", () -> "file-sync");
        registry.add("minio.auto-create-bucket", () -> true);
    }

    @Autowired
    MinioClient minioClient;

    @Autowired
    MinioProperties properties;

    @Test
    void bucketDuocTaoTuDongKhiKhoiDong() throws Exception {
        boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(properties.getBucket())
                        .build());

        assertThat(exists).isTrue();
    }
}
