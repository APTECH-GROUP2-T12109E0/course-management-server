package com.aptech.coursemanagementserver.services.servicesImpl;

import static com.aptech.coursemanagementserver.constants.GlobalStorage.ACCEPT_RANGES;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.BAD_REQUEST_EXCEPTION;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.BYTES;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.BYTE_RANGE;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.CHUNK_SIZE;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.CONTENT_LENGTH;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.CONTENT_RANGE;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.CONTENT_TYPE;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.VIDEO;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.VIDEO_CONTENT;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.VIDEO_PATH;
import static com.aptech.coursemanagementserver.constants.GlobalStorage.VTT_CONTENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.aptech.coursemanagementserver.dtos.VideoDto;
import com.aptech.coursemanagementserver.dtos.baseDto.BaseDto;
import com.aptech.coursemanagementserver.enums.AntType;
import com.aptech.coursemanagementserver.exceptions.BadRequestException;
import com.aptech.coursemanagementserver.exceptions.IsExistedException;
import com.aptech.coursemanagementserver.models.Lesson;
import com.aptech.coursemanagementserver.models.Video;
import com.aptech.coursemanagementserver.repositories.LessonRepository;
import com.aptech.coursemanagementserver.repositories.VideoRepository;
import com.aptech.coursemanagementserver.services.VideoService;
import com.aptech.coursemanagementserver.services.azureServices.AzureService;
import com.azure.core.util.Context;
import com.azure.storage.blob.models.BlobProperties;
import com.azure.storage.blob.models.BlobRange;
import com.azure.storage.blob.models.DownloadRetryOptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {
    private final VideoRepository videoRepository;
    private final LessonRepository lessonRepository;
    private final AzureService azureService;

    @Override
    public Video findVideoByName(String videoName) {
        return videoRepository.findVideoByName(videoName);
    }

    @Override
    public List<Video> findAll() {
        return videoRepository.findAll();
    }

    @Override
    public VideoDto findByLessonId(long lessonId) {
        try {
            Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(
                    () -> new NoSuchElementException("The lesson with lessonId: [" + lessonId + "] is not exist."));
            Video video = lesson.getVideo();

            String[] parts = video.getCaptionUrls().split(",");
            Map<String, String> map = new HashMap<>();
            for (String part : parts) {
                int lastDotIndex = part.lastIndexOf(".");
                if (lastDotIndex > 0) {
                    String langCode = part.substring(lastDotIndex - 2, lastDotIndex);
                    map.put(langCode, part);
                }
            }
            // ObjectMapper objectMapper = new ObjectMapper();
            // String captionData = objectMapper.writeValueAsString(map);

            VideoDto videoDto = VideoDto.builder().id(video.getId()).name(video.getName()).url(video.getUrl())
                    .status(video.getStatus())
                    .captionData(map).lessonId(lessonId).build();

            return videoDto;
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }

    }

    @Override
    public BaseDto save(VideoDto videoDto, long lessonId) {
        try {
            Lesson lesson = lessonRepository.findById(lessonId).orElseThrow(
                    () -> new NoSuchElementException("The lesson with id: [" + lessonId + "]does not exist."));

            if (findVideoByName(videoDto.getName()) != null) {
                throw new IsExistedException(videoDto.getName());
            }

            Video videoOfLesson = lesson.getVideo();

            if (videoOfLesson != null) {
                Files.deleteIfExists(
                        VIDEO_PATH.resolve(videoOfLesson.getName()));
            }

            Video video = videoOfLesson != null ? videoOfLesson : new Video();
            video.setName(videoDto.getName())
                    .setUrl(videoDto.getUrl())
                    .setMobileUrl(videoDto.getMobileUrl())
                    .setStatus(videoDto.getStatus())
                    .setCaptionUrls(String.join(",", videoDto.getCaptionUrls()));

            if (videoOfLesson == null) {
                video.setLesson(lesson);
            }

            videoRepository.save(video);

            return BaseDto.builder().type(AntType.success).message("Create video successfully.")
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());
        } catch (IsExistedException e) {
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }

    }

    @Override
    public List<Video> saveAll(List<VideoDto> videosDto) {
        List<Video> videos = videosDto.stream().map(videoDto -> findVideoByName(videoDto.getName()))
                .collect(Collectors.toList());
        return videoRepository.saveAll(videos);
    }

    @Override
    public BaseDto delete(long videoId) {
        try {
            Video video = videoRepository.findById(videoId).orElseThrow(
                    () -> new NoSuchElementException("The video with videoId: [" + videoId + "] is not exist."));
            videoRepository.delete(video);

            Files.deleteIfExists(
                    VIDEO_PATH.resolve(video.getName()));

            return BaseDto.builder().type(AntType.success).message("Delete video successfully.")
                    .build();
        } catch (NoSuchElementException e) {
            throw new NoSuchElementException(e.getMessage());

        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public BaseDto update(VideoDto videoDto) {
        try {
            Video video = videoRepository.findById(videoDto.getId()).get();
            video.setUrl(videoDto.getUrl())
                    .setName(videoDto.getName())
                    .setStatus(videoDto.getStatus())
                    .setCaptionUrls(String.join(",", videoDto.getCaptionUrls()));

            videoRepository.save(video);

            return BaseDto.builder().type(AntType.success).message("Update video successfully.")
                    .build();
        } catch (NoSuchElementException e) {
            throw new BadRequestException("The video with videoId: [" + videoDto.getId() + "] is not exist.");

        } catch (Exception e) {
            throw new BadRequestException(BAD_REQUEST_EXCEPTION);
        }
    }

    @Override
    public ResponseEntity<byte[]> prepareVideoContent(String fileName, String fileType, String range) {
        try {
            final String fileKey = fileName + "." + fileType;
            long rangeStart = 0;
            long rangeEnd = CHUNK_SIZE;
            final Long fileSize = getVideoSize(fileKey);
            if (range == null) {
                return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                        .header(CONTENT_TYPE, VIDEO_CONTENT + fileType)
                        .header(ACCEPT_RANGES, BYTES)
                        .header(CONTENT_LENGTH, String.valueOf(rangeEnd))
                        .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                        .header(CONTENT_LENGTH, String.valueOf(fileSize))
                        .body(readByteRangeNew(fileKey, rangeStart, rangeEnd)); // Read the object and convert it as
                                                                                // bytes
            }
            String[] ranges = range.split("-");
            rangeStart = Long.parseLong(ranges[0].substring(6));
            if (ranges.length > 1) {
                rangeEnd = Long.parseLong(ranges[1]);
            } else {
                rangeEnd = rangeStart + CHUNK_SIZE;
            }

            rangeEnd = Math.min(rangeEnd, fileSize - 1);
            final byte[] data = readByteRangeNew(fileKey, rangeStart, rangeEnd);
            final String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
            HttpStatus httpStatus = HttpStatus.PARTIAL_CONTENT;
            if (rangeEnd >= fileSize) {
                httpStatus = HttpStatus.OK;
            }
            return ResponseEntity.status(httpStatus)
                    .header(CONTENT_TYPE, VIDEO_CONTENT + fileType)
                    .header(ACCEPT_RANGES, BYTES)
                    .header(CONTENT_LENGTH, contentLength)
                    .header(CONTENT_RANGE, BYTES + " " + rangeStart + "-" + rangeEnd + "/" + fileSize)
                    .body(data);
        } catch (IOException e) {
            log.error("Exception while reading the file {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<byte[]> prepareCaptionContent(final String fileName) {
        try {
            // Path path = Paths.get(CAPTION, fileName);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();

            azureService.getBlobCaptions(fileName).downloadStream(bytes);

            return ResponseEntity.status(HttpStatus.OK)
                    .contentType(MediaType.parseMediaType(VTT_CONTENT))
                    .body(bytes.toByteArray());
        } catch (Exception e) {
            log.error("Exception while reading the file {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

    }

    public byte[] readByteRangeNew(String filename, long start, long end) throws IOException {
        Path path = Paths.get(VIDEO, filename);
        byte[] data = Files.readAllBytes(path);
        byte[] result = new byte[(int) (end - start) + 1];
        System.arraycopy(data, (int) start, result, 0, (int) (end - start) + 1);
        return result;
    }

    public byte[] readByteRange(String filename, long start, long end) throws IOException {
        Path path = Paths.get(VIDEO, filename);
        try (InputStream inputStream = (Files.newInputStream(path));
                ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[BYTE_RANGE];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                bufferedOutputStream.write(data, 0, nRead);
            }
            bufferedOutputStream.flush();
            byte[] result = new byte[(int) (end - start) + 1];
            // System.arraycopy(bufferedOutputStream.toByteArray(), (int) start, result, 0,
            // result.length);
            return result;
        }
    }

    // private String getVideoPath() {
    // URL url = this.getClass().getClassLoader().getResource(VIDEO);
    // assert url != null;
    // return new File(url.getFile()).getAbsolutePath();
    // }

    // private String getCaptionPath() {
    // URL url = this.getClass().getClassLoader().getResource(CAPTION);
    // assert url != null;
    // return new File(url.getFile()).getAbsolutePath();
    // }

    /*
     * public Long getVideoSize(String fileName) {
     * return Optional.ofNullable(fileName)
     * .map(file -> Paths.get(getVideoPath(), file))
     * .map(this::sizeFromFile)
     * .orElse(0L);
     * }
     */
    public Long getVideoSize(String fileName) throws IOException {

        return Files.size((Paths.get(VIDEO, fileName)));
        // return Optional.ofNullable(fileName)
        // .map(file -> Paths.get(VIDEO, file))
        // .map(this::sizeFromFile)
        // .orElse(0L);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> loadPartialMediaFile(String localMediaFilePath, String rangeValues)
            throws IOException {
        if (!StringUtils.hasText(rangeValues)) {

            return loadEntireMediaFile(localMediaFilePath);
        } else {
            long rangeStart = 0L;
            long rangeEnd = 0L;

            // if (!StringUtils.hasText(localMediaFilePath)) {
            // throw new IllegalArgumentException("The full path to the media file is NULL
            // or empty.");
            // }

            // Path filePath = Paths.get(localMediaFilePath);
            // if (!filePath.toFile().exists()) {
            // throw new FileNotFoundException("The media file does not exist.");
            // }

            long fileSize = azureService.getBlob(localMediaFilePath).getProperties().getBlobSize();
            int dashPos = rangeValues.indexOf("-");
            if (dashPos > 0 && dashPos <= (rangeValues.length() - 1)) {
                String[] rangesArr = rangeValues.split("-");

                if (rangesArr != null && rangesArr.length > 0) {
                    // System.out.println("ArraySize: " + rangesArr.length);
                    if (StringUtils.hasText(rangesArr[0])) {
                        // System.out.println("Rang values[0]: [" + rangesArr[0] + "]");
                        String valToParse = numericStringValue(rangesArr[0]);
                        rangeStart = safeParseStringValuetoLong(valToParse, 0L);
                    } else {
                        rangeStart = 0L;
                    }

                    if (rangesArr.length > 1) {
                        // System.out.println("Rang values[1]: [" + rangesArr[1] + "]");
                        String valToParse = numericStringValue(rangesArr[1]);
                        rangeEnd = safeParseStringValuetoLong(valToParse, 0L);
                    } else {
                        if (fileSize > 0) {
                            rangeEnd = fileSize - 1L;
                        } else {
                            rangeEnd = 0L;
                        }
                    }
                }
            }

            if (rangeEnd == 0L && fileSize > 0L) {
                rangeEnd = fileSize - 1;
            }
            if (fileSize < rangeEnd) {
                rangeEnd = fileSize - 1;
            }

            // System.out.println(String.format("Parsed Range Values: [%d] - [%d]",
            // rangeStart, rangeEnd));

            return loadPartialMediaFile(localMediaFilePath, rangeStart, rangeEnd);
        }

    }

    @Override
    public ResponseEntity<StreamingResponseBody> loadPartialMediaFile(String localMediaFilePath, long fileStartPos,
            long fileEndPos) throws IOException {
        StreamingResponseBody responseStream;
        // Path filePath = Paths.get(localMediaFilePath);
        // if (!filePath.toFile().exists()) {
        // throw new FileNotFoundException("The media file does not exist.");
        // }
        BlobProperties properties = azureService.getBlob(localMediaFilePath).getProperties();
        long fileSize = properties.getBlobSize();
        if (fileStartPos < 0L) {
            fileStartPos = 0L;
        }

        if (fileSize > 0L) {
            if (fileStartPos >= fileSize) {
                fileStartPos = fileSize - 1L;
            }

            if (fileEndPos >= fileSize) {
                fileEndPos = fileSize - 1L;
            }
        } else {
            fileStartPos = 0L;
            fileEndPos = 0L;
        }

        String mimeType = properties.getContentType();

        final HttpHeaders responseHeaders = new HttpHeaders();
        String contentLength = String.valueOf((fileEndPos - fileStartPos) + 1);
        responseHeaders.add("Content-Type", mimeType);
        responseHeaders.add("Content-Length", contentLength);
        responseHeaders.add("Accept-Ranges", "bytes");
        responseHeaders.add("Content-Range", String.format("bytes %d-%d/%d", fileStartPos, fileEndPos, fileSize));

        final long fileStartPos2 = fileStartPos;
        final long fileEndPos2 = fileEndPos;

        responseStream = os -> {
            // RandomAccessFile file = new RandomAccessFile(localMediaFilePath, "r");
            // try (file) {
            // long pos = fileStartPos2;
            // file.seek(pos);
            // while (pos < fileEndPos2) {
            // file.read(buffer);
            // os.write(buffer);
            // pos += buffer.length;
            // }
            try {

                BlobRange blobRange = new BlobRange(fileStartPos2, fileEndPos2);
                azureService.getBlob(localMediaFilePath).downloadStreamWithResponse(os, blobRange,
                        new DownloadRetryOptions().setMaxRetryRequests(5),
                        null, false, null, Context.NONE);
                azureService.downloadBlobFromAzure(os, localMediaFilePath);
                // os.flush();
            }

            catch (Exception e) {
            }
        };

        return new ResponseEntity<StreamingResponseBody>(responseStream, responseHeaders, HttpStatus.PARTIAL_CONTENT);

    }

    @Override
    public ResponseEntity<StreamingResponseBody> loadEntireMediaFile(String localMediaFilePath) throws IOException {
        // Path filePath = Paths.get(localMediaFilePath);
        // if (!filePath.toFile().exists()) {
        // throw new FileNotFoundException("The media file does not exist.");
        // }

        long fileSize = azureService.getBlob(localMediaFilePath).getProperties().getBlobSize();
        long endPos = fileSize;
        if (fileSize > 0L) {
            endPos = fileSize - 1;
        } else {
            endPos = 0L;
        }

        ResponseEntity<StreamingResponseBody> retVal = loadPartialMediaFile(localMediaFilePath, 0, endPos);

        return retVal;
    }

    private long safeParseStringValuetoLong(String valToParse, long defaultVal) {
        long retVal = defaultVal;
        if (StringUtils.hasText(valToParse)) {
            try {
                retVal = Long.parseLong(valToParse);
            } catch (NumberFormatException ex) {

                retVal = defaultVal;
            }
        }

        return retVal;
    }

    private String numericStringValue(String origVal) {
        String retVal = "";
        if (StringUtils.hasText(origVal)) {
            retVal = origVal.replaceAll("[^0-9]", "");

        }

        return retVal;
    }

    // private Long sizeFromFile(Path path) {
    // try {
    // return Files.size(path);
    // } catch (IOException e) {
    // log.error("Error while getting the file size: {}", e.getMessage());
    // }
    // return 0L;
    // }
}