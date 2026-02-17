import { ClientDetail, ClientItem, MessageDraft, ReviewItem, StatsData } from '../types';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/api/v1';
const OWNER_ID = process.env.NEXT_PUBLIC_OWNER_ID || '11111111-1111-1111-1111-111111111111';

type PageResponse<T> = {
  content: T[];
};

type ReviewResponse = {
  id: string;
  customerGuess: string | null;
  uncertainFields: string[] | null;
  proposedPayload: Record<string, unknown> | null;
  overallConfidence: number | null;
};

type CustomerResponse = {
  id: string;
  name: string;
  phone: string | null;
  email: string | null;
  projectType: string | null;
  inquirySummary: string | null;
};

type TaskResponse = {
  id: string;
  customerName: string;
  taskType: string;
  proposedMessage: string;
  createdAt: string;
};

type StatsResponse = {
  monthlyRevenue: number;
  projectCount: number;
  aiAccuracy: number;
  timeSaved: number;
  revenueByMonth: { month: string; amount: number }[];
};

const toText = (value: unknown): string => {
  if (value == null) return '';
  if (typeof value === 'string') return value;
  if (Array.isArray(value)) return value.join(', ');
  return String(value);
};

export const api = {
  // --- Tasks (오늘 할 일) ---
  async getTodayTasks(): Promise<MessageDraft[]> {
    const res = await fetch(`${API_BASE_URL}/tasks?ownerId=${OWNER_ID}`);
    if (!res.ok) throw new Error('Failed to fetch tasks');
    const data = (await res.json()) as TaskResponse[];
    return data.map(task => ({
      id: task.id,
      clientName: task.customerName,
      content: task.proposedMessage,
      type: task.taskType as any,
    }));
  },

  async sendTask(taskId: string) {
    const res = await fetch(`${API_BASE_URL}/tasks/${taskId}/send?ownerId=${OWNER_ID}`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to send task');
    return res.json();
  },

  async snoozeTask(taskId: string) {
    const res = await fetch(`${API_BASE_URL}/tasks/${taskId}/snooze?ownerId=${OWNER_ID}`, {
      method: 'POST',
    });
    if (!res.ok) throw new Error('Failed to snooze task');
    return res.json();
  },

  // --- Reviews (검수함) ---
  async getReviewItems(): Promise<ReviewItem[]> {
    const res = await fetch(`${API_BASE_URL}/reviews?ownerId=${OWNER_ID}&status=OPEN`);
    if (!res.ok) throw new Error('Failed to fetch reviews');
    const page = (await res.json()) as PageResponse<ReviewResponse>;
    return page.content.map((item) => {
      const primaryField = item.uncertainFields?.[0] ?? '확인 필요';
      const rawValue = item.proposedPayload?.[primaryField];
      return {
        id: item.id,
        clientName: item.customerGuess ?? '미확인 고객',
        fieldName: primaryField,
        value: toText(rawValue) || '값 없음',
        confidence: item.overallConfidence ?? 0,
      };
    });
  },

  async resolveReviewItem(id: string, value: string) {
    const res = await fetch(`${API_BASE_URL}/reviews/${id}/resolve?ownerId=${OWNER_ID}`, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ correctedPayload: { resolvedValue: value } }),
    });
    if (!res.ok) throw new Error('Failed to resolve review item');
    return res.json();
  },

  // --- Customers (고객 관리) ---
  async getClients(): Promise<ClientItem[]> {
    const res = await fetch(`${API_BASE_URL}/customers?ownerId=${OWNER_ID}`);
    if (!res.ok) throw new Error('Failed to fetch clients');
    const page = (await res.json()) as PageResponse<CustomerResponse>;
    return page.content.map((client) => ({
      id: client.id,
      name: client.name,
      company: client.projectType ?? '프로젝트 미지정',
      tags: ['고객'],
    }));
  },

  async getClientDetail(id: string): Promise<ClientDetail> {
    const res = await fetch(`${API_BASE_URL}/customers/${id}?ownerId=${OWNER_ID}`);
    if (!res.ok) throw new Error('Failed to fetch client detail');
    const customer = (await res.json()) as CustomerResponse;
    return {
      id: customer.id,
      name: customer.name,
      company: customer.projectType ?? '프로젝트 미지정',
      tags: ['고객'],
      email: customer.email ?? '-',
      phone: customer.phone ?? '-',
      totalRevenue: 0, // TODO: 백엔드에서 매출 정보 제공 시 연결
      projectHistory: [],
    };
  },

  // --- Statistics (통계) ---
  async getStats(): Promise<StatsData> {
    const res = await fetch(`${API_BASE_URL}/stats?ownerId=${OWNER_ID}`);
    if (!res.ok) throw new Error('Failed to fetch stats');
    return res.json();
  },
};
