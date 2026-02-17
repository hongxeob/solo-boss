package com.soloboss.ai.domain.customer

/** 고객 등록 경로 */
enum class CustomerSource {
    /** 카카오톡 웹훅을 통한 자동 등록 */
    KAKAO,

    /** 사용자가 직접 수동 등록 */
    MANUAL,

    /** 외부 데이터 일괄 가져오기 */
    IMPORT,
}
