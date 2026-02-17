'use client';

import { useEffect, useState } from 'react';
import { MessageDraft } from '../types';
import { api } from '../lib/api';

export default function TodayTasks() {
  const [tasks, setTasks] = useState<MessageDraft[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api.getTodayTasks()
      .then(setTasks)
      .finally(() => setLoading(false));
  }, []);

  const handleSend = async (id: string) => {
    try {
      await api.sendMessage(id);
      alert('메시지가 전송되었습니다.');
      setTasks(tasks.filter(t => t.id !== id));
    } catch (err) {
      alert('전송 실패');
    }
  };

  if (loading) return <div className="text-center py-10 text-slate-500">불러오는 중...</div>;

  return (
    <div className="space-y-4">
      {tasks.length === 0 && <div className="text-center py-10 text-slate-500">오늘 할 일이 없습니다.</div>}
      {tasks.map((msg) => (
        <div key={msg.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-5 space-y-4 shadow-xl">
          <div className="flex justify-between items-start">
            <div>
              <span className="text-xs font-medium text-indigo-400 uppercase tracking-wider">{msg.projectType}</span>
              <h3 className="text-lg font-semibold mt-1">{msg.clientName}</h3>
            </div>
            <span className="bg-indigo-500/10 text-indigo-400 text-[10px] px-2 py-1 rounded-full border border-indigo-500/20">AI Draft</span>
          </div>
          
          <p className="text-slate-400 text-sm leading-relaxed bg-slate-950/50 p-4 rounded-xl border border-slate-800/50">
            "{msg.suggestedMessage}"
          </p>

          <div className="grid grid-cols-3 gap-2">
            <button 
              onClick={() => handleSend(msg.id)}
              className="bg-indigo-600 text-white py-3 rounded-xl font-medium text-sm hover:bg-indigo-500 transition-colors"
            >
              바로 전송
            </button>
            <button className="bg-slate-800 text-slate-200 py-3 rounded-xl font-medium text-sm hover:bg-slate-700">수정</button>
            <button className="bg-transparent text-slate-500 py-3 rounded-xl font-medium text-sm hover:text-slate-300">미루기</button>
          </div>
        </div>
      ))}
    </div>
  );
}
