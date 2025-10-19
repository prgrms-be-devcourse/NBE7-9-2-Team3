'use client';

import { useEffect, useState } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Link from 'next/link';
import { Trade, ApiResponse, TradeStatus, TradeComment, CommentFormData } from '@/type/trade';
import { fetchApi } from '@/lib/client';
import { useAuth } from '@/context/AuthContext';

export default function SecondhandDetailPage() {
  const router = useRouter();
  const params = useParams();
  const tradeId = params.id as string;
  const { user, isAuthenticated } = useAuth();

  const [post, setPost] = useState<Trade | null>(null);
  const [comments, setComments] = useState<TradeComment[]>([]);
  const [loading, setLoading] = useState(true);
  const [commentText, setCommentText] = useState('');
  const [currentImageIndex, setCurrentImageIndex] = useState(0);
  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [editingCommentText, setEditingCommentText] = useState('');

  useEffect(() => {
    if (tradeId) {
      fetchPost();
      fetchComments();
    }
  }, [tradeId]);

  const fetchPost = async () => {
    try {
      const data: ApiResponse<Trade> = await fetchApi(`/api/market/secondhand/${tradeId}`);
      if (data.resultCode.startsWith('200')) {
        setPost(data.data);
      }
    } catch (error) {
      console.error('Failed to load post:', error);
      alert('게시글을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const fetchComments = async () => {
    try {
      const data: ApiResponse<TradeComment[]> = await fetchApi(`/api/market/secondhand/${tradeId}/comments`);
      if (data.resultCode.startsWith('200')) {
        setComments(data.data);
      }
    } catch (error) {
      console.error('Failed to load comments:', error);
    }
  };

  const handleDelete = async () => {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
      await fetchApi(`/api/market/secondhand/${tradeId}`, { method: 'DELETE' });
      alert('게시글이 삭제되었습니다.');
      router.push('/market/secondhand');
    } catch (error) {
      console.error('Failed to delete post:', error);
      alert('게시글 삭제에 실패했습니다.');
    }
  };

  const handleCommentSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!commentText.trim()) return;

    // 로그인 확인
    if (!isAuthenticated || !user) {
      alert('로그인이 필요합니다.');
      router.push('/login');
      return;
    }

    try {
      const commentData: CommentFormData = {
        memberId: user.memberId,
        tradeId: Number(tradeId),
        content: commentText,
      };

      await fetchApi(`/api/market/secondhand/${tradeId}/comments`, {
        method: 'POST',
        body: JSON.stringify(commentData),
      });

      setCommentText('');
      fetchComments();
    } catch (error) {
      console.error('Failed to post comment:', error);
      alert('댓글 작성에 실패했습니다.');
    }
  };

  const nextImage = () => {
    if (post && post.images.length > 0) {
      setCurrentImageIndex((prev) => (prev + 1) % post.images.length);
    }
  };

  const prevImage = () => {
    if (post && post.images.length > 0) {
      setCurrentImageIndex((prev) => (prev - 1 + post.images.length) % post.images.length);
    }
  };

  const handleCommentDelete = async (commentId: number) => {
    if (!confirm('댓글을 삭제하시겠습니까?')) return;

    try {
      await fetchApi(`/api/market/secondhand/${tradeId}/comments/${commentId}`, {
        method: 'DELETE',
      });
      fetchComments();
    } catch (error) {
      console.error('Failed to delete comment:', error);
      alert('댓글 삭제에 실패했습니다.');
    }
  };

  const handleCommentEdit = (commentId: number, currentText: string) => {
    setEditingCommentId(commentId);
    setEditingCommentText(currentText);
  };

  const handleCommentUpdate = async (commentId: number) => {
    if (!editingCommentText.trim()) return;

    try {
      const commentData = {
        memberId: user?.memberId,
        tradeId: Number(tradeId),
        content: editingCommentText,
      };

      await fetchApi(`/api/market/secondhand/${tradeId}/comments/${commentId}`, {
        method: 'PUT',
        body: JSON.stringify(commentData),
      });

      setEditingCommentId(null);
      setEditingCommentText('');
      fetchComments();
    } catch (error) {
      console.error('Failed to update comment:', error);
      alert('댓글 수정에 실패했습니다.');
    }
  };

  const handleCancelEdit = () => {
    setEditingCommentId(null);
    setEditingCommentText('');
  };

  if (loading) {
    return <div className="p-8">Loading...</div>;
  }

  if (!post) {
    return <div className="p-8">게시글을 찾을 수 없습니다.</div>;
  }

  return (
    <div className="max-w-6xl mx-auto p-8">
      <div className="mb-6">
        <Link href="/market/secondhand" className="text-blue-500 hover:underline">
          ← 목록으로
        </Link>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* 왼쪽: 이미지 */}
        <div className="space-y-4">
          <div className="relative bg-gray-100 rounded-lg overflow-hidden" style={{ height: '500px' }}>
            {post.images && post.images.length > 0 ? (
              <>
                <img
                  src={post.images[currentImageIndex]}
                  alt={post.title}
                  className="w-full h-full object-contain"
                />
                {post.images.length > 1 && (
                  <>
                    <button
                      onClick={prevImage}
                      className="absolute left-4 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white rounded-full w-10 h-10 flex items-center justify-center hover:bg-opacity-70"
                    >
                      ‹
                    </button>
                    <button
                      onClick={nextImage}
                      className="absolute right-4 top-1/2 -translate-y-1/2 bg-black bg-opacity-50 text-white rounded-full w-10 h-10 flex items-center justify-center hover:bg-opacity-70"
                    >
                      ›
                    </button>
                    <div className="absolute bottom-4 left-1/2 -translate-x-1/2 bg-black bg-opacity-50 text-white px-3 py-1 rounded-full text-sm">
                      {currentImageIndex + 1} / {post.images.length}
                    </div>
                  </>
                )}
              </>
            ) : (
              <div className="w-full h-full flex items-center justify-center text-gray-400">
                이미지 없음
              </div>
            )}
          </div>

          {/* 썸네일 */}
          {post.images && post.images.length > 1 && (
            <div className="flex gap-2 overflow-x-auto">
              {post.images.map((image, index) => (
                <button
                  key={index}
                  onClick={() => setCurrentImageIndex(index)}
                  className={`flex-shrink-0 w-20 h-20 rounded-lg overflow-hidden border-2 ${
                    currentImageIndex === index ? 'border-blue-500' : 'border-transparent'
                  }`}
                >
                  <img src={image} alt={`Thumbnail ${index + 1}`} className="w-full h-full object-cover" />
                </button>
              ))}
            </div>
          )}
        </div>

        {/* 오른쪽: 정보 */}
        <div className="space-y-6">
          {/* 제목 & 상태 */}
          <div>
            <div className="flex items-center gap-2 mb-2">
              {post.category && (
                <span className="inline-block px-3 py-1 bg-gray-100 text-gray-600 text-sm rounded-full">
                  {post.category}
                </span>
              )}
              <span
                className={`inline-block px-3 py-1 text-sm rounded-full ${
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
            <h1 className="text-3xl font-bold mb-4">{post.title}</h1>
            <div className="flex items-center justify-between text-sm text-gray-500 mb-4">
              <span>{post.memberNickname}</span>
              <span>{new Date(post.createdDate).toLocaleDateString('ko-KR')}</span>
            </div>
            <div className="text-4xl font-bold text-blue-600 mb-6">
              {post.price.toLocaleString()}원
            </div>
          </div>

          {/* 설명 */}
          <div className="bg-gray-50 p-6 rounded-lg">
            <h3 className="font-semibold mb-3 flex items-center">
              <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
              상품 설명
            </h3>
            <p className="text-gray-700 whitespace-pre-wrap leading-relaxed">{post.description}</p>
          </div>

          {/* 버튼 */}
          <div className="space-y-3">
            <div className="flex gap-3">
              <Link
                href={`/market/secondhand/${tradeId}/edit`}
                className="flex-1 bg-blue-500 text-white py-3 rounded-lg hover:bg-blue-600 font-semibold text-center transition"
              >
                수정
              </Link>
              <button
                onClick={handleDelete}
                className="flex-1 bg-red-500 text-white py-3 rounded-lg hover:bg-red-600 font-semibold transition"
              >
                삭제
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* 댓글 섹션 */}
      <div className="mt-12 border-t pt-8">
        <h2 className="text-2xl font-bold mb-6">댓글 ({comments.length})</h2>

        {/* 댓글 작성 */}
        <form onSubmit={handleCommentSubmit} className="mb-8">
          <textarea
            value={commentText}
            onChange={(e) => setCommentText(e.target.value)}
            className="w-full p-4 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
            rows={4}
            placeholder="댓글을 입력하세요"
          />
          <div className="flex justify-end mt-3">
            <button
              type="submit"
              className="px-6 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 font-semibold"
            >
              댓글 작성
            </button>
          </div>
        </form>

        {/* 댓글 목록 */}
        <div className="space-y-4">
          {comments.length === 0 ? (
            <div className="text-center py-8 text-gray-500">첫 댓글을 작성해보세요!</div>
          ) : (
            comments.map((comment) => (
              <div key={comment.commentId} className="bg-gray-50 p-4 rounded-lg">
                <div className="flex items-center justify-between mb-2">
                  <span className="font-semibold">{comment.memberNickname}</span>
                  <div className="flex items-center gap-3">
                    <span className="text-sm text-gray-500">
                      {new Date(comment.createdDate).toLocaleDateString('ko-KR')}
                    </span>
                    {user && user.memberId === comment.memberId && editingCommentId !== comment.commentId && (
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleCommentEdit(comment.commentId, comment.comment)}
                          className="text-sm text-blue-600 hover:text-blue-700"
                        >
                          수정
                        </button>
                        <button
                          onClick={() => handleCommentDelete(comment.commentId)}
                          className="text-sm text-red-600 hover:text-red-700"
                        >
                          삭제
                        </button>
                      </div>
                    )}
                  </div>
                </div>
                {editingCommentId === comment.commentId ? (
                  <div className="space-y-2">
                    <textarea
                      value={editingCommentText}
                      onChange={(e) => setEditingCommentText(e.target.value)}
                      className="w-full p-2 border border-gray-300 rounded-lg resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
                      rows={3}
                    />
                    <div className="flex gap-2 justify-end">
                      <button
                        onClick={() => handleCommentUpdate(comment.commentId)}
                        className="px-4 py-1 bg-blue-500 text-white rounded hover:bg-blue-600 text-sm"
                      >
                        저장
                      </button>
                      <button
                        onClick={handleCancelEdit}
                        className="px-4 py-1 bg-gray-300 text-gray-700 rounded hover:bg-gray-400 text-sm"
                      >
                        취소
                      </button>
                    </div>
                  </div>
                ) : (
                  <p className="text-gray-700">{comment.comment}</p>
                )}
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}
