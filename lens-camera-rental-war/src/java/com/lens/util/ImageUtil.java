package com.lens.util;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

/**
 * Utility class xử lý đường dẫn và tài nguyên hình ảnh trong JSF.
 */
public class ImageUtil {

    public static final String DEFAULT_IMAGE_NAME = "default-img.png";
    public static final String DEFAULT_IMAGE_LIBRARY = "images";
    public static final String DEVICE_IMAGE_PREFIX = "device/";

    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp");
    private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/pjpeg", "image/x-png");

    private ImageUtil() {
    }
    
    /**
     * Tìm thư mục web trong source project thật (lens-camera-rental-war/web).
     */
    private static File findSourceWebDirectory(String uploadDirRealPath) {
        if (uploadDirRealPath == null || uploadDirRealPath.isBlank()) {
            return null;
        }

        try {
            File current = new File(uploadDirRealPath);

            while (current != null) {
                // 1. Trường hợp dự án EAR đa module (thư mục root chứa lens-camera-rental-war/web)
                File warWeb = new File(current, "lens-camera-rental-war/web");
                if (warWeb.exists() && warWeb.isDirectory()) {
                    return warWeb;
                }

                // 2. Trường hợp current chính là thư mục module lens-camera-rental-war
                File srcDir = new File(current, "src");
                File webDir = new File(current, "web");

                if (!current.getName().equalsIgnoreCase("build")
                        && !current.getName().equalsIgnoreCase("dist")
                        && srcDir.exists() && srcDir.isDirectory()
                        && webDir.exists() && webDir.isDirectory()) {
                    return webDir;
                }

                current = current.getParentFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lưu ảnh vào thư mục source của project.
     *
     * @param targetFile file ảnh trong runtime
     * @param fileName tên file ảnh
     * @param uploadDirRealPath đường dẫn thư mục runtime
     * @param subDir thư mục con
     */
    private static void saveToSourceDirectory(
            File targetFile,
            String fileName,
            String uploadDirRealPath,
            String subDir) {

        try {
            File webDir = findSourceWebDirectory(uploadDirRealPath);
            if (webDir != null) {
                File sourceDir = new File(webDir, "resources/images/" + (subDir != null && !subDir.isBlank()
                        ? subDir : "")
                );

                if (!sourceDir.exists()) {
                    sourceDir.mkdirs();
                }

                File sourceFile = new File(sourceDir, fileName);
                Files.copy(targetFile.toPath(), sourceFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("[ImageUtil] Saved to source: " + sourceFile.getAbsolutePath());

                // Đồng thời copy vào build/web (nếu có) để đảm bảo đồng bộ
                File buildWebDir = new File(webDir.getParentFile(), "build/web/resources/images/" + (subDir != null && !subDir.isBlank() ? subDir : ""));
                if (buildWebDir.exists() && buildWebDir.isDirectory()) {
                    File buildFile = new File(buildWebDir, fileName);
                    Files.copy(targetFile.toPath(), buildFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } else {
                System.err.println("[ImageUtil] Could not find source web directory for: " + uploadDirRealPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Lấy URL của ảnh mặc định.
     */
    public static String getDefaultImageUrl() {
        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                return context.getApplication().getResourceHandler().createResource(DEFAULT_IMAGE_NAME, DEFAULT_IMAGE_LIBRARY).getRequestPath();
            }
        } catch (Exception ignored) {
        }
        return "/resources/images/" + DEFAULT_IMAGE_NAME;
    }

    /**
     * Lấy URL ảnh theo tên file và tiền tố thư mục con (device)
     *
     * @param imageUrl tên file ảnh hoặc URL đầy đủ
     * @param prefix tiền tố thư mục con 
     * @return đường dẫn tài nguyên hợp lệ trên JSF
     */
    public static String getImageUrl(String imageUrl, String prefix) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return getDefaultImageUrl();
        }

        String path = imageUrl.trim();
        if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("/")) {
            return path;
        }

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            if (context != null) {
                // chuẩn hóa tiền tố thư mục
                String formattedPrefix = (prefix != null && !prefix.isBlank())
                        ? (prefix.endsWith("/") ? prefix : prefix + "/") : "";

                // tạo tên resource
                String resourceName = (path.equals(DEFAULT_IMAGE_NAME) || (!formattedPrefix.isEmpty()
                        && path.startsWith(formattedPrefix))) ? path : formattedPrefix + path;

                //tạo jsf resource
                var resource = context.getApplication().getResourceHandler().createResource(resourceName, DEFAULT_IMAGE_LIBRARY);
                //kiểm tra resource có tồn tại không
                if (resource != null) {
                    return resource.getRequestPath();
                }
            }

        } catch (Exception ignored) {
        }
        //trả về ảnh mặc định nếu không tìm thấy resource
        return getDefaultImageUrl();
    }

    /**
     * Upload và lưu hình ảnh.
     *
     * @param imagePart file được upload
     * @param subDir thư mục con để lưu ảnh
     * @param errorClientId client id của component hiển thị lỗi
     * @param filePrefix tiền tố của tên file
     * @return tên file mới hoặc null nếu upload thất bại
     */
    public static String processUpload(
            Part imagePart,
            String subDir,
            String errorClientId,
            String filePrefix) {

        //kiểm tra có file được upload hay không
        if (imagePart == null || imagePart.getSize() <= 0) {
            return null;
        }

        //lấy tên file gốc
        String submittedFileName = imagePart.getSubmittedFileName();

        //lấy phần mở rộng của file
        String extension = "";

        if (submittedFileName != null && submittedFileName.lastIndexOf('.') >= 0) {
            extension = submittedFileName.substring(submittedFileName.lastIndexOf('.')).toLowerCase();
        }

        //lấy loại file
        String contentType = imagePart.getContentType();

        //kiểm tra phần mở rộng
        boolean validExt = ALLOWED_EXTENSIONS.contains(extension);

        //kiểm tra mime type
        boolean validType = contentType != null && ALLOWED_MIME_TYPES.contains(contentType.toLowerCase().trim());

        //từ chối nếu file không hợp lệ
        if (!validExt || !validType) {
            FacesUtil.addFieldError(errorClientId, "Incompatible file format. Only JPG, PNG, and WEBP files are allowed.");
            FacesContext.getCurrentInstance().validationFailed();
            return null;
        }

        //kiểm tra kích thước file
        if (imagePart.getSize() > MAX_IMAGE_SIZE) {
            FacesUtil.addFieldError(errorClientId, "Image size must not exceed 5MB.");
            FacesContext.getCurrentInstance().validationFailed();
            return null;
        }

        try {
            //đặt tiền tố mặc định cho tên file
            String prefix = (filePrefix != null && !filePrefix.isBlank()) ? filePrefix : "img_";

            //tạo tên file duy nhất
            String uniqueFileName = prefix + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;

            //xác định thư mục lưu ảnh
            String resourcePath = "/resources/images/" + (subDir != null && !subDir.isBlank() ? subDir : "");

            //lấy đường dẫn thực tế trên server
            String uploadDirRealPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(resourcePath);

            //kiểm tra đường dẫn lưu ảnh
            if (uploadDirRealPath == null) {
                FacesUtil.addFieldError(errorClientId, "Cannot determine server upload directory.");
                FacesContext.getCurrentInstance().validationFailed();
                return null;
            }

            //tạo thư mục nếu chưa tồn tại
            File uploadDir = new File(uploadDirRealPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            //tạo file đích
            File targetFile = new File(uploadDir, uniqueFileName);

            //ghi file upload vào thư mục
            try (InputStream inputStream = imagePart.getInputStream()) {
                Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            //lưu ảnh vào source project
            saveToSourceDirectory(targetFile, uniqueFileName, uploadDirRealPath, subDir);

            //trả về tên file để lưu vào database
            return uniqueFileName;
        } catch (Exception e) {
            //xử lý lỗi khi upload
            e.printStackTrace();
            FacesUtil.addFieldError(errorClientId, "Failed to upload image.");
            FacesContext.getCurrentInstance().validationFailed();
            return null;
        }
    }

    /**
     * Xóa hình ảnh trong thư mục resource.
     *
     * @param fileName tên file cần xóa
     * @param subDir thư mục con chứa file
     */
    public static void deleteImage(String fileName, String subDir) {

        //không xóa ảnh mặc định
        if (fileName == null || fileName.isBlank() || fileName.equals(DEFAULT_IMAGE_NAME)) {
            return;
        }

        try {
            String resourcePath = "/resources/images/" + (subDir != null && !subDir.isBlank() ? subDir : "");

            //lấy đường dẫn thực tế trên server
            String uploadDirRealPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(resourcePath);

            if (uploadDirRealPath == null) {
                return;
            }

            // 1. Xóa file ở thư mục runtime
            File imageFile = new File(uploadDirRealPath, fileName);
            if (imageFile.exists()) {
                Files.delete(imageFile.toPath());
            }

            // 2. Xóa file ở thư mục source code
            File webDir = findSourceWebDirectory(uploadDirRealPath);
            if (webDir != null) {
                File sourceDir = new File(webDir, "resources/images/" + (subDir != null && !subDir.isBlank() ? subDir : ""));
                File sourceFile = new File(sourceDir, fileName);
                if (sourceFile.exists()) {
                    Files.delete(sourceFile.toPath());
                }

                File buildWebDir = new File(webDir.getParentFile(), "build/web/resources/images/" + (subDir != null && !subDir.isBlank() ? subDir : ""));
                File buildFile = new File(buildWebDir, fileName);
                if (buildFile.exists()) {
                    Files.delete(buildFile.toPath());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getImageUrl(String imageUrl) {
        return getImageUrl(imageUrl, null);
    }

    public static String getDeviceImageUrl(String imageUrl) {
        return getImageUrl(imageUrl, DEVICE_IMAGE_PREFIX);
    }
}
