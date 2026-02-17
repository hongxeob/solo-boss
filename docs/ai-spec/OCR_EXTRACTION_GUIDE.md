# OCR 및 구조화 데이터 추출 기술 규격 (Gemini & Spring AI)

이 문서는 Gemini API의 Multimodal 기능을 활용하여 이미지(OCR)에서 텍스트를 추출하고, 이를 Spring AI를 통해 정형화된 JSON 데이터로 변환하는 전략과 규격을 정의합니다.

## 1. 프롬프트 전략 (Prompt Engineering)

Gemini의 시각 지능(Vision)을 극대화하기 위해 **Role-Task-Constraint-Output** 구조를 채택합니다.

### 1.1 프롬프트 템플릿
```text
[Persona]
당신은 이미지 내의 모든 텍스트를 분석하고 의미 있는 정보를 추출하는 정밀 OCR 분석 전문가입니다.

[Task]
제공된 이미지에서 정보를 읽고, 아래 명시된 JSON 포맷에 맞게 데이터를 구조화하세요.

[Constraints]
1. 시각적으로 확인 가능한 정보만 추출하며, 절대 추측하지 마세요.
2. 숫자에 포함된 통화 기호(₩, $), 콤마(,) 등은 제외하고 순수 숫자값만 추출하세요.
3. 날짜 형식은 ISO-8601 (YYYY-MM-DD) 규격을 준수하세요.
4. 인식할 수 없는 필드는 빈 문자열이 아닌 null로 설정하세요.
5. 출력은 마크다운 코드 블록(```json) 없이 순수 JSON 문자열만 반환하세요.

[Output Format]
{format}
```

### 1.2 주요 전략
*   **Zero-Shot Extraction**: Gemini는 별도의 학습 없이도 이미지 구조를 잘 파악하므로, 명확한 스키마 정의만으로도 충분합니다.
*   **Chain-of-Verification**: 복잡한 데이터의 경우, "먼저 전체 텍스트를 읽고, 그 다음 필드별로 매핑하라"는 지시를 추가하여 정확도를 높입니다.

---

## 2. JSON 스키마 규격

추출할 데이터의 구조를 정의합니다. Java의 `Record` 또는 `POJO`를 기반으로 `BeanOutputConverter`가 스키마를 자동 생성하도록 유도합니다.

### 2.1 대상 데이터 모델 (예시: 영수증)
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "properties": {
    "storeName": { "type": "string" },
    "date": { "type": "string", "format": "date" },
    "items": {
      "type": "array",
      "items": {
        "type": "object",
        "properties": {
          "name": { "type": "string" },
          "price": { "type": "integer" },
          "quantity": { "type": "integer" }
        }
      }
    },
    "totalPrice": { "type": "integer" }
  },
  "required": ["storeName", "totalPrice"]
}
```

---

## 3. Spring AI 연동 규격

### 3.1 기술 스택
*   **Framework**: Spring AI 1.0.0 (M1 이상)
*   **Model**: Google Gemini 1.5 Pro / Flash
*   **Interface**: `ChatClient` (Fluent API)

### 3.2 구현 표준 코드
이미지 리소스를 `Media` 객체에 담아 `UserMessage`와 함께 전달하는 방식을 표준으로 합니다.

```java
public <T> T extractDataFromImage(Resource imageResource, Class<T> clazz) {
    // 1. Output Converter 설정 (JSON 파싱 자동화)
    var converter = new BeanOutputConverter<>(clazz);

    // 2. 메시지 구성 (텍스트 프롬프트 + 이미지 미디어)
    UserMessage userMessage = new UserMessage(
        PROMPT_TEMPLATE.replace("{format}", converter.getFormat()),
        List.of(new Media(MimeTypeUtils.IMAGE_JPEG, imageResource))
    );

    // 3. AI 모델 호출 및 결과 변환
    ChatResponse response = chatClient.prompt(new Prompt(userMessage))
        .call()
        .chatResponse();

    return converter.convert(response.getResult().getOutput().getContent());
}
```

### 3.3 설정 가이드 (application.yml)
```yaml
spring:
  ai:
    google:
      gemini:
        api-key: ${GEMINI_API_KEY}
        options:
          model: gemini-1.5-flash # 속도와 비용 효율을 위해 Flash 모델 권장
          temperature: 0.1       # 데이터 정밀도를 위해 낮은 값 설정
          responseMimeType: application/json # JSON 모드 강제
```

---

## 4. 예외 처리 및 품질 관리

1.  **Parsing Failure**: AI가 반환한 JSON이 스키마를 벗어날 경우 `BeanOutputConverter`에서 예외가 발생합니다. 이 경우 재시도 로직(Retry) 또는 수동 확인 프로세스로 분기합니다.
2.  **Low Confidence**: 추출된 데이터 중 핵심 필드가 `null`인 경우 사용자에게 이미지 재촬영을 가이드합니다.
3.  **Token Limit**: 고해상도 이미지는 다량의 토큰을 소비하므로, 전송 전 2048px 이하로 리사이징을 권장합니다.
