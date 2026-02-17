'use client';

import { useState } from 'react';
import { TabType } from '../types';
import TodayTasks from '../components/TodayTasks';
import ReviewBox from '../components/ReviewBox';
import ClientList from '../components/ClientList';
import Statistics from '../components/Statistics';
import BottomNav from '../components/BottomNav';

export default function Home() {
  const [activeTab, setActiveTab] = useState<TabType>('today');

  return (
    <main className="min-h-screen bg-background text-slate-100 pb-24">
      {/* Header */}
      <header className="p-6 border-b border-white/5 bg-background/50 backdrop-blur-md sticky top-0 z-10">
        <h1 className="text-xl font-bold tracking-tight">
          {activeTab === 'today' && '오늘 할 일'}
          {activeTab === 'review' && '검수함'}
          {activeTab === 'clients' && '고객 리스트'}
          {activeTab === 'stats' && '비즈니스 리포트'}
        </h1>
      </header>

      {/* Content */}
      <div className="p-4">
        {activeTab === 'today' && <TodayTasks />}
        {activeTab === 'review' && <ReviewBox />}
        {activeTab === 'clients' && <ClientList />}
        {activeTab === 'stats' && <Statistics />}
      </div>

      {/* Navigation */}
      <BottomNav activeTab={activeTab} onTabChange={setActiveTab} />
    </main>
  );
}
