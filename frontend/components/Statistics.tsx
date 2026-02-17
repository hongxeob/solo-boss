'use client';

import { useEffect, useState } from 'react';
import { StatsData } from '../types';
import { api } from '../lib/api';

export default function Statistics() {
  const [stats, setStats] = useState<StatsData | null>(null);

  useEffect(() => {
    // Mock 데이터로 우선 세팅 (API 완성 전)
    setStats({
      monthlyRevenue: 12500000,
      projectCount: 8,
      aiAccuracy: 92,
      timeSaved: 14,
      revenueByMonth: [
        { month: '3월', amount: 800 },
        { month: '4월', amount: 1100 },
        { month: '5월', amount: 1250 }
      ]
    });
  }, []);

  if (!stats) return null;

  return (
    <div className="space-y-6">
      {/* 주요 지표 */}
      <div className="grid grid-cols-2 gap-3">
        <div className="bg-slate-900 p-5 rounded-3xl border border-slate-800">
          <p className="text-slate-500 text-xs mb-1">이번 달 수익</p>
          <h3 className="text-xl font-bold text-indigo-400">₩{(stats.monthlyRevenue/10000).toLocaleString()}만</h3>
        </div>
        <div className="bg-slate-900 p-5 rounded-3xl border border-slate-800">
          <p className="text-slate-500 text-xs mb-1">진행 프로젝트</p>
          <h3 className="text-xl font-bold text-slate-100">{stats.projectCount}개</h3>
        </div>
      </div>

      {/* AI 효율 섹션 */}
      <div className="bg-indigo-600/10 border border-indigo-500/20 p-6 rounded-3xl">
        <div className="flex justify-between items-end mb-4">
          <div>
            <h4 className="text-indigo-200 text-sm font-semibold">AI 업무 자동화 효과</h4>
            <p className="text-slate-400 text-[10px]">지난 30일 기준</p>
          </div>
          <span className="text-2xl">🚀</span>
        </div>
        <div className="flex gap-8">
          <div>
            <p className="text-slate-500 text-[10px] uppercase font-bold tracking-wider">추출 정확도</p>
            <p className="text-xl font-bold text-indigo-300">{stats.aiAccuracy}%</p>
          </div>
          <div>
            <p className="text-slate-500 text-[10px] uppercase font-bold tracking-wider">절약된 시간</p>
            <p className="text-xl font-bold text-indigo-300">{stats.timeSaved}시간</p>
          </div>
        </div>
      </div>

      {/* 단순 차트 시각화 */}
      <div className="bg-slate-900 p-6 rounded-3xl border border-slate-800">
        <h4 className="text-sm font-semibold mb-6">월별 수익 추이</h4>
        <div className="flex items-end justify-around h-32 gap-2">
          {stats.revenueByMonth.map((item) => (
            <div key={item.month} className="flex flex-col items-center flex-1 gap-2">
              <div 
                className="w-full bg-indigo-500/20 border-t-2 border-indigo-500 rounded-t-lg transition-all duration-1000" 
                style={{ height: `${(item.amount / 1500) * 100}%` }}
              />
              <span className="text-[10px] text-slate-500">{item.month}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
