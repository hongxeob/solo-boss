export type TabType = 'today' | 'review' | 'clients' | 'stats';

export interface MessageDraft {
  id: string;
  clientName: string;
  projectType: string;
  suggestedMessage: string;
}

export interface ReviewItem {
  id: string;
  clientName: string;
  fieldName: string;
  value: string;
  confidence: number;
}

export interface ClientDetail extends ClientItem {
  email: string;
  phone: string;
  totalRevenue: number;
  projectHistory: {
    id: string;
    title: string;
    date: string;
    status: string;
    amount: number;
  }[];
}

export interface ClientItem {
  id: string;
  name: string;
  company: string;
  tags: string[];
}

export interface StatsData {
  monthlyRevenue: number;
  projectCount: number;
  aiAccuracy: number;
  timeSaved: number;
  revenueByMonth: { month: string; amount: number }[];
}

export interface ConsultationDetail {
  id: string;
  ownerId: string;
  customerId: string;
  ingestJobId: string | null;
  summary: string;
  rawText: string | null;
  consultationDate: string;
  createdAt: string;
  updatedAt: string;
}

export type ConsultationItem = ConsultationDetail;

export interface CreateConsultationRequest {
  ownerId: string;
  customerId: string;
  summary: string;
  rawText?: string;
  consultationDate?: string;
}

export interface UpdateConsultationRequest {
  summary?: string;
  rawText?: string;
  consultationDate?: string;
}
