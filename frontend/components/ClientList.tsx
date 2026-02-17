'use client';

const MOCK_CLIENTS = [
  { id: '1', name: '김철수', company: '디자인스튜디오', tags: ['중요', '진행중'] },
  { id: '2', name: '이영희', company: '개인', tags: ['신규'] },
  { id: '3', name: '박지민', company: '테크솔루션', tags: ['대기'] },
];

export default function ClientList() {
  return (
    <div className="space-y-3">
      {MOCK_CLIENTS.map((client) => (
        <div key={client.id} className="bg-slate-900 border border-slate-800 rounded-2xl p-4 flex items-center justify-between">
          <div>
            <h4 className="text-base font-semibold text-slate-100">{client.name}</h4>
            <p className="text-xs text-slate-500">{client.company}</p>
          </div>
          <div className="flex gap-2">
            {client.tags.map(tag => (
              <span key={tag} className="text-[10px] bg-slate-800 text-slate-400 px-2 py-1 rounded-md">
                {tag}
              </span>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
