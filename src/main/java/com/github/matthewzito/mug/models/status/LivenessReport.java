package com.github.matthewzito.mug.models.status;

import java.sql.Timestamp;

public record LivenessReport(String system, SystemStatus status, Timestamp now) {

}
