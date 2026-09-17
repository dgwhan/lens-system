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
 * Utility class xß╗¡ l├╜ ─æ╞░ß╗¥ng dß║½n v├á t├ái nguy├¬n h├¼nh ß║únh trong JSF.
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
     * T├¼m th╞░ mß╗Ñc web trong source project thß║¡t (lens-camera-rental-war/web).
     */
    private static File findSourceWebDirectory(String uploadDirRealPath) {
        if (uploadDirRealPath == null || uploadDirRealPath.isBlank()) {
            return null;
        }

        try {
            File current = new File(uploadDirRealPath);

            while (current != null) {
                // 1. Tr╞░ß╗¥ng hß╗úp dß╗▒ ├ín EAR ─æa module (th╞░ mß╗Ñc root chß╗⌐a lens-camera-rental-war/web)
                File warWeb = new File(current, "lens-camera-rental-war/web");
                if (warWeb.exists() && warWeb.isDirectory()) {
                    return warWeb;
                }

                // 2. Tr╞░ß╗¥ng hß╗úp current ch├¡nh l├á th╞░ mß╗Ñc module lens-camera-rental-war
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
     * L╞░u ß║únh v├áo th╞░ mß╗Ñc source cß╗ºa project.
     *
     * @param targetFile file ß║únh trong runtime
     * @param fileName t├¬n file ß║únh
     * @param uploadDirRealPath ─æ╞░ß╗¥ng dß║½n th╞░ mß╗Ñc runtime
     * @param subDir th╞░ mß╗Ñc con
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

                // ─Éß╗ông thß╗¥i copy v├áo build/web (nß║┐u c├│) ─æß╗â ─æß║úm bß║úo ─æß╗ông bß╗Ö
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
     * Lß║Ñy URL cß╗ºa ß║únh mß║╖c ─æß╗ïnh.
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
     * Lß║Ñy URL ß║únh theo t├¬n file v├á tiß╗ün tß╗æ th╞░ mß╗Ñc con (device)
     *
     * @param imageUrl t├¬n file ß║únh hoß║╖c URL ─æß║ºy ─æß╗º
     * @param prefix tiß╗ün tß╗æ th╞░ mß╗Ñc con 
     * @return ─æ╞░ß╗¥ng dß║½n t├ái nguy├¬n hß╗úp lß╗ç tr├¬n JSF
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
                // chuß║⌐n h├│a tiß╗ün tß╗æ th╞░ mß╗Ñc
                String formattedPrefix = (prefix != null && !prefix.isBlank())
                        ? (prefix.endsWith("/") ? prefix : prefix + "/") : "";

                // tß║ío t├¬n resource
                String resourceName = (path.equals(DEFAULT_IMAGE_NAME) || (!formattedPrefix.isEmpty()
                        && path.startsWith(formattedPrefix))) ? path : formattedPrefix + path;

                //tß║ío jsf resource
                var resource = context.getApplication().getResourceHandler().createResource(resourceName, DEFAULT_IMAGE_LIBRARY);
                //kiß╗âm tra resource c├│ tß╗ôn tß║íi kh├┤ng
                if (resource != null) {
                    return resource.getRequestPath();
                }
            }

        } catch (Exception ignored) {
        }
        //trß║ú vß╗ü ß║únh mß║╖c ─æß╗ïnh nß║┐u kh├┤ng t├¼m thß║Ñy resource
        return getDefaultImageUrl();
    }

    /**
     * Upload v├á l╞░u h├¼nh ß║únh.
     *
     * @param imagePart file ─æ╞░ß╗úc upload
     * @param subDir th╞░ mß╗Ñc con ─æß╗â l╞░u ß║únh
     * @param errorClientId client id cß╗ºa component hiß╗ân thß╗ï lß╗ùi
     * @param filePrefix tiß╗ün tß╗æ cß╗ºa t├¬n file
     * @return t├¬n file mß╗¢i hoß║╖c null nß║┐u upload thß║Ñt bß║íi
     */
    public static String processUpload(
            Part imagePart,
            String subDir,
            String errorClientId,
            String filePrefix) {

        //kiß╗âm tra c├│ file ─æ╞░ß╗úc upload hay kh├┤ng
        if (imagePart == null || imagePart.getSize() <= 0) {
            return null;
        }

        //lß║Ñy t├¬n file gß╗æc
        String submittedFileName = imagePart.getSubmittedFileName();

        //lß║Ñy phß║ºn mß╗ƒ rß╗Öng cß╗ºa file
        String extension = "";

        if (submittedFileName != null && submittedFileName.lastIndexOf('.') >= 0) {
            extension = submittedFileName.substring(submittedFileName.lastIndexOf('.')).toLowerCase();
        }

        //lß║Ñy loß║íi file
        String contentType = imagePart.getContentType();

        //kiß╗âm tra phß║ºn mß╗ƒ rß╗Öng
        boolean validExt = ALLOWED_EXTENSIONS.contains(extension);

        //kiß╗âm tra mime type
        boolean validType = contentType != null && ALLOWED_MIME_TYPES.contains(contentType.toLowerCase().trim());

        //tß╗½ chß╗æi nß║┐u file kh├┤ng hß╗úp lß╗ç
        if (!validExt || !validType) {
            FacesUtil.addFieldError(errorClientId, "Incompatible file format. Only JPG, PNG, and WEBP files are allowed.");
            FacesContext.getCurrentInstance().validationFailed();
            return null;
        }

        //kiß╗âm tra k├¡ch th╞░ß╗¢c file
        if (imagePart.getSize() > MAX_IMAGE_SIZE) {
            FacesUtil.addFieldError(errorClientId, "Image size must not exceed 5MB.");
            FacesContext.getCurrentInstance().validationFailed();
            return null;
        }

        try {
            //─æß║╖t tiß╗ün tß╗æ mß║╖c ─æß╗ïnh cho t├¬n file
            String prefix = (filePrefix != null && !filePrefix.isBlank()) ? filePrefix : "img_";

            //tß║ío t├¬n file duy nhß║Ñt
            String uniqueFileName = prefix + UUID.randomUUID().toString().substring(0, 8) + "_" + System.currentTimeMillis() + extension;

            //x├íc ─æß╗ïnh th╞░ mß╗Ñc l╞░u ß║únh
            String resourcePath = "/resources/images/" + (subDir != null && !subDir.isBlank() ? subDir : "");

            //lß║Ñy ─æ╞░ß╗¥ng dß║½n thß╗▒c tß║┐ tr├¬n server
            String uploadDirRealPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(resourcePath);

            //kiß╗âm tra ─æ╞░ß╗¥ng dß║½n l╞░u ß║únh
            if (uploadDirRealPath == null) {
                FacesUtil.addFieldError(errorClientId, "Cannot determine server upload directory.");
                FacesContext.getCurrentInstance().validationFailed();
                return null;
            }

            //tß║ío th╞░ mß╗Ñc nß║┐u ch╞░a tß╗ôn tß║íi
            File uploadDir = new File(uploadDirRealPath);

            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            //tß║ío file ─æ├¡ch
            File targetFile = new File(uploadDir, uniqueFileName);

            //ghi file upload v├áo th╞░ mß╗Ñc
            try (InputStream inputStream = imagePart.getInputStream()) {
                Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            //l╞░u ß║únh v├áo source project
            saveToSourceDirectory(targetFile, uniqueFileName, uploadDirRealPath, subDir);

            //trß║ú vß╗ü t├¬n file ─æß╗â l╞░u v├áo database
            return uniqueFileName;
        } catch (Exception e) {
            //xß╗¡ l├╜ lß╗ùi khi upload
            e.printStackTrace();
            FacesUtil.addFieldError(errorClientId, "Failed to upload image.");
            FacesContext.getCurrentInstance().validationFailed();
            return null;
        }
    }

    /**
     * X├│a h├¼nh ß║únh trong th╞░ mß╗Ñc resource.
     *
     * @param fileName t├¬n file cß║ºn x├│a
     * @param subDir th╞░ mß╗Ñc con chß╗⌐a file
     */
    public static void deleteImage(String fileName, String subDir) {

        //kh├┤ng x├│a ß║únh mß║╖c ─æß╗ïnh
        if (fileName == null || fileName.isBlank() || fileName.equals(DEFAULT_IMAGE_NAME)) {
            return;
        }

        try {
            String resourcePath = "/resources/images/" + (subDir != null && !subDir.isBlank() ? subDir : "");

            //lß║Ñy ─æ╞░ß╗¥ng dß║½n thß╗▒c tß║┐ tr├¬n server
            String uploadDirRealPath = FacesContext.getCurrentInstance().getExternalContext().getRealPath(resourcePath);

            if (uploadDirRealPath == null) {
                return;
            }

            // 1. X├│a file ß╗ƒ th╞░ mß╗Ñc runtime
            File imageFile = new File(uploadDirRealPath, fileName);
            if (imageFile.exists()) {
                Files.delete(imageFile.toPath());
            }

            // 2. X├│a file ß╗ƒ th╞░ mß╗Ñc source code
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
