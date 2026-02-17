const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api/v1';

export const api = {
  // 오늘 할 일 (메시지 초안) 가져오기
  async getTodayTasks() {
    const res = await fetch(`${API_BASE_URL}/tasks`);
    if (!res.ok) throw new Error('Failed to fetch tasks');
    return res.json();
  },

  // 메시지 전송 처리
  async sendMessage(taskId: string) {
    const res = await fetch(`${API_BASE_URL}/tasks/${taskId}/send`, {
      method: 'POST',
    });
    return res.json();
  },

  // 검수 항목 리스트 가져오기 (GET /api/v1/reviews)
  async getReviewItems() {
    const res = await fetch(`${API_BASE_URL}/reviews`);
    if (!res.ok) throw new Error('Failed to fetch reviews');
    return res.json();
  },

  // 검수 데이터 수정 및 해결 (POST /api/v1/reviews/{id}/resolve)
  async resolveReviewItem(id: string, value: string) {
    const res = await fetch(`${API_BASE_URL}/reviews/${id}/resolve`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ value }),
    });
    if (!res.ok) throw new Error('Failed to resolve review item');
    return res.json();
  },

  // 고객 상세 정보 가져오기
  async getClientDetail(id: string) {
    const res = await fetch(`${API_BASE_URL}/clients/${id}`);
    if (!res.ok) throw new Error('Failed to fetch client detail');
    return res.json();
  },

  // 통계 데이터 가져오기
  async getStats() {
    const res = await fetch(`${API_BASE_URL}/stats`);
    if (!res.ok) throw new Error('Failed to fetch stats');
    return res.json();
  }
};
