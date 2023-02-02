package com.censodev.minidrive.services.impl;

import com.censodev.minidrive.services.AwsS3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.List;

@Service
@Slf4j
public class AwsS3ServiceImpl implements AwsS3Service {
    @Value("${aws.s3.bucket-name}")
    private String BUCKET_NAME;

    @Value("${aws.secret-key}")
    private String SECRET_KEY;

    @Value("${aws.access-key}")
    private String ACCESS_KEY;

    private S3Client s3;

    @PostConstruct
    public void postConstruct() {
        s3 = S3Client.builder()
                .region(Region.AP_SOUTHEAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();
        try {
            s3.createBucket(CreateBucketRequest.builder()
                    .bucket(BUCKET_NAME)
                    .build());
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private String getKeyFromPath(Path path) {
        return path.toString().replaceAll("\\\\", "/");
    }

    @Override
    public void upload(InputStream is, Path path) throws IOException {
        var req = PutObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(getKeyFromPath(path))
                .build();
        s3.putObject(req, RequestBody.fromByteBuffer(ByteBuffer.wrap(is.readAllBytes())));
    }

    @Override
    public void delete(Path path) {
        var req = DeleteObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(getKeyFromPath(path))
                .build();
        s3.deleteObject(req);
    }

    @Override
    public void delete(List<Path> paths) {
        paths.forEach(this::delete);
    }

    @Override
    public InputStream load(Path path) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(getKeyFromPath(path))
                .build();
        return s3.getObject(getObjectRequest);
    }
}
