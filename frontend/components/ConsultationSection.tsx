'use client';

import { useState, useEffect } from 'react';
import { useParams } from 'next/navigation';
import { format } from 'date-fns';
import { ko } from 'date-fns/locale';
import { FileText, Plus, Edit, Trash, X, CalendarDays } from 'lucide-react';
import {
  ConsultationItem,
  ConsultationDetail,
  CreateConsultationRequest,
  UpdateConsultationRequest,
} from '../types';
import { api } from '../lib/api';

interface ConsultationCardProps {
  consultation: ConsultationItem;
  onView: (id: string) => void;
  onEdit: (consultation: ConsultationDetail) => void;
  onDelete: (id: string) => void;
}

const ConsultationCard = ({ consultation, onView, onEdit, onDelete }: ConsultationCardProps) => {
  const formattedDate = format(new Date(consultation.consultationDate), 'yyyy년 MM월 dd일', { locale: ko });

  return (
    <div
      className="bg-surface/50 border border-white/5 p-5 rounded-2xl flex flex-col hover:bg-surface transition-colors cursor-pointer"
      onClick={() => onView(consultation.id)}
    >
      <div className="flex justify-between items-start mb-2">
        <p className="text-sm text-slate-500 font-medium flex items-center gap-1">
          <CalendarDays className="w-3 h-3" />
          {formattedDate}
        </p>
        <div className="flex gap-2">
            {consultation.rawText && (
                <FileText className="w-4 h-4 text-slate-400" />
            )}
            <button onClick={(e) => { e.stopPropagation(); onEdit(consultation); }} className="text-slate-400 hover:text-primary transition-colors">
                <Edit className="w-4 h-4" />
            </button>
            <button onClick={(e) => { e.stopPropagation(); onDelete(consultation.id); }} className="text-slate-400 hover:text-red-500 transition-colors">
                <Trash className="w-4 h-4" />
            </button>
        </div>
      </div>
      <p className="text-base font-semibold text-slate-200 line-clamp-2">{consultation.summary}</p>
    </div>
  );
};

interface ConsultationSectionProps {
  customerId: string;
}

export default function ConsultationSection({ customerId }: ConsultationSectionProps) {
  const [consultations, setConsultations] = useState<ConsultationItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingConsultation, setEditingConsultation] = useState<ConsultationDetail | null>(null);
  const [viewingConsultation, setViewingConsultation] = useState<ConsultationDetail | null>(null);

  const fetchConsultations = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const fetchedConsultations = await api.getConsultations(customerId);
      // Sort by consultationDate descending (newest first)
      const sortedConsultations = fetchedConsultations.sort((a, b) => 
        new Date(b.consultationDate).getTime() - new Date(a.consultationDate).getTime()
      );
      setConsultations(sortedConsultations);
    } catch (err) {
      console.error('Failed to fetch consultations:', err);
      setError('상담 이력을 불러오는데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    if (customerId) {
      fetchConsultations();
    }
  }, [customerId]);

  const handleCreateOrUpdateConsultation = async (formData: {
    summary: string;
    rawText?: string;
    consultationDate?: string;
  }) => {
    try {
      if (editingConsultation) {
        await api.updateConsultation(editingConsultation.id, formData);
      } else {
        await api.createConsultation({ ...formData, ownerId: api.OWNER_ID, customerId });
      }
      setIsFormOpen(false);
      setEditingConsultation(null);
      fetchConsultations(); // Re-fetch to update the list
    } catch (err) {
      console.error('Failed to save consultation:', err);
      alert('상담 기록 저장에 실패했습니다.');
    }
  };

  const handleDeleteConsultation = async (id: string) => {
    if (!confirm('정말로 이 상담 기록을 삭제하시겠습니까?')) return;
    try {
      await api.deleteConsultation(id);
      fetchConsultations();
    } catch (err) {
      console.error('Failed to delete consultation:', err);
      alert('상담 기록 삭제에 실패했습니다.');
    }
  };

  const handleViewConsultation = async (id: string) => {
    try {
      const detail = await api.getConsultationDetail(id);
      setViewingConsultation(detail);
    } catch (err) {
      console.error('Failed to fetch consultation detail:', err);
      alert('상담 상세 정보를 불러오는데 실패했습니다.');
    }
  };

  const openCreateForm = () => {
    setEditingConsultation(null);
    setIsFormOpen(true);
  };

  const openEditForm = (consultation: ConsultationDetail) => {
    setEditingConsultation(consultation);
    setIsFormOpen(true);
  };

  const ConsultationForm = () => {
    const today = format(new Date(), 'yyyy-MM-dd');
    const [summary, setSummary] = useState(editingConsultation?.summary || '');
    const [rawText, setRawText] = useState(editingConsultation?.rawText || '');
    const [consultationDate, setConsultationDate] = useState(
      editingConsultation?.consultationDate ? format(new Date(editingConsultation.consultationDate), 'yyyy-MM-dd') : today
    );

    const handleSubmit = (e: React.FormEvent) => {
      e.preventDefault();
      if (!summary.trim()) {
        alert('요약은 필수 입력 항목입니다.');
        return;
      }
      handleCreateOrUpdateConsultation({
        summary,
        rawText: rawText || undefined,
        consultationDate: consultationDate ? `${consultationDate}T09:00:00Z` : undefined, // ISO 8601
      });
    };

    return (
      <div className="fixed inset-0 bg-black/80 flex items-end z-50 animate-in fade-in duration-300">
        <div className="bg-surface rounded-t-3xl p-6 w-full max-h-[90vh] overflow-y-auto animate-in slide-in-from-bottom-full duration-300">
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-bold text-slate-100">{editingConsultation ? '상담 기록 수정' : '새 상담 기록'}</h2>
            <button onClick={() => setIsFormOpen(false)} className="text-slate-400 hover:text-white transition-colors">
              <X className="w-6 h-6" />
            </button>
          </div>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="summary" className="block text-sm font-medium text-slate-400 mb-1">요약 (필수)</label>
              <textarea
                id="summary"
                value={summary}
                onChange={(e) => setSummary(e.target.value)}
                rows={3}
                className="w-full p-3 bg-background border border-white/10 rounded-xl text-slate-100 placeholder-slate-600 focus:ring-primary focus:border-primary outline-none transition-colors"
                placeholder="상담 내용을 요약해주세요."
                required
              ></textarea>
            </div>
            <div>
              <label htmlFor="rawText" className="block text-sm font-medium text-slate-400 mb-1">상세 내용 (선택)</label>
              <textarea
                id="rawText"
                value={rawText}
                onChange={(e) => setRawText(e.target.value)}
                rows={5}
                className="w-full p-3 bg-background border border-white/10 rounded-xl text-slate-100 placeholder-slate-600 focus:ring-primary focus:border-primary outline-none transition-colors"
                placeholder="자세한 상담 내용을 입력해주세요."
              ></textarea>
            </div>
            <div>
              <label htmlFor="consultationDate" className="block text-sm font-medium text-slate-400 mb-1">상담 날짜 (선택)</label>
              <input
                type="date"
                id="consultationDate"
                value={consultationDate}
                onChange={(e) => setConsultationDate(e.target.value)}
                className="w-full p-3 bg-background border border-white/10 rounded-xl text-slate-100 focus:ring-primary focus:border-primary outline-none transition-colors"
              />
            </div>
            <button
              type="submit"
              className="w-full bg-primary text-white font-bold py-3 rounded-xl hover:bg-primary/90 transition-colors"
            >
              {editingConsultation ? '수정하기' : '저장하기'}
            </button>
          </form>
        </div>
      </div>
    );
  };

  const ConsultationDetailModal = () => {
    if (!viewingConsultation) return null;
    const formattedDate = format(new Date(viewingConsultation.consultationDate), 'yyyy년 MM월 dd일', { locale: ko });

    return (
      <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50 animate-in fade-in duration-300 p-6">
        <div className="bg-surface rounded-3xl p-6 w-full max-w-md max-h-[90vh] overflow-y-auto animate-in zoom-in-90 duration-300">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold text-slate-100">상담 상세 내용</h2>
            <button onClick={() => setViewingConsultation(null)} className="text-slate-400 hover:text-white transition-colors">
              <X className="w-6 h-6" />
            </button>
          </div>
          <p className="text-sm text-slate-500 font-medium mb-4 flex items-center gap-1">
            <CalendarDays className="w-3 h-3" />
            {formattedDate}
          </p>
          <div className="space-y-4">
            <div>
              <h3 className="text-md font-semibold text-slate-200 mb-2">요약</h3>
              <p className="text-slate-100">{viewingConsultation.summary}</p>
            </div>
            {viewingConsultation.rawText && (
              <div>
                <h3 className="text-md font-semibold text-slate-200 mb-2">상세 내용</h3>
                <p className="text-slate-100 whitespace-pre-wrap">{viewingConsultation.rawText}</p>
              </div>
            )}
          </div>
        </div>
      </div>
    );
  };

  return (
    <section>
      <div className="flex justify-between items-center mb-4 px-1">
        <h4 className="text-sm font-bold uppercase tracking-widest text-slate-500">상담 이력</h4>
        <button
          onClick={openCreateForm}
          className="bg-primary/20 text-primary hover:bg-primary/30 transition-colors rounded-full p-2 flex items-center justify-center"
        >
          <Plus className="w-4 h-4" />
        </button>
      </div>

      <div className="space-y-3">
        {isLoading ? (
          <div className="text-slate-400 animate-pulse px-1">상담 이력 불러오는 중...</div>
        ) : error ? (
          <div className="text-red-400 px-1">{error}</div>
        ) : consultations.length === 0 ? (
          <div className="text-center py-8 bg-surface/50 rounded-2xl border border-white/5">
            <FileText className="w-12 h-12 text-slate-600 mx-auto mb-4" />
            <p className="text-slate-400">아직 상담 이력이 없습니다.</p>
            <p className="text-slate-400">새로운 상담 기록을 추가해보세요.</p>
          </div>
        ) : (
          consultations.map((consultation) => (
            <ConsultationCard
              key={consultation.id}
              consultation={consultation}
              onView={handleViewConsultation}
              onEdit={openEditForm}
              onDelete={handleDeleteConsultation}
            />
          ))
        )}
      </div>

      {isFormOpen && <ConsultationForm />}
      {viewingConsultation && <ConsultationDetailModal />}
    </section>
  );
}
