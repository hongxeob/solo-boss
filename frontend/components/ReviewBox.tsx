'use client';

import { useState } from 'react';
import { ReviewItem } from '../types';

const MOCK_REVIEWS: ReviewItem[] = [
  { id: '1', clientName: '박지민', fieldName: '예상 예산', value: '3,000,000원', confidence: 0.45 },
  { id: '2', clientName: '최유진', fieldName: '마감 기한', value: '2024-06-15', confidence: 0.88 },
  { id: '3', clientName: '글로벌 테크', fieldName: '고객명', value: '글로벌 텍', confidence: 0.32 }
];

export default function ReviewBox() {
  return (
    <div className="space-y-3">
      <div className="bg-amber-500/10 border border-amber-500/20 p-4 rounded-xl mb-6">
        <p className="text-amber-200 text-xs leading-tight">
          AI가 정보를 추출하는 과정에서 확신이 낮은 항목들입니다. <br/>정확한 업무를 위해 검토 부탁드립니다.
        </p>
      </div>

      {MOCK_REVIEWS.map((item) => (
        <div key={item.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-4 flex items-center justify-between group active:scale-[0.98] transition-all">
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <span className="text-xs text-slate-500">{item.clientName}</span>
              <div className={`w-1.5 h-1.5 rounded-full ${item.confidence < 0.5 ? 'bg-red-500 animate-pulse' : 'bg-amber-500'}`} />
            </div>
            <h4 className="text-sm font-medium text-slate-400">{item.fieldName}</h4>
            <p className="text-base font-semibold text-slate-100 mt-0.5">{item.value}</p>
          </div>
          
          <div className="text-right">
            <span className={`text-[10px] font-bold px-2 py-1 rounded-md ${
              item.confidence < 0.5 ? 'bg-red-500/10 text-red-400' : 'bg-amber-500/10 text-amber-400'
            }`}>
              {Math.round(item.confidence * 100)}% 확신
            </span>
            <button className="block mt-2 text-indigo-400 text-xs font-semibold">수정하기</button>
          </div>
        </div>
      ))}
    </div>
  );
}
