export type TabType = 'today' | 'review' | 'clients';

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
