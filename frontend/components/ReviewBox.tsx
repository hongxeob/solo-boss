'use client';

import { useEffect, useState } from 'react';
import { ReviewItem } from '../types';
import { api } from '../lib/api';
import { AlertCircle, CheckCircle2, Edit3 } from 'lucide-react';

export default function ReviewBox() {
  const [items, setItems] = useState<ReviewItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editValue, setEditValue] = useState('');

  useEffect(() => {
    fetchItems();
  }, []);

  const fetchItems = async () => {
    try {
      const data = await api.getReviewItems();
      setItems(data);
    } catch (err) {
      console.error('Failed to fetch reviews', err);
    } finally {
      setLoading(false);
    }
  };

  const handleStartEdit = (item: ReviewItem) => {
    setEditingId(item.id);
    setEditValue(item.value);
  };

  const handleSave = async (id: string) => {
    try {
      await api.resolveReviewItem(id, editValue);
      setItems(items.filter(item => item.id !== id));
      setEditingId(null);
    } catch (err) {
      alert('저장에 실패했습니다.');
    }
  };

  if (loading) return (
    <div className="flex justify-center py-20">
      <div className="animate-pulse text-primary font-medium">검수 항목을 분석 중...</div>
    </div>
  );

  return (
    <div className="space-y-4">
      <div className="bg-primary/5 border border-primary/20 p-5 rounded-3xl mb-8 flex items-start gap-3">
        <AlertCircle className="w-5 h-5 text-secondary shrink-0 mt-0.5" />
        <p className="text-slate-400 text-xs leading-relaxed font-medium">
          AI가 정보를 추출하는 과정에서 확신이 낮은 항목들입니다. <br/>
          <span className="text-secondary">서브컬러로 표시된 항목</span>을 우선적으로 확인해주세요.
        </p>
      </div>

      {items.length === 0 && (
        <div className="flex flex-col items-center justify-center py-20 text-slate-500">
          <CheckCircle2 className="w-12 h-12 text-primary/30 mb-4" />
          <p>모든 검수가 완료되었습니다.</p>
        </div>
      )}

      {items.map((item) => (
        <div 
          key={item.id} 
          className={`bg-surface border rounded-[2rem] p-6 transition-all duration-300 ${
            item.confidence < 0.5 
              ? 'border-secondary/30 shadow-[0_0_25px_rgba(254,192,206,0.05)]' 
              : 'border-white/5'
          }`}
        >
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-slate-500 uppercase tracking-tight">{item.clientName}</span>
              <span className={`h-1 w-1 rounded-full ${item.confidence < 0.5 ? 'bg-secondary animate-pulse' : 'bg-primary'}`} />
              <span className={`text-[10px] font-black tracking-tighter uppercase ${item.confidence < 0.5 ? 'text-secondary' : 'text-primary'}`}>
                {Math.round(item.confidence * 100)}% Confidence
              </span>
            </div>
          </div>

          <h4 className="text-xs font-bold text-slate-500 mb-2 uppercase tracking-widest">{item.fieldName}</h4>
          
          {editingId === item.id ? (
            <div className="space-y-4">
              <input
                type="text"
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                className="w-full bg-background border border-primary/50 rounded-2xl px-5 py-4 text-slate-100 focus:outline-none focus:ring-2 focus:ring-primary/30 transition-all font-medium"
                autoFocus
              />
              <div className="flex gap-2">
                <button 
                  onClick={() => handleSave(item.id)}
                  className="flex-1 bg-primary text-white py-3 rounded-xl text-sm font-bold shadow-lg shadow-primary/20 active:scale-95 transition-transform"
                >
                  확정 및 해결
                </button>
                <button 
                  onClick={() => setEditingId(null)}
                  className="px-6 bg-white/5 text-slate-400 py-3 rounded-xl text-sm font-bold active:scale-95 transition-transform"
                >
                  취소
                </button>
              </div>
            </div>
          ) : (
            <div className="flex items-center justify-between group">
              <p className={`text-xl font-bold tracking-tight ${item.confidence < 0.5 ? 'text-secondary' : 'text-slate-100'}`}>
                {item.value || <span className="opacity-30 italic">비어 있음</span>}
              </p>
              <button 
                onClick={() => handleStartEdit(item)}
                className="p-2.5 bg-primary/10 text-primary rounded-xl border border-primary/20 opacity-60 group-hover:opacity-100 transition-all active:scale-90"
              >
                <Edit3 className="w-4 h-4" />
              </button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
