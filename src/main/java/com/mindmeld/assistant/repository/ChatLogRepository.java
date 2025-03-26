package com.mindmeld.assistant.repository;

import com.mindmeld.assistant.entity.ChatLog;
import com.mindmeld.assistant.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatLogRepository extends JpaRepository<ChatLog, Long> {
    List<ChatLog> findByUserOrderByTimestampAsc(User user);
}