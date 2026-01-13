package com.meetscribe.app.common.response;

public record ApiError(
        String code,
        String message
) {}

