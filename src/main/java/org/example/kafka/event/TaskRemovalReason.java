package org.example.kafka.event;

public enum TaskRemovalReason {
    DELETED, // 真正删除
    CANCELED // 用户取消
}
