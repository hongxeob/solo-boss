import { NextResponse } from 'next/server';

export async function GET() {
  const tasks = [
    {
      id: '1',
      clientName: '김철수 대표님',
      projectType: '로고 디자인',
      suggestedMessage: '[API 연동 데이터] 안녕하세요 대표님, 초안 확인 부탁드립니다.'
    }
  ];
  return NextResponse.json(tasks);
}
