'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { Trade, ApiResponse, BoardType, TradeStatus } from '@/type/trade';
import { fetchApi } from '@/lib/client';
import { useAuth } from '@/context/AuthContext';

export default function FishMarketPage() {
  const router = useRouter();
  const { isAuthenticated, loading: authLoading } = useAuth();
  const [posts, setPosts] = useState<Trade[]>([]);
  const [filteredPosts, setFilteredPosts] = useState<Trade[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [searchType, setSearchType] = useState('전체'); // 전체, 제목, 본문, 태그
  const [priceRange, setPriceRange] = useState<[number, number]>([0, 100000]);
  const [statusFilter, setStatusFilter] = useState<TradeStatus | '전체'>('전체');
  const [currentPage, setCurrentPage] = useState(1);
  const postsPerPage = 8; // 4열 × 2줄

  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [authLoading, isAuthenticated, router]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchPosts();
    }
  }, [isAuthenticated]);

  const fetchPosts = async () => {
    try {
      setLoading(true);
      // TODO: 나중에 백엔드에 검색 파라미터 추가
      // const params = new URLSearchParams();
      // if (searchTerm) params.append('search', searchTerm);
      // if (searchType !== '전체') params.append('searchType', searchType);
      // params.append('minPrice', priceRange[0].toString());
      // params.append('maxPrice', priceRange[1].toString());
      // const url = `/api/market/fish?${params.toString()}`;

      const data: ApiResponse<Trade[]> = await fetchApi('/api/market/fish');

      if (data.resultCode.startsWith('200')) {
        setPosts(data.data);
        setFilteredPosts(data.data);
      }
    } catch (error) {
      console.error('Failed to load fish market posts:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    // 클라이언트 측 필터링 (임시)
    let filtered = [...posts];

    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter((post) => {
        switch (searchType) {
          case '제목':
            return post.title.toLowerCase().includes(term);
          case '본문':
            return post.description.toLowerCase().includes(term);
          case '태그':
            return post.category?.toLowerCase().includes(term);
          case '전체':
          default:
            return (
              post.title.toLowerCase().includes(term) ||
              post.description.toLowerCase().includes(term) ||
              post.category?.toLowerCase().includes(term)
            );
        }
      });
    }

    filtered = filtered.filter(
      (post) => post.price >= priceRange[0] && post.price <= priceRange[1]
    );

    // 상태 필터 적용
    if (statusFilter !== '전체') {
      filtered = filtered.filter((post) => post.status === statusFilter);
    }

    setFilteredPosts(filtered);
    setCurrentPage(1);

    // TODO: 나중에 백엔드 API 호출로 변경
    // fetchPosts();
  };

  const handleStatusFilter = (status: TradeStatus | '전체') => {
    setStatusFilter(status);
    let filtered = [...posts];

    // 검색어 필터
    if (searchTerm) {
      const term = searchTerm.toLowerCase();
      filtered = filtered.filter((post) => {
        switch (searchType) {
          case '제목':
            return post.title.toLowerCase().includes(term);
          case '본문':
            return post.description.toLowerCase().includes(term);
          case '태그':
            return post.category?.toLowerCase().includes(term);
          case '전체':
          default:
            return (
              post.title.toLowerCase().includes(term) ||
              post.description.toLowerCase().includes(term) ||
              post.category?.toLowerCase().includes(term)
            );
        }
      });
    }

    // 가격 필터
    filtered = filtered.filter(
      (post) => post.price >= priceRange[0] && post.price <= priceRange[1]
    );

    // 상태 필터
    if (status !== '전체') {
      filtered = filtered.filter((post) => post.status === status);
    }

    setFilteredPosts(filtered);
    setCurrentPage(1);
  };

  // 페이지네이션
  const indexOfLastPost = currentPage * postsPerPage;
  const indexOfFirstPost = indexOfLastPost - postsPerPage;
  const currentPosts = filteredPosts.slice(indexOfFirstPost, indexOfLastPost);
  const totalPages = Math.ceil(filteredPosts.length / postsPerPage);

  const paginate = (pageNumber: number) => setCurrentPage(pageNumber);

  // 인증 확인 중이거나 로그인되지 않은 경우
  if (authLoading || !isAuthenticated) {
    return <div className="p-8">Loading...</div>;
  }

  if (loading) {
    return <div className="p-8">Loading...</div>;
  }

  return (
    <div className="max-w-7xl mx-auto p-8">
      {/* 헤더 */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold">물고기 거래</h1>
      </div>

      {/* 검색 및 필터 */}
      <div className="mb-6 space-y-4">
        {/* 검색바 */}
        <div className="flex gap-3">
          <select
            value={searchType}
            onChange={(e) => setSearchType(e.target.value)}
            className="px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 bg-white"
          >
            <option value="전체">전체</option>
            <option value="제목">제목</option>
            <option value="본문">본문</option>
            <option value="태그">태그</option>
          </select>
          <input
            type="text"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
            className="flex-1 px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={handleSearch}
            className="px-6 py-3 bg-blue-500 text-white rounded-lg hover:bg-blue-600 font-semibold transition"
          >
            검색
          </button>
        </div>

        {/* 가격 필터 & 상태 필터 */}
        <div className="flex items-center gap-6">
          <div className="flex items-center gap-3">
            <span className="text-sm text-gray-600 font-medium">가격:</span>
            <input
              type="number"
              value={priceRange[0]}
              onChange={(e) => setPriceRange([Number(e.target.value), priceRange[1]])}
              className="w-32 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <span className="text-gray-600">~</span>
            <input
              type="number"
              value={priceRange[1]}
              onChange={(e) => setPriceRange([priceRange[0], Number(e.target.value)])}
              className="w-32 px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <span className="text-gray-600">원</span>
          </div>

          <div className="flex items-center gap-3">
            <span className="text-sm text-gray-600 font-medium">상태:</span>
            <div className="flex gap-2">
              <button
                onClick={() => handleStatusFilter('전체')}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  statusFilter === '전체'
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                전체
              </button>
              <button
                onClick={() => handleStatusFilter(TradeStatus.SELLING)}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  statusFilter === TradeStatus.SELLING
                    ? 'bg-green-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                판매중
              </button>
              <button
                onClick={() => handleStatusFilter(TradeStatus.COMPLETED)}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  statusFilter === TradeStatus.COMPLETED
                    ? 'bg-gray-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                판매완료
              </button>
              <button
                onClick={() => handleStatusFilter(TradeStatus.CANCELLED)}
                className={`px-4 py-2 rounded-lg font-medium transition ${
                  statusFilter === TradeStatus.CANCELLED
                    ? 'bg-red-500 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                판매취소
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 게시글 목록 */}
      <div>
        {/* 필터 결과 정보 */}
        <div className="mb-4 flex items-center justify-between">
          <p className="text-gray-600">
            전체 <span className="font-semibold text-blue-600">{filteredPosts.length}</span>개
          </p>
          <select className="px-3 py-2 border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500">
            <option>최신순</option>
            <option>낮은 가격순</option>
            <option>높은 가격순</option>
          </select>
        </div>

        {/* 게시글 그리드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
            {currentPosts.map((post) => (
              <Link
                key={post.tradeId}
                href={`/market/fish/${post.tradeId}`}
                className="border rounded-lg overflow-hidden hover:shadow-xl transition-shadow bg-white"
              >
                <div className="h-48 bg-gray-200 relative">
                  {post.images && post.images.length > 0 ? (
                    <img
                      src={post.images[0]}
                      alt={post.title}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center text-gray-400">
                      이미지 없음
                    </div>
                  )}
                  {post.status !== TradeStatus.SELLING && (
                    <div className="absolute inset-0 bg-black bg-opacity-50 flex items-center justify-center">
                      <span className="text-white font-bold text-lg">
                        {post.status === TradeStatus.COMPLETED ? '판매완료' : '판매취소'}
                      </span>
                    </div>
                  )}
                </div>

                <div className="p-4">
                  <div className="flex items-start justify-between mb-2">
                    <h3 className="font-semibold text-lg truncate flex-1">{post.title}</h3>
                    <span
                      className={`ml-2 px-2 py-1 text-xs rounded whitespace-nowrap ${
                        post.status === TradeStatus.SELLING
                          ? 'bg-green-100 text-green-800'
                          : post.status === TradeStatus.COMPLETED
                          ? 'bg-gray-100 text-gray-800'
                          : 'bg-red-100 text-red-800'
                      }`}
                    >
                      {post.status === TradeStatus.SELLING
                        ? '판매중'
                        : post.status === TradeStatus.COMPLETED
                        ? '판매완료'
                        : '판매취소'}
                    </span>
                  </div>
                  <p className="text-gray-600 text-sm line-clamp-2 mb-3">{post.description}</p>
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-xl font-bold text-blue-600">
                      {post.price.toLocaleString()}원
                    </span>
                    <span className="text-xs text-gray-500">{post.memberNickname}</span>
                  </div>
                  {post.category && (
                    <div className="mt-2">
                      <span className="inline-block px-2 py-1 bg-gray-100 text-gray-600 text-xs rounded">
                        {post.category}
                      </span>
                    </div>
                  )}
                </div>
              </Link>
            ))}
        </div>

        {/* 게시글이 없을 때 */}
        {filteredPosts.length === 0 && (
          <div className="text-center py-16 text-gray-500">
            <p className="text-lg mb-2">검색 결과가 없습니다</p>
            <p className="text-sm">다른 검색어를 입력하거나 필터를 조정해보세요</p>
          </div>
        )}

        {/* 페이지네이션 */}
        <div className="flex justify-between items-center mt-8">
          <div className="flex-1"></div>
          {totalPages > 1 && (
            <div className="flex justify-center items-center gap-2 flex-1">
              <button
                onClick={() => paginate(currentPage - 1)}
                disabled={currentPage === 1}
                className="px-3 py-2 border rounded hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                이전
              </button>

              {Array.from({ length: totalPages }, (_, i) => i + 1).map((number) => (
                <button
                  key={number}
                  onClick={() => paginate(number)}
                  className={`px-4 py-2 rounded ${
                    currentPage === number
                      ? 'bg-blue-500 text-white font-semibold'
                      : 'border hover:bg-gray-100'
                  }`}
                >
                  {number}
                </button>
              ))}

              <button
                onClick={() => paginate(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="px-3 py-2 border rounded hover:bg-gray-100 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                다음
              </button>
            </div>
          )}
          <div className="flex-1 flex justify-end">
            <Link
              href="/market/fish/create"
              className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600 font-semibold transition"
            >
              글쓰기
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
