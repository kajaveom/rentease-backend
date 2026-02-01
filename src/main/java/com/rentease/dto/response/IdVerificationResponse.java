package com.rentease.dto.response;

import com.rentease.entity.User;
import com.rentease.entity.enums.IdVerificationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IdVerificationResponse {

    private IdVerificationStatus status;
    private boolean idVerified;
    private LocalDateTime submittedAt;
    private String rejectionReason;

    public static IdVerificationResponse fromEntity(User user) {
        return IdVerificationResponse.builder()
                .status(user.getIdVerificationStatus())
                .idVerified(user.isIdVerified())
                .submittedAt(user.getIdVerificationSubmittedAt())
                .rejectionReason(user.getIdVerificationRejectionReason())
                .build();
    }
}
