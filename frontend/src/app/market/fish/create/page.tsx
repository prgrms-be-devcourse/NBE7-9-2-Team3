'use client';

import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { TradeFormData, TradeStatus, ApiResponse, Trade } from '@/type/trade';
import { fetchApi } from '@/lib/client';
import { useAuth } from '@/context/AuthContext';

export default function FishCreatePage() {
  const router = useRouter();
  const { user, isAuthenticated, loading: authLoading } = useAuth();
  const [formData, setFormData] = useState<TradeFormData>({
    memberId: 0, // Will be set from auth context
    title: '',
    description: '',
    price: 0,
    status: TradeStatus.SELLING,
    category: '',
  });
  const [images, setImages] = useState<File[]>([]);
  const [previews, setPreviews] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  // Check authentication
  useEffect(() => {
    if (!authLoading && !isAuthenticated) {
      alert('로그인이 필요합니다.');
      router.push('/login');
    } else if (user && user.memberId) {
      setFormData(prev => ({ ...prev, memberId: user.memberId }));
    }
  }, [authLoading, isAuthenticated, user, router]);

  // Show loading screen while checking auth
  if (authLoading) {
    return <div className="p-8">로딩 중...</div>;
  }

  // Don't render form if not authenticated
  if (!isAuthenticated || !user) {
    return null;
  }

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>
  ) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'price' ? Number(value) : value,
    }));
  };

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const files = Array.from(e.target.files);
      setImages(files);

      // Create preview URLs
      const previewUrls = files.map((file) => URL.createObjectURL(file));
      setPreviews(previewUrls);
    }
  };

  const removeImage = (index: number) => {
    const newImages = images.filter((_, i) => i !== index);
    const newPreviews = previews.filter((_, i) => i !== index);
    setImages(newImages);
    setPreviews(newPreviews);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      // Create FormData for multipart/form-data
      const formDataToSend = new FormData();
      formDataToSend.append('memberId', formData.memberId.toString());
      formDataToSend.append('title', formData.title);
      formDataToSend.append('description', formData.description);
      formDataToSend.append('price', formData.price.toString());
      formDataToSend.append('status', formData.status);
      formDataToSend.append('category', formData.category);

      // Add images if any
      images.forEach((image) => {
        formDataToSend.append('images', image);
      });

      const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080';
      const response = await fetch(`${baseUrl}/api/market/fish`, {
        method: 'POST',
        credentials: 'include',
        body: formDataToSend,
      });

      if (!response.ok) {
        const errorText = await response.text();
        console.error('Server error response:', errorText);
        throw new Error(`Server returned ${response.status}: ${errorText}`);
      }

      const data: ApiResponse<Trade> = await response.json();

      if (data.resultCode.startsWith('201') || data.resultCode.startsWith('200')) {
        alert('게시글이 등록되었습니다!');
        router.push('/market/fish');
      } else {
        alert('게시글 등록 실패: ' + data.msg);
      }
    } catch (error) {
      console.error('Failed to create post:', error);
      if (error instanceof TypeError && error.message === 'Failed to fetch') {
        alert('서버에 연결할 수 없습니다. 백엔드 서버(http://localhost:8080)가 실행 중인지 확인해주세요.');
      } else {
        alert('게시글 등록 실패: ' + (error instanceof Error ? error.message : 'Unknown error'));
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-8">
      <div className="mb-6">
        <Link href="/market/fish" className="text-blue-500 hover:underline">
          ← 목록으로
        </Link>
      </div>

      <h1 className="text-3xl font-bold mb-8">물고기 거래 글쓰기</h1>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* 제목 */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            제목
          </label>
          <input
            type="text"
            name="title"
            value={formData.title}
            onChange={handleChange}
            required
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            placeholder="제목을 입력하세요"
          />
        </div>

        {/* 이미지 업로드 */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            이미지 추가
          </label>

          <div className="grid grid-cols-4 gap-4">
            {/* 이미지 추가 버튼 */}
            <label className="border-2 border-dashed border-gray-300 rounded-lg h-32 flex flex-col items-center justify-center cursor-pointer hover:border-blue-500 hover:bg-blue-50 transition">
              <div className="text-4xl text-gray-400 mb-1">+</div>
              <span className="text-sm text-gray-500">이미지 추가</span>
              <input
                type="file"
                accept="image/*"
                multiple
                onChange={handleImageChange}
                className="hidden"
              />
            </label>

            {/* 이미지 미리보기 */}
            {previews.map((preview, index) => (
              <div key={index} className="relative group">
                <img
                  src={preview}
                  alt={`Preview ${index + 1}`}
                  className="w-full h-32 object-cover rounded-lg border"
                />
                <button
                  type="button"
                  onClick={() => removeImage(index)}
                  className="absolute top-2 right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center opacity-0 group-hover:opacity-100 transition"
                >
                  ×
                </button>
              </div>
            ))}
          </div>
          <p className="text-xs text-gray-500 mt-2">
            * 최대 5개, 각 파일은 5MB 이하, jpg/jpeg/png/gif/webp만 가능
          </p>
        </div>

        {/* 가격 */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            가격
          </label>
          <div className="flex items-center gap-2">
            <input
              type="number"
              name="price"
              value={formData.price}
              onChange={handleChange}
              required
              min="0"
              className="flex-1 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              placeholder="0원 입력 시 나눔 게시글로 등록됩니다"
            />
            <span className="text-gray-600 font-medium">원</span>
          </div>
        </div>

        {/* 카테고리 (선택) */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            태그 (선택)
          </label>
          <input
            type="text"
            name="category"
            value={formData.category}
            onChange={handleChange}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* 설명 */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            설명
          </label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            required
            className="w-full p-3 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={10}
            placeholder="물고기에 대한 자세한 설명을 입력하세요&#10;&#10;예시:&#10;- 어종: 구피 / 네온테트라 등&#10;- 크기: 약 3cm&#10;- 나이: 생후 6개월&#10;- 건강 상태: 양호&#10;- 거래 장소: 서울 강남구 등"
          />
          <p className="text-xs text-gray-500 mt-2">
            * 물고기 종류, 크기, 건강 상태, 거래 장소 등을 자세히 적어주세요
          </p>
        </div>

        {/* 상태 (판매중/판매완료) */}
        <div className="bg-gray-50 p-6 rounded-lg">
          <label className="block text-sm font-semibold mb-3 flex items-center">
            <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
            판매 상태
          </label>
          <select
            name="status"
            value={formData.status}
            onChange={handleChange}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value={TradeStatus.SELLING}>판매중</option>
            <option value={TradeStatus.COMPLETED}>판매완료</option>
            <option value={TradeStatus.CANCELLED}>판매취소</option>
          </select>
        </div>

        {/* 버튼 */}
        <div className="flex gap-4 pt-4">
          <button
            type="submit"
            disabled={loading}
            className="flex-1 px-6 py-4 bg-blue-500 text-white rounded-lg hover:bg-blue-600 disabled:bg-gray-400 font-semibold text-lg transition"
          >
            {loading ? '등록 중...' : '등록하기'}
          </button>
          <Link
            href="/market/fish"
            className="px-6 py-4 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 font-semibold text-lg transition flex items-center justify-center"
          >
            취소
          </Link>
        </div>
      </form>
    </div>
  );
}
