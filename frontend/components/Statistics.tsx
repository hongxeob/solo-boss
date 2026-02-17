'use client';

import { useEffect, useState } from 'react';
import { StatsData } from '../types';
import { api } from '../lib/api';
import { TrendingUp, Timer, Target, BarChart3 } from 'lucide-react';

export default function Statistics() {
  const [stats, setStats] = useState<StatsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  useEffect(() => {
    api.getStats()
      .then(setStats)
      .catch(() => setError(true))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="flex justify-center py-20">
      <div className="animate-pulse text-primary font-medium">비즈니스 리포트 생성 중...</div>
    </div>
  );

  if (error || !stats) return (
    <div className="flex flex-col items-center justify-center py-20 text-slate-500">
      <p>리포트 데이터를 불러올 수 없습니다.</p>
      <p className="text-xs mt-2">서버 연결 상태와 OWNER_ID를 확인해주세요.</p>
    </div>
  );

  return (
    <div className="space-y-6">
      {/* 주요 지표 */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-surface p-6 rounded-[2rem] border border-white/5 shadow-xl relative overflow-hidden group">
          <div className="absolute top-0 right-0 p-4 opacity-5 group-hover:scale-110 transition-transform">
            <TrendingUp className="w-12 h-12 text-secondary" />
          </div>
          <p className="text-slate-500 text-[10px] font-black uppercase tracking-widest mb-2">Monthly Revenue</p>
          <h3 className="text-2xl font-black text-secondary">
            ₩{(stats.monthlyRevenue / 10000).toLocaleString()}만
          </h3>
        </div>
        <div className="bg-surface p-6 rounded-[2rem] border border-white/5 shadow-xl">
          <p className="text-slate-500 text-[10px] font-black uppercase tracking-widest mb-2">Active Projects</p>
          <h3 className="text-2xl font-black text-slate-100">{stats.projectCount}개</h3>
        </div>
      </div>

      {/* AI 효율 섹션 */}
      <div className="bg-primary/10 border border-primary/20 p-8 rounded-[2.5rem] relative overflow-hidden shadow-2xl">
        <div className="absolute -top-10 -right-10 w-40 h-40 bg-primary/10 rounded-full blur-3xl" />
        <div className="flex justify-between items-start mb-8">
          <div>
            <h4 className="text-primary text-sm font-black uppercase tracking-tighter">AI Automation Impact</h4>
            <p className="text-slate-500 text-[10px] font-medium uppercase tracking-widest">Powered by Gemini 1.5</p>
          </div>
          <div className="w-10 h-10 bg-primary/20 rounded-2xl flex items-center justify-center">
            <Target className="w-5 h-5 text-primary" />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-10">
          <div className="space-y-1">
            <p className="text-slate-500 text-[10px] font-bold uppercase tracking-widest flex items-center gap-1.5">
              <Target className="w-3 h-3" /> Accuracy
            </p>
            <p className="text-3xl font-black text-slate-100">{stats.aiAccuracy}%</p>
          </div>
          <div className="space-y-1">
            <p className="text-slate-500 text-[10px] font-bold uppercase tracking-widest flex items-center gap-1.5">
              <Timer className="w-3 h-3" /> Time Saved
            </p>
            <p className="text-3xl font-black text-slate-100">{stats.timeSaved}h</p>
          </div>
        </div>
      </div>

      {/* 수익 차트 */}
      <div className="bg-surface p-8 rounded-[2.5rem] border border-white/5 shadow-xl">
        <div className="flex items-center justify-between mb-8">
          <h4 className="text-sm font-black text-slate-400 uppercase tracking-widest flex items-center gap-2">
            <BarChart3 className="w-4 h-4 text-primary" /> Revenue Trend
          </h4>
        </div>
        <div className="flex items-end justify-around h-40 gap-4">
          {stats.revenueByMonth.map((item) => (
            <div key={item.month} className="flex flex-col items-center flex-1 gap-4 group">
              <div className="relative w-full flex items-end justify-center">
                <div 
                  className="w-full bg-primary/20 border-t-4 border-primary rounded-t-2xl transition-all duration-1000 group-hover:bg-primary/40 group-hover:border-secondary" 
                  style={{ height: `${(item.amount / 1500) * 100}%` }}
                />
                <div className="absolute -top-8 opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap bg-background px-2 py-1 rounded-md text-[9px] font-bold text-secondary border border-white/5">
                  ₩{item.amount}만
                </div>
              </div>
              <span className="text-[10px] font-black text-slate-500 uppercase">{item.month}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
