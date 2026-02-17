'use client';

import { ClientDetail } from '../types';

interface Props {
  client: ClientDetail;
  onBack: () => void;
}

export default function ClientDetailView({ client, onBack }: Props) {
  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-right-4 duration-300">
      <button onClick={onBack} className="text-indigo-400 text-sm font-medium mb-4 flex items-center gap-1">
        ← 돌아가기
      </button>

      <div className="flex justify-between items-start">
        <div>
          <h2 className="text-2xl font-bold">{client.name}</h2>
          <p className="text-slate-500">{client.company}</p>
        </div>
        <div className="text-right">
          <p className="text-xs text-slate-500">누적 매출</p>
          <p className="text-lg font-bold text-indigo-400">₩{(client.totalRevenue/10000).toLocaleString()}만</p>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-2">
        <div className="bg-slate-900 p-4 rounded-2xl border border-slate-800">
          <p className="text-[10px] text-slate-500 mb-1">연락처</p>
          <p className="text-xs text-slate-300">{client.phone}</p>
        </div>
        <div className="bg-slate-900 p-4 rounded-2xl border border-slate-800">
          <p className="text-[10px] text-slate-500 mb-1">이메일</p>
          <p className="text-xs text-slate-300 truncate">{client.email}</p>
        </div>
      </div>

      <div>
        <h4 className="text-sm font-semibold mb-3">프로젝트 히스토리</h4>
        <div className="space-y-2">
          {client.projectHistory.map((pj) => (
            <div key={pj.id} className="bg-slate-900/50 border border-slate-800 p-4 rounded-xl flex justify-between items-center">
              <div>
                <p className="text-sm font-medium">{pj.title}</p>
                <p className="text-[10px] text-slate-500">{pj.date}</p>
              </div>
              <div className="text-right">
                <p className="text-xs font-bold">₩{(pj.amount/10000).toLocaleString()}만</p>
                <span className="text-[9px] px-1.5 py-0.5 rounded bg-green-500/10 text-green-400 border border-green-500/20">
                  {pj.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
