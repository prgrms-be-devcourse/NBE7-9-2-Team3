"use client";

import { useEffect, useState, useRef, useCallback } from "react";
import Link from "next/link";
import { fetchApi } from "@/lib/client";

interface PostDto {
  id: number;
  title: string;
  content: string;
  nickname: string;
  createDate: string;
  images: string[];
  likeCount: number;
  liked?: boolean; // 로그인 사용자 좋아요 여부
}

interface PostListResponse {
  posts: PostDto[];
  totalCount: number;
}

interface ApiResponse<T> {
  resultCode: string;
  msg: string;
  data: T;
}

// -------------------- PostItem --------------------
function PostItem({
  post,
  onLike,
}: {
  post: PostDto;
  onLike: (liked: boolean, likeCount: number) => void;
}) {
  const [currentImage, setCurrentImage] = useState(0);

  const nextImage = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setCurrentImage((prev) => (prev + 1) % post.images.length);
  };

  const prevImage = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    setCurrentImage((prev) => (prev - 1 + post.images.length) % post.images.length);
  };

  const handleToggleLike = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();

    const newLiked = !post.liked;
    const newLikeCount = newLiked ? post.likeCount + 1 : post.likeCount - 1;

    // 1️⃣ 부모 상태 즉시 갱신
    onLike(newLiked, newLikeCount);

    try {
      // 2️⃣ 서버 요청
      const rs: ApiResponse<{ liked: boolean; likeCount: number }> = await fetchApi(
        `/api/posts/${post.id}/likes`,
        { method: "POST" }
      );

      // 3️⃣ 서버 결과로 최종 동기화
      onLike(rs.data?.liked ?? newLiked, rs.data?.likeCount ?? newLikeCount);
    } catch (err) {
      console.error(err);
      // 실패 시 롤백
      onLike(post.liked ?? false, post.likeCount);
    }
  };

  return (
    <div className="border-b border-gray-200 pb-4 mb-4 last:mb-0 last:pb-0 last:border-0 hover:bg-gray-50 p-2 rounded">
      <Link href={`/posts/showoff/${post.id}`} className="block">
        <p className="font-bold">{post.title}</p>

        {post.images && post.images.length > 0 && (
          <div className="relative w-full h-64 my-2 bg-gray-100 rounded overflow-hidden flex items-center justify-center">
            <img
              src={post.images[currentImage]}
              alt={post.title}
              className="max-w-full max-h-full object-contain"
            />
            {post.images.length > 1 && (
              <>
                <button
                  className="absolute top-1/2 left-2 transform -translate-y-1/2 bg-black bg-opacity-40 text-white p-1 rounded-full"
                  onClick={prevImage}
                >
                  ◀
                </button>
                <button
                  className="absolute top-1/2 right-2 transform -translate-y-1/2 bg-black bg-opacity-40 text-white p-1 rounded-full"
                  onClick={nextImage}
                >
                  ▶
                </button>
              </>
            )}
          </div>
        )}

        <p className="text-gray-600">{post.content}</p>
        <div className="flex justify-between text-sm text-gray-500 mt-1">
          <span>작성자: {post.nickname}</span>
          <span>{new Date(post.createDate).toLocaleDateString()}</span>
        </div>
      </Link>

      {/* 좋아요 버튼 */}
      <button
        onClick={handleToggleLike}
        className={`mt-2 px-2 py-1 rounded ${post.liked ? "bg-red-500 text-white" : "bg-gray-200"}`}
      >
        ❤️ {post.likeCount}
      </button>
    </div>
  );
}

// -------------------- PostListPage --------------------
export default function PostListPage() {
  const [activeTab, setActiveTab] = useState<"all" | "following">("all");
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [totalCount, setTotalCount] = useState(0);

  const pageRef = useRef(0);
  const hasNextRef = useRef(true);
  const loadingRef = useRef(false);

  const PAGE_SIZE = 10;

  const loadPosts = useCallback(
    async (reset = false) => {
      if (loadingRef.current || (!hasNextRef.current && !reset)) return;
      loadingRef.current = true;

      const pageToLoad = reset ? 0 : pageRef.current;

      try {
        const rsData: ApiResponse<PostListResponse> = await fetchApi(
          `/api/posts?boardType=SHOWOFF&filterType=${activeTab}&page=${pageToLoad}&size=${PAGE_SIZE}`
        );

        const data = rsData.data?.posts ?? [];
        const total = rsData.data?.totalCount ?? 0;

        setPosts((prev) => (reset ? data : [...prev, ...data]));
        setTotalCount(total);

        pageRef.current = pageToLoad + 1;
        hasNextRef.current = (reset ? data.length : pageRef.current * PAGE_SIZE) < total;
      } catch (err) {
        console.error(err);
      } finally {
        loadingRef.current = false;
      }
    },
    [activeTab]
  );

  useEffect(() => {
    pageRef.current = 0;
    hasNextRef.current = true;
    loadPosts(true);
  }, [activeTab, loadPosts]);

  const handleScroll = useCallback(() => {
    if (
      window.innerHeight + window.scrollY >= document.body.offsetHeight - 300 &&
      hasNextRef.current
    ) {
      loadPosts();
    }
  }, [loadPosts]);

  useEffect(() => {
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [handleScroll]);

  // -------------------- 좋아요 상태 업데이트 함수 --------------------
  const handleLikeUpdate = (postId: number, liked: boolean, likeCount: number) => {
    setPosts((prev) =>
      prev.map((p) => (p.id === postId ? { ...p, liked, likeCount } : p))
    );
  };

  return (
    <div className="max-w-2xl mx-auto p-6">
      <div className="flex items-center mb-6 justify-between">
        <div className="flex gap-4">
          {["all", "following"].map((tab) => (
            <button
              key={tab}
              className={`px-4 py-2 rounded ${
                activeTab === tab ? "bg-gray-800 text-white" : "bg-gray-100"
              }`}
              onClick={() => setActiveTab(tab as "all" | "following")}
            >
              {tab === "all" ? "전체" : "팔로잉"}
            </button>
          ))}
        </div>

        <Link
          href="/posts/showoff/new"
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium"
        >
          새 글 작성
        </Link>
      </div>

      {posts.map((post) => (
        <PostItem
          key={post.id}
          post={post}
          onLike={(liked, likeCount) => handleLikeUpdate(post.id, liked, likeCount)}
        />
      ))}

      {loadingRef.current && <p className="text-center py-4">Loading...</p>}
      {!hasNextRef.current && posts.length > 0 && (
        <p className="text-center py-4 text-gray-500">더 이상 게시물이 없습니다.</p>
      )}
    </div>
  );
}
