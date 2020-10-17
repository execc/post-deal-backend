package com.wavesplatform.we.app.deals.api.exception

enum class ErrorDtoDict(val errorDto: ErrorDto) {
    VC_NOT_FOUND(ErrorDto(
            VstErrorCode.VC_NOT_FOUND.value,
            "VC not found"
    ))
}
