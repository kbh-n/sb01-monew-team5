package com.example.part35teammonew.domain.article.batch;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("dev")
@Service
@RequiredArgsConstructor
public class S3UploadArticle {

  private final AmazonS3 amazonS3Client;

  @Value("${cloud.aws.s3.bucket}")
  private String bucketName;

  public void upload(File file, String filename) {
    try (FileInputStream inputStream = new FileInputStream(file)) {
      ObjectMetadata metadata = new ObjectMetadata();
      metadata.setContentLength(file.length());
      metadata.setContentType("application/json");

      PutObjectRequest request = new PutObjectRequest(bucketName, filename, inputStream, metadata);
      amazonS3Client.putObject(request);
    } catch (Exception e) {
      throw new IllegalArgumentException("Failed to upload file to S3", e);
    }
  }

  public void delete(String filename) {
    try {
      amazonS3Client.deleteObject(bucketName, filename);
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }
  public boolean exists(String fileName){
    return amazonS3Client.doesObjectExist(bucketName, fileName);
  }

  public void download(File file) {
    try (FileOutputStream outputStream = new FileOutputStream(file)) {
      String today = LocalDate.now().toString();
      amazonS3Client.getObject(bucketName, "articles_" + today + ".json")
          .getObjectContent()
          .transferTo(outputStream);
    } catch (Exception e) {
      throw new IllegalArgumentException("S3에서 파일 다운로드 실패: " + file.getName(), e);
    }
  }
}