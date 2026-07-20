package org.example.file_sync_service.auth.infrastructure.cache;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.example.file_sync_service.auth.application.auth_service.refresh.RefreshTokenCache;
import org.springframework.stereotype.Component;

/**
 * Bản cài đặt RefreshTokenCache bằng bộ nhớ tiến trình (thay cho Redis).
 * Database (refresh_tokens) mới là nguồn dữ liệu chính; cache chỉ để tăng tốc,
 * nên cài đặt in-memory này an toàn về mặt chức năng cho môi trường một node.
 */
@Component
public class InMemoryRefreshTokenCache implements RefreshTokenCache {

  private final Map<String, Entry> store = new ConcurrentHashMap<>();

  @Override
  public boolean contains(String refreshToken) {
    Entry entry = store.get(refreshToken);
    if (entry == null) {
      return false;
    }
    if (entry.expiryDate().isBefore(Instant.now())) {
      store.remove(refreshToken);
      return false;
    }
    return true;
  }

  @Override
  public void put(String refreshToken, UUID userId, Instant expiryDate) {
    store.put(refreshToken, new Entry(userId, expiryDate));
  }

  @Override
  public void evict(String refreshToken) {
    store.remove(refreshToken);
  }

  private record Entry(UUID userId, Instant expiryDate) {}
}
