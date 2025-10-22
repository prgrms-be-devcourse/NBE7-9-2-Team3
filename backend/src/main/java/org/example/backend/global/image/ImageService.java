package org.example.backend.global.image;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.example.backend.global.exception.BusinessException;
import org.example.backend.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * AWS S3 이미지 Presigned URL 및 삭제 공통 서비스
 *
 * <p>Presigned URL을 통한 클라이언트 직접 업로드 방식 사용
 * <p>업로드된 파일은 UUID로 고유한 이름 생성
 */
@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    private final S3Presigner s3Presigner;


    /** Presigned URL + 최종 URL 함께 생성 */
    public PresignedUrlResponse createPresignedUrl(String fileName, String directory) {
        String key = generateKey(fileName, directory);
        String presignedUrl = generatePresignedUrl(key);
        String finalUrl = getFileUrl(key);

        return new PresignedUrlResponse(presignedUrl, finalUrl);
    }

    /** S3에 PUT 요청할 수 있는 임시 URL 생성 */
    private String generatePresignedUrl(String key) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(key)
            .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .putObjectRequest(putObjectRequest)
            .signatureDuration(Duration.ofMinutes(10))
            .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);
        return presignedRequest.url().toString();
    }

    /** S3 객체 키 생성 (디렉토리/UUID.확장자) */
    private String generateKey(String fileName, String directory) {
        String extension = getFileExtension(fileName);
        String uuid = UUID.randomUUID().toString();
        return directory + "/" + uuid + "." + extension;
    }

    /** S3 객체의 공개 접근 URL 생성 */
    public String getFileUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, key);
    }

    /**
     * S3에서 단일 파일 삭제
     * @param fileUrl 삭제할 파일의 S3 URL
     */
    public void deleteFile(String fileUrl) {
        try {
            String fileName = extractNameFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .build();

            s3Client.deleteObject(deleteObjectRequest);

        } catch (Exception e) {
            throw new BusinessException(ErrorCode.IMAGE_NOT_UPLOADED);
        }
    }

    /** S3에서 여러 파일 삭제 */
    public void deleteFiles(List<String> fileUrls) {
        fileUrls.forEach(this::deleteFile);
    }

    /** 파일 확장자 추출 */
    private String getFileExtension(String filename) {
        int lastIndexOf = filename.lastIndexOf('.');
        if (lastIndexOf == -1) {
            return "";
        }
        return filename.substring(lastIndexOf + 1);
    }

    /**
     * S3 URL에서 파일명(키) 추출
     * 보안을 위해 자체 S3 버킷의 URL만 허용
     */
    private String extractNameFromUrl(String fileUrl) {
        try {
            URL url = new URL(fileUrl);

            // S3 URL 검증 (자체 버킷만 허용)
            String expectedHost = bucket + ".s3." + region + ".amazonaws.com";
            if (!url.getHost().equals(expectedHost)) {
                throw new BusinessException(ErrorCode.IMAGE_URL_NOT_ALLOWED);
            }
            String path = url.getPath();
            return path.startsWith("/") ? path.substring(1) : path;

        } catch (MalformedURLException e) {
            throw new BusinessException(ErrorCode.IMAGE_URL_INVALID);
        }
    }
}