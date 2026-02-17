'use client';

import { useEffect, useState } from 'react';
import { ReviewItem } from '../types';
import { api } from '../lib/api';

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

  if (loading) return <div className="text-center py-10 text-slate-500">불러오는 중...</div>;

  return (
    <div className="space-y-3">
      <div className="bg-amber-500/10 border border-amber-500/20 p-4 rounded-xl mb-6">
        <p className="text-amber-200 text-xs leading-tight">
          AI가 정보를 추출하는 과정에서 확신이 낮은 항목들입니다. <br/>정확한 업무를 위해 검토 부탁드립니다.
        </p>
      </div>

      {items.length === 0 && <div className="text-center py-10 text-slate-500">검수할 항목이 없습니다.</div>}

      {items.map((item) => (
        <div key={item.id} className={`bg-slate-900 border rounded-2xl p-4 transition-all ${
          item.confidence < 0.5 ? 'border-red-500/50 shadow-[0_0_15px_rgba(239,68,68,0.1)]' : 'border-slate-800'
        }`}>
          <div className="flex items-center justify-between mb-3">
            <div className="flex items-center gap-2">
              <span className="text-xs text-slate-500">{item.clientName}</span>
              <div className={`w-1.5 h-1.5 rounded-full ${item.confidence < 0.5 ? 'bg-red-500 animate-pulse' : 'bg-amber-500'}`} />
              <span className={`text-[10px] font-bold ${item.confidence < 0.5 ? 'text-red-400' : 'text-amber-400'}`}>
                {Math.round(item.confidence * 100)}% 확신
              </span>
            </div>
          </div>

          <h4 className="text-sm font-medium text-slate-400 mb-1">{item.fieldName}</h4>
          
          {editingId === item.id ? (
            <div className="space-y-3">
              <input
                type="text"
                value={editValue}
                onChange={(e) => setEditValue(e.target.value)}
                className="w-full bg-slate-950 border border-indigo-500/50 rounded-xl px-4 py-3 text-slate-100 focus:outline-none focus:ring-1 focus:ring-indigo-500"
                autoFocus
              />
              <div className="flex gap-2">
                <button 
                  onClick={() => handleSave(item.id)}
                  className="flex-1 bg-indigo-600 text-white py-2 rounded-lg text-sm font-bold"
                >
                  저장 및 해결
                </button>
                <button 
                  onClick={() => setEditingId(null)}
                  className="px-4 bg-slate-800 text-slate-400 py-2 rounded-lg text-sm"
                >
                  취소
                </button>
              </div>
            </div>
          ) : (
            <div className="flex items-end justify-between">
              <p className={`text-lg font-bold ${item.confidence < 0.5 ? 'text-red-200' : 'text-slate-100'}`}>
                {item.value}
              </p>
              <button 
                onClick={() => handleStartEdit(item)}
                className="text-indigo-400 text-xs font-semibold px-3 py-1 bg-indigo-500/10 rounded-md border border-indigo-500/20"
              >
                수정
              </button>
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
