'use client';

import { useEffect, useState } from 'react';
import { MessageDraft } from '../types';
import { api } from '../lib/api';
import { Sparkles, Send, Clock, Edit2 } from 'lucide-react';

export default function TodayTasks() {
  const [tasks, setTasks] = useState<MessageDraft[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchTasks();
  }, []);

  const fetchTasks = async () => {
    try {
      const data = await api.getTodayTasks();
      setTasks(data);
    } catch (err) {
      console.error('Failed to fetch tasks', err);
    } finally {
      setLoading(false);
    }
  };

  const handleSend = async (id: string) => {
    try {
      await api.sendTask(id);
      alert('메시지가 성공적으로 전송되었습니다.');
      setTasks(tasks.filter(t => t.id !== id));
    } catch (err) {
      alert('전송에 실패했습니다.');
    }
  };

  const handleSnooze = async (id: string) => {
    try {
      await api.snoozeTask(id);
      setTasks(tasks.filter(t => t.id !== id));
    } catch (err) {
      alert('미루기에 실패했습니다.');
    }
  };

  if (loading) return (
    <div className="flex justify-center py-20">
      <div className="animate-pulse text-primary font-medium">AI가 업무를 분석 중...</div>
    </div>
  );

  return (
    <div className="space-y-6">
      {tasks.length === 0 && (
        <div className="flex flex-col items-center justify-center py-20 text-slate-500">
          <div className="w-16 h-16 bg-surface rounded-full flex items-center justify-center mb-4">
            <Sparkles className="w-8 h-8 text-primary/20" />
          </div>
          <p className="font-medium">현재 진행할 팔로업 업무가 없습니다.</p>
        </div>
      )}

      {tasks.map((msg) => (
        <div key={msg.id} className="bg-surface border border-white/5 rounded-[2rem] p-6 space-y-5 shadow-2xl transition-all hover:border-primary/20">
          <div className="flex justify-between items-start">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-2xl bg-primary/10 flex items-center justify-center">
                <Sparkles className="w-5 h-5 text-primary" />
              </div>
              <div>
                <h3 className="text-lg font-bold text-slate-100 leading-tight">{msg.clientName}</h3>
                <p className="text-[10px] font-black text-primary uppercase tracking-widest mt-0.5">AI Suggested Follow-up</p>
              </div>
            </div>
            <span className="bg-secondary/10 text-secondary text-[10px] font-black px-3 py-1 rounded-full border border-secondary/20 uppercase tracking-tighter">Draft</span>
          </div>
          
          <div className="relative group">
            <p className="text-slate-300 text-sm leading-relaxed bg-background/50 p-5 rounded-2xl border border-white/5 font-medium italic">
              "{msg.content}"
            </p>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <button 
              onClick={() => handleSend(msg.id)}
              className="bg-primary text-white py-4 rounded-2xl font-bold text-sm flex items-center justify-center gap-2 shadow-lg shadow-primary/20 active:scale-95 transition-all"
            >
              <Send className="w-4 h-4" />
              지금 보내기
            </button>
            <div className="grid grid-cols-2 gap-2">
              <button className="bg-white/5 text-slate-300 rounded-2xl flex items-center justify-center hover:bg-white/10 transition-colors">
                <Edit2 className="w-4 h-4" />
              </button>
              <button 
                onClick={() => handleSnooze(msg.id)}
                className="bg-white/5 text-slate-300 rounded-2xl flex items-center justify-center hover:bg-white/10 transition-colors"
              >
                <Clock className="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}
