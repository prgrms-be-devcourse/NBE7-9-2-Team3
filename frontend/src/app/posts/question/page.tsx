"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { fetchApi } from "@/lib/client";

interface PostDto {
  id: number;
  title: string;
  nickname: string;
  createDate: string;
  commentCount: number;
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

export default function QuestionBoardPage() {
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);

  const PAGE_SIZE = 10;

  const loadPosts = async (pageToLoad: number) => {
    setLoading(true);
    try {
      const rsData: ApiResponse<PostListResponse> = await fetchApi(
        `/api/posts?boardType=QUESTION&page=${pageToLoad - 1}&size=${PAGE_SIZE}`
      );

      // data 안에서 posts 꺼내기
      const data = rsData.data?.posts ?? [];
      setPosts(data);

      // totalCount 계산
      const totalCount = rsData.data?.totalCount ?? 0;
      setTotalPages(Math.ceil(totalCount / PAGE_SIZE));

      setPage(pageToLoad);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadPosts(1);
  }, []);

  const renderPageButtons = () => {
    const buttons = [];
    for (let i = 1; i <= totalPages; i++) {
      buttons.push(
        <button
          key={i}
          onClick={() => loadPosts(i)}
          className={`px-3 py-1 border rounded ${i === page ? "bg-gray-800 text-white" : "bg-white"
            }`}
        >
          {i}
        </button>
      );
    }
    return buttons;
  };

  return (
    <div className="max-w-3xl mx-auto p-6">
      {/* 제목 + 게시 버튼 */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">질문 게시판</h1>
        <Link
          href="/posts/question/new"
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium"
        >
          새 글 작성
        </Link>
      </div>
      <div className="border-t border-b divide-y">
  {posts.map(post => (
    <Link
      key={post.id}
      href={`/posts/question/${post.id}`}
      className="block py-3 px-2 hover:bg-gray-50"
    >
      {/* 제목 */}
      <p className="font-medium">{post.title}</p>

      {/* 작성자와 작성일 */}
      <div className="flex justify-between text-sm text-gray-500 mt-1">
        <span>작성자: {post.nickname}</span>
        <span>{new Date(post.createDate).toLocaleDateString()}</span>
      </div>
    </Link>
  ))}
</div>

      {loading && <p className="text-center py-4">로딩 중...</p>}

      <div className="flex justify-center gap-2 mt-6">
        {renderPageButtons()}
      </div>
    </div>
  );
}