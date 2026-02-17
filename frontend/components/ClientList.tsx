'use client';

import { useEffect, useState } from 'react';
import { ClientItem } from '../types';
import { api } from '../lib/api';
import { useRouter } from 'next/navigation';

export default function ClientList() {
  const router = useRouter();
  const [clients, setClients] = useState<ClientItem[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    api
      .getClients()
      .then(setClients)
      .finally(() => setLoading(false));
  }, []);

  if (loading) return (
    <div className="flex justify-center py-20">
      <div className="animate-pulse text-primary font-medium">고객 목록을 불러오는 중...</div>
    </div>
  );

  return (
    <div className="space-y-4">
      {clients.length === 0 && <div className="text-center py-20 text-slate-500">등록된 고객이 없습니다.</div>}
      {clients.map((client) => (
        <div 
          key={client.id} 
          onClick={() => router.push(`/clients/${client.id}`)}
          className="bg-surface border border-white/5 rounded-3xl p-5 flex items-center justify-between cursor-pointer hover:border-primary/40 active:scale-[0.98] transition-all duration-200 group"
        >
          <div>
            <h4 className="text-lg font-bold text-slate-100 group-hover:text-secondary transition-colors">{client.name}</h4>
            <p className="text-sm text-slate-500 font-medium">{client.company}</p>
          </div>
          <div className="flex gap-2">
            {client.tags.map((tag) => (
              <span key={tag} className="text-[10px] bg-primary/10 text-primary border border-primary/20 px-3 py-1 rounded-full font-bold">
                {tag}
              </span>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
