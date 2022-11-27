package com.bigdata.mongodb.entity;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

public class CreateAuditable {
    @CreatedDate
    private LocalDateTime createdAt = LocalDateTime.now();
    @CreatedBy
    private User createdBy = null;
}
