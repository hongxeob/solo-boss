'use client';

import { MessageDraft } from '../types';

const MOCK_MESSAGES: MessageDraft[] = [
  {
    id: '1',
    clientName: '김철수 대표님',
    projectType: '로고 디자인',
    suggestedMessage: '안녕하세요 김철수 대표님, 지난 상담 때 말씀하신 로고 리뉴얼 관련하여 초안이 준비되었습니다. 편하실 때 확인 부탁드립니다!'
  },
  {
    id: '2',
    clientName: '이영희 실장님',
    projectType: '번역 프로젝트',
    suggestedMessage: '실장님 안녕하세요, 법률 자문 번역 건 마감 기한을 5월 30일로 확정 지으려 하는데 괜찮으실까요?'
  }
];

export default function TodayTasks() {
  return (
    <div className="space-y-4">
      {MOCK_MESSAGES.map((msg) => (
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
            <button className="bg-indigo-600 text-white py-3 rounded-xl font-medium text-sm hover:bg-indigo-500 transition-colors">바로 전송</button>
            <button className="bg-slate-800 text-slate-200 py-3 rounded-xl font-medium text-sm hover:bg-slate-700">수정</button>
            <button className="bg-transparent text-slate-500 py-3 rounded-xl font-medium text-sm hover:text-slate-300">미루기</button>
          </div>
        </div>
      ))}
    </div>
  );
}
