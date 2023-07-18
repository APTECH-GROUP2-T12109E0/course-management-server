INSERT INTO section ([created_at], [name], [ordered], [status], [updated_at], [course_id])
SELECT GETUTCDATE(), 'Section 1', 1, 1, GETUTCDATE(), course.id
FROM course
WHERE course.id BETWEEN 8 AND 21
UNION ALL
SELECT GETUTCDATE(), 'Section 2', 2, 1, GETUTCDATE(), course.id
FROM course
WHERE course.id BETWEEN 8 AND 21

INSERT INTO lesson ([created_at], [description], [duration], [name], [ordered], [status], [updated_at], [section_id])
SELECT GETUTCDATE(), '<p>Lesson 1 Description</p>', 13, 'Lesson 1', 1, 1, GETUTCDATE(), section.id
FROM section
WHERE section.id BETWEEN 8 AND 34
UNION ALL
SELECT GETUTCDATE(), '<p>Lesson 2 Description</p>', 15, 'Lesson 2', 2, 1, GETUTCDATE(), section.id
FROM section
WHERE section.id BETWEEN 8 AND 34

INSERT [dbo].[video] ([lesson_id], [caption_urls], [created_at], [duration], [name], [status], [updated_at], [url])
SELECT l.id , N'http://localhost:8080/video/caption/sample.en.vtt,http://localhost:8080/video/caption/sample.vi.vtt,http://localhost:8080/video/caption/sample.jp.vtt',
 CAST(N'2023-06-12T15:36:43.8405520+00:00' AS DateTimeOffset), 0, N'230612223643_course_1.mp4', 1, CAST(N'2023-06-12T15:36:43.8405520+00:00' AS DateTimeOffset), N'http://localhost:8080/video/stream/mp4/230612223643_course_1' FROM lesson l
WHERE l.id BETWEEN 9 AND 63

DECLARE @i INT = 9;

WHILE @i <= 63
BEGIN
    UPDATE video
	SET url = CONCAT('http://localhost:8080/video/stream/mp4/', 230612223800 + @i, '_course_1'),
	name = CONCAT(230612223800 + @i, '_course_1.mp4')
	WHERE lesson_id = @i AND @i <= 63;
    
    SET @i = @i + 1; -- Increment the value of @i
END

