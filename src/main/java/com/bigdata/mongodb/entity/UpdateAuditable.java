package com.bigdata.mongodb.entity;

import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

public class UpdateAuditable extends CreateAuditable{
    @LastModifiedDate
    private LocalDateTime lastUpdatedAt = LocalDateTime.now();
    @LastModifiedBy
    private User lastUpdatedBy = null;
}
