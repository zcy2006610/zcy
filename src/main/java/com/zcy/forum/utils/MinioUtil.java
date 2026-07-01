package com.zcy.forum.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Component
public class MinioUtil {
    @Autowired
    private  MinioClient minioClient;

    // 注入 MinIO 桶名
    @Value("${minio.bucketName}")
    private String bucketName;
    // MinIO 访问地址（和 endpoint 一致，用于拼接 URL）
    @Value("${minio.endpoint}")
    private String endpoint;



    /**
     * 上传头像到 MinIO
     * @param file 前端上传的头像文件
     * @return 可访问的头像 URL
     */
    public String uploadAvatar(MultipartFile file) throws Exception {
        // 1. 生成唯一文件名（避免重名覆盖）：用户头像_随机UUID.后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = FilenameUtils.getExtension(originalFilename); // 获取文件后缀（jpg/png等）
        String newFileName = "avatar_" + UUID.randomUUID() + "." + suffix;

        // 2. 上传文件到 MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)          // 桶名
                        .object(newFileName)         // 文件名
                        .stream(file.getInputStream(), file.getSize(), -1) // 文件流
                        .contentType(file.getContentType()) // 文件类型（image/jpeg等）
                        .build()
        );

        // 3. 生成可访问的 URL（公共读桶直接拼接地址）
        // 格式：http://MinIO地址/桶名/文件名
        return endpoint + "/" + bucketName + "/" + newFileName;
    }


    public void deleteOldAvatar(String oldAvatarUrl) throws Exception {
        if (oldAvatarUrl == null || oldAvatarUrl.isEmpty()) {
            return; // 无旧头像则直接返回
        }
        // 从URL中解析出MinIO的文件名（截取最后一个/后的部分）
        String fileName = oldAvatarUrl.substring(oldAvatarUrl.lastIndexOf("/") + 1);
        // 删除MinIO中的旧文件
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fileName)
                        .build()
        );
    }





}