export type TabType = 'today' | 'review' | 'clients' | 'stats';

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
