# 전문직 프리랜서용 상담 노트 OCR 추출 규격 (신뢰도 기반)

이 문서는 프리랜서의 고객 상담 메모나 계약 관련 문서 이미지에서 핵심 정보를 추출할 때, UX 측면에서 사용자에게 수정 제안을 할 수 있도록 **필드별 신뢰도 점수**를 포함하는 규격을 정의합니다.

## 1. 신뢰도 기반 프롬프트 전략

### 1.1 프롬프트 템플릿
```text
[Persona]
당신은 전문직 프리랜서를 위한 비즈니스 비서 AI입니다. 상담 메모 이미지에서 정보를 추출하여 정확한 데이터로 변환하며, 각 정보의 확실성을 평가하는 데 탁월한 능력이 있습니다.

[Task]
제공된 이미지에서 아래 6가지 필드를 추출하고, 각 필드마다 추출 결과의 확실성(confidence, 0.0~1.0)을 함께 기록하세요.

[Field Definitions]
1. clientName: 고객명 (개인 또는 업체명)
2. projectType: 프로젝트 종류 (예: 웹 디자인, 법률 자문, 번역 등)
3. estimatedBudget: 예상 예산 (숫자만 추출)
4. deadline: 마감 기한 (YYYY-MM-DD 형식)
5. consultationSummary: 상담 요약 (반드시 3개의 문장으로 구성된 리스트)
6. followUpDate: 팔로업 예정일 (YYYY-MM-DD 형식)

[Confidence Scoring Criteria]
- 1.0: 텍스트가 매우 선명하고 오타 없이 명확히 읽힘.
- 0.7~0.9: 텍스트는 읽히지만 문맥상 추측이 일부 포함됨.
- 0.4~0.6: 글씨가 흐릿하거나 필기체라 오독의 가능성이 있음.
- 0.0~0.3: 거의 읽을 수 없어 매우 낮은 확률로 추측함.

[Constraints]
- 모든 필드는 { "value": ..., "confidence": ... } 구조를 유지하세요.
- 상담 요약은 3줄이어야 하며, 각 줄별로 신뢰도를 측정하는 대신 요약 전체에 대한 신뢰도를 부여하세요.
- 출력은 마크다운 코드 블록 없이 순수 JSON만 반환하세요.

[Output Format]
{format}
```

## 2. JSON 스키마 규격 (Freelancer Consultation)

### 2.1 Java Record 정의 (Spring AI 연동용)
```java
public record ConsultationExtraction(
    Field<String> clientName,
    Field<String> projectType,
    Field<Long> estimatedBudget,
    Field<LocalDate> deadline,
    SummaryField consultationSummary,
    Field<LocalDate> followUpDate
) {
    public record Field<T>(T value, Double confidence) {}
    public record SummaryField(List<String> lines, Double confidence) {}
}
```

### 2.2 JSON 출력 예시
```json
{
  "clientName": { "value": "김철수", "confidence": 0.95 },
  "projectType": { "value": "로고 디자인 리뉴얼", "confidence": 0.9 },
  "estimatedBudget": { "value": 2000000, "confidence": 0.8 },
  "deadline": { "value": "2024-05-30", "confidence": 0.7 },
  "consultationSummary": {
    "lines": [
      "기존 브랜드의 정체성을 유지하면서 트렌디한 느낌을 원함.",
      "주요 타겟층은 2030 세대로 설정하여 미니멀한 스타일 강조.",
      "최종 시안은 총 3개를 제안하기로 협의함."
    ],
    "confidence": 0.85
  },
  "followUpDate": { "value": "2024-05-15", "confidence": 0.6 }
}
```

## 3. UX 활용 가이드

1.  **신뢰도 하이라이트**: `confidence`가 0.7 미만인 필드는 입력 폼에서 노란색 배경이나 경고 아이콘을 표시하여 사용자가 한 번 더 확인하도록 유도합니다.
2.  **데이터 보정**: 사용자가 값을 수정하면 해당 필드의 `confidence`는 즉시 1.0으로 업데이트하여 시스템에 반영합니다.
3.  **지능형 요약**: 상담 요약의 신뢰도가 낮을 경우 "이미지가 흐릿하여 요약이 부정확할 수 있습니다"라는 안내 문구를 노출합니다.
