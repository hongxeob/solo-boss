'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { ClientDetail } from '../../../types';
import { api } from '../../../lib/api';
import { ChevronLeft } from 'lucide-react';

export default function ClientDetailPage() {
  const { id } = useParams();
  const router = useRouter();
  const [client, setClient] = useState<ClientDetail | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (id) {
      api.getClientDetail(id as string)
        .then(setClient)
        .finally(() => setLoading(false));
    }
  }, [id]);

  if (loading) return (
    <div className="min-h-screen bg-background text-slate-100 flex items-center justify-center">
      <div className="animate-pulse text-primary font-medium">데이터를 가져오는 중...</div>
    </div>
  );

  if (!client) return (
    <div className="min-h-screen bg-background text-slate-100 flex flex-col items-center justify-center p-6 text-center">
      <p className="text-slate-500 mb-4">고객 정보를 찾을 수 없습니다.</p>
      <button onClick={() => router.back()} className="text-primary font-medium">돌아가기</button>
    </div>
  );

  return (
    <main className="min-h-screen bg-background text-slate-100 pb-12">
      {/* Header */}
      <header className="p-6 flex items-center gap-4 sticky top-0 bg-background/80 backdrop-blur-md z-10">
        <button onClick={() => router.back()} className="p-2 -ml-2 rounded-full hover:bg-surface transition-colors">
          <ChevronLeft className="w-6 h-6 text-primary" />
        </button>
        <h1 className="text-xl font-bold tracking-tight">고객 상세 정보</h1>
      </header>

      <div className="p-6 space-y-8 animate-in fade-in slide-in-from-bottom-4 duration-500">
        {/* Profile Card */}
        <section className="flex justify-between items-start">
          <div>
            <h2 className="text-3xl font-bold mb-1">{client.name}</h2>
            <p className="text-slate-400 font-medium">{client.company}</p>
          </div>
          <div className="text-right">
            <p className="text-[10px] uppercase tracking-wider text-slate-500 mb-1">누적 매출</p>
            <p className="text-2xl font-black text-secondary">
              ₩{(client.totalRevenue / 10000).toLocaleString()}만
            </p>
          </div>
        </section>

        {/* Contact Info Grid */}
        <section className="grid grid-cols-1 gap-3">
          <div className="bg-surface p-5 rounded-3xl border border-white/5 hover:border-primary/30 transition-colors group">
            <p className="text-[10px] uppercase tracking-widest text-slate-500 mb-2 group-hover:text-primary transition-colors">연락처</p>
            <p className="text-lg font-medium text-slate-200">{client.phone}</p>
          </div>
          <div className="bg-surface p-5 rounded-3xl border border-white/5 hover:border-primary/30 transition-colors group">
            <p className="text-[10px] uppercase tracking-widest text-slate-500 mb-2 group-hover:text-primary transition-colors">이메일</p>
            <p className="text-lg font-medium text-slate-200 truncate">{client.email}</p>
          </div>
        </section>

        {/* Project History */}
        <section>
          <h4 className="text-sm font-bold uppercase tracking-widest text-slate-500 mb-4 px-1">프로젝트 히스토리</h4>
          <div className="space-y-3">
            {client.projectHistory.map((pj) => (
              <div key={pj.id} className="bg-surface/50 border border-white/5 p-5 rounded-2xl flex justify-between items-center hover:bg-surface transition-colors">
                <div>
                  <p className="text-base font-semibold text-slate-200 mb-1">{pj.title}</p>
                  <p className="text-xs text-slate-500 font-medium">{pj.date}</p>
                </div>
                <div className="text-right">
                  <p className="text-sm font-bold text-slate-200 mb-2">₩{(pj.amount / 10000).toLocaleString()}만</p>
                  <span className="text-[10px] px-3 py-1 rounded-full bg-primary/20 text-secondary border border-primary/30 font-bold uppercase tracking-tighter">
                    {pj.status}
                  </span>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </main>
  );
}
