package com.censodev.minidrive.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface AwsS3Service {
    void upload(InputStream is, Path path) throws IOException;

    void delete(Path path);

    void delete(List<Path> paths);

    InputStream load(Path path);
}
