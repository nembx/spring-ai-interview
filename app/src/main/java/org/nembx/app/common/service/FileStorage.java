package org.nembx.app.common.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

/**
 * @author Lian
 */
public interface FileStorage {
    String upload(MultipartFile file, String namespace);

    void delete(String key);

    String getUrl(String key);

    InputStream download(String key);

    boolean notExists(String key);
}
