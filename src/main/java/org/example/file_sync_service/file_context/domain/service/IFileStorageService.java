package org.example.file_sync_service.file_context.domain.service;

import java.io.InputStream;
import java.util.List;

/**
 * Cổng (port) cho việc lưu trữ nội dung file trên object storage. Tầng application chỉ phụ thuộc
 * interface này; phần cài đặt (MinIO) nằm ở infrastructure.
 */
public interface IFileStorageService {

  /**
   * Lưu một phần (part) của phiên upload multipart.
   *
   * <p>Mỗi part được lưu thành một object riêng theo quy ước đặt tên, để bước hoàn tất upload có thể
   * ghép (compose) lại thành object cuối cùng theo {@code objectKey}.
   *
   * @param objectKey khoá object cuối cùng của file.
   * @param partNumber số thứ tự của part, bắt đầu từ 1.
   * @param data luồng dữ liệu của part.
   * @param size kích thước part tính bằng byte.
   * @return ETag của part vừa lưu.
   */
  String uploadPart(String objectKey, int partNumber, InputStream data, long size);

  /**
   * Ghép các part đã upload thành object cuối cùng theo {@code objectKey}.
   *
   * @param objectKey khoá object cuối cùng.
   * @param partNumbers danh sách số thứ tự part theo đúng thứ tự cần ghép.
   * @return kích thước (byte) của object sau khi ghép.
   */
  long composeParts(String objectKey, List<Integer> partNumbers);

  /**
   * Tính checksum SHA-256 của object.
   *
   * @param objectKey khoá object cần tính.
   * @return chuỗi checksum dạng {@code "sha256:<hex>"}.
   */
  String computeChecksum(String objectKey);

  /** Xoá các object part sau khi đã ghép xong (dọn dẹp). */
  void deleteParts(String objectKey, List<Integer> partNumbers);
}
