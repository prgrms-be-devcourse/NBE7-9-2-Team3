'use client';

import Link from 'next/link';
import { useAuth } from '@/context/AuthContext';
import { useState } from 'react';

export default function Navbar() {
  const { user, logout } = useAuth();
  const [isCommunityOpen, setIsCommunityOpen] = useState(false);
  const [isMarketOpen, setIsMarketOpen] = useState(false);

  const handleCommunityEnter = () => {
    setIsCommunityOpen(true);
  };

  const handleCommunityLeave = () => {
    setIsCommunityOpen(false);
  };

  const handleMarketEnter = () => {
    setIsMarketOpen(true);
  };

  const handleMarketLeave = () => {
    setIsMarketOpen(false);
  };

  return (
    <nav className="bg-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex items-center">
            <Link href="/" className="flex-shrink-0 flex items-center">
              <span className="text-2xl font-bold text-blue-600">우리집 물고기</span>
            </Link>
          </div>
          
          {/* 메인 메뉴 */}
          <div className="hidden md:flex items-center space-x-8">
            <Link 
              href="/aquarium" 
              className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors"
            >
              어항관리
            </Link>
            
            {/* 커뮤니티 드롭다운 */}
            <div 
              className="relative"
              onMouseEnter={handleCommunityEnter}
              onMouseLeave={handleCommunityLeave}
            >
              <button className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors flex items-center">
                커뮤니티
                <svg className="ml-1 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              {isCommunityOpen && (
                <div className="absolute left-0 top-full w-48 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-200">
                  <Link href="/community/boast" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                    자랑게시판
                  </Link>
                  <Link href="/community/question" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                    질문 게시판
                  </Link>
                </div>
              )}
            </div>
            
            {/* 거래마켓 드롭다운 */}
            <div 
              className="relative"
              onMouseEnter={handleMarketEnter}
              onMouseLeave={handleMarketLeave}
            >
              <button className="text-gray-700 hover:text-blue-600 px-3 py-2 rounded-md text-sm font-medium transition-colors flex items-center">
                거래마켓
                <svg className="ml-1 h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              
              {isMarketOpen && (
                <div className="absolute left-0 top-full w-48 bg-white rounded-md shadow-lg py-1 z-50 border border-gray-200">
                  <Link href="/market/used" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                    중고물품 거래
                  </Link>
                  <Link href="/market/fish" className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-100">
                    물고기 거래
                  </Link>
                </div>
              )}
            </div>
          </div>
          
          {/* 사용자 메뉴 */}
          <div className="flex items-center space-x-4">
            {user ? (
              <>
                <span className="text-gray-700">안녕하세요, {user.nickname}님!</span>
                <button
                  onClick={logout}
                  className="bg-red-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-red-700"
                >
                  로그아웃
                </button>
              </>
            ) : (
              <>
                <Link
                  href="/login"
                  className="bg-blue-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-blue-700"
                >
                  로그인
                </Link>
                <Link
                  href="/signup"
                  className="bg-green-600 text-white px-4 py-2 rounded-md text-sm font-medium hover:bg-green-700"
                >
                  회원가입
                </Link>
              </>
            )}
          </div>
        </div>
      </div>
    </nav>
  );
}
