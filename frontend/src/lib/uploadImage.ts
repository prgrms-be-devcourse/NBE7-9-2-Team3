import { fetchApi } from './client';

// Presigned URL 응답 타입
interface PresignedURLResponse {
  presignedUrl: string;
  fileUrl : string;
}

/** 1단계: 백엔드에서 Presigned URL 요청 */
async function getPresignedUrl(
    fileName: string,
    directory: string
): Promise<PresignedURLResponse> {
  const response = await fetchApi('/api/images/presigned-url', {
    method: 'POST',
    body: JSON.stringify({ fileName, directory })
  });
  return response.data;
}

/** 2단계: Presigned URL로 S3에 직접 업로드 */
async function uploadToS3(presignedUrl: string, file: File): Promise<void> {
  const response = await fetch(presignedUrl, {
    method: 'PUT',
    body: file,
    headers: {
      'Content-Type': file.type
    }
  });

  if (!response.ok) {
    throw new Error('S3 업로드 실패');
  }
}

/**
 * 이미지 업로드 전체 프로세스 (메인 함수)
 * @param file - 업로드할 파일
 * @param directory - S3 저장 경로 (예: 'trade', 'profile')
 * @returns 최종 S3 URL (DB 저장용)
 */
export async function uploadImage(
    file: File,
    directory: string
): Promise<string> {
  const { presignedUrl, fileUrl } = await getPresignedUrl(file.name, directory);
  await uploadToS3(presignedUrl, file);
  return fileUrl;
}

/** 여러 이미지 한번에 업로드 */
export async function uploadImages(
    files: File[],
    directory: string
): Promise<string[]> {
  const uploadPromises = files.map(file => uploadImage(file, directory));
  return Promise.all(uploadPromises);
}