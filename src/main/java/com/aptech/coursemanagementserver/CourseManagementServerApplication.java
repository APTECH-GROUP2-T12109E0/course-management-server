package com.aptech.coursemanagementserver;

import static com.aptech.coursemanagementserver.enums.Role.ADMIN;
import static com.aptech.coursemanagementserver.enums.Role.EMPLOYEE;
import static com.aptech.coursemanagementserver.enums.Role.MANAGER;
import static com.aptech.coursemanagementserver.enums.Role.USER;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import com.aptech.coursemanagementserver.configs.ApplicationProperties;
import com.aptech.coursemanagementserver.dtos.AuthorDto;
import com.aptech.coursemanagementserver.dtos.CategoryDto;
import com.aptech.coursemanagementserver.dtos.PostDto;
import com.aptech.coursemanagementserver.dtos.RegisterRequestDto;
import com.aptech.coursemanagementserver.enums.CommentType;
import com.aptech.coursemanagementserver.models.Permissions;
import com.aptech.coursemanagementserver.models.Roles;
import com.aptech.coursemanagementserver.models.User;
import com.aptech.coursemanagementserver.services.AuthorService;
import com.aptech.coursemanagementserver.services.CategoryService;
import com.aptech.coursemanagementserver.services.CourseService;
import com.aptech.coursemanagementserver.services.PostService;
import com.aptech.coursemanagementserver.services.authServices.AuthenticationService;
import com.aptech.coursemanagementserver.services.authServices.UserPermissionService;
import com.aptech.coursemanagementserver.utils.CommonUtils;

@SpringBootApplication
@EnableWebSecurity
@EnableConfigurationProperties(ApplicationProperties.class)
public class CourseManagementServerApplication {

	@Value("${spring.jpa.hibernate.ddl-auto}")
	String seedData;

	public static void main(String[] args) {
		SpringApplication.run(CourseManagementServerApplication.class, args);
	}

	// Define @Bean to tell SpringBoot it should create an instance of the class and
	// register it with the application context. When the application starts up,
	// Spring Boot will call the run() method on each CommandLineRunner Bean in the
	// order in which they were defined.
	@Bean
	CommandLineRunner commandLineRunner(
			AuthenticationService service,
			CategoryService categoryService,
			AuthorService authorService,
			CourseService courseService,
			PostService postService,
			UserPermissionService userPermissionService) {
		return args -> {

			if (seedData.equalsIgnoreCase("create")) {
				// =============== ROLE & PERMISSION ===============
				var roleAdmin = Roles.builder().name(ADMIN).build();
				var roleManager = Roles.builder().name(MANAGER).build();
				var roleEmployee = Roles.builder().name(EMPLOYEE).build();
				var roleUser = Roles.builder().name(USER).build();

				userPermissionService.saveRole(roleAdmin);
				userPermissionService.saveRole(roleManager);
				userPermissionService.saveRole(roleEmployee);
				userPermissionService.saveRole(roleUser);

				var permissionAdmin = Permissions.builder()
						.permission("ADMIN").role(roleAdmin).build();
				var permissionManager = Permissions.builder()
						.permission("MANAGER").role(roleManager).build();
				var permissionEmployee = Permissions.builder()
						.permission("EMPLOYEE").role(roleEmployee).build();
				var permissionCourse = Permissions.builder()
						.permission("EMP_COURSE").role(roleEmployee).build();
				var permissionBlog = Permissions.builder()
						.permission("EMP_BLOG").role(roleEmployee).build();
				var permissionExam = Permissions.builder()
						.permission("EMP_EXAM").role(roleEmployee).build();
				var permissionUser = Permissions.builder()
						.permission("USER").role(roleUser).build();

				userPermissionService.savePermission(permissionAdmin);
				userPermissionService.savePermission(permissionManager);
				userPermissionService.savePermission(permissionEmployee);
				userPermissionService.savePermission(permissionCourse);
				userPermissionService.savePermission(permissionBlog);
				userPermissionService.savePermission(permissionExam);
				userPermissionService.savePermission(permissionUser);
				// =============== ADMIN ===============
				var admin = RegisterRequestDto.builder()
						.first_name("ClicknLearn")
						.last_name("")
						.email("admin@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/2KnQrF0/logo-click-thumb-light.png")
						.role(ADMIN)
						.isVerified(true)
						.build();

				User adminAcc = service.register(admin);

				System.out.println(
						"Admin token: " +
								service.generateTokenWithoutVerify(adminAcc).getAccessToken());

				// =============== MANAGER ===============
				var manager1 = RegisterRequestDto.builder()
						.first_name("Manager1")
						.last_name("")
						.email("manager1@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/2KnQrF0/logo-click-thumb-light.png")
						.role(MANAGER)
						.isVerified(true)

						.build();

				User managerAcc1 = service.register(manager1);

				System.out.println(
						"Manager1  token: " +
								service.generateTokenWithoutVerify(managerAcc1).getAccessToken());

				// =============== EMPLOYEE ===============
				var employee1 = RegisterRequestDto.builder()
						.first_name("Employee1")
						.last_name("")
						.email("employee1@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/2KnQrF0/logo-click-thumb-light.png")
						.role(EMPLOYEE)
						.isVerified(true)

						.build();

				User employeeAcc1 = service.register(employee1);
				userPermissionService.saveUserPermission(permissionCourse, employeeAcc1);

				System.out.println("Employee1 token: "
						+
						service.generateTokenWithoutVerify(employeeAcc1).getAccessToken());

				// =============== USER ===============
				var userTestDto = RegisterRequestDto.builder()
						.first_name("TeeCee")
						.last_name("")
						.email("user-test@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/7GDTVRf/aptech.png")
						// .role(USER)
						.isVerified(true)

						.build();
				User userTest = service.register(userTestDto);
				System.out.println(
						"User1 token: " +
								service.generateTokenWithoutVerify(userTest).getAccessToken());
				var userTest2Dto = RegisterRequestDto.builder()
						.first_name("Ric")
						.last_name("")
						.email("user-test2@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/7GDTVRf/aptech.png")
						// .role(USER)
						.isVerified(true)

						.build();
				User userTest2 = service.register(userTest2Dto);
				System.out.println(
						"User2 token: " +
								service.generateTokenWithoutVerify(userTest2).getAccessToken());
				var userTest3Dto = RegisterRequestDto.builder()
						.first_name("AnPham")
						.last_name("")
						.email("user-test3@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/7GDTVRf/aptech.png")
						// .role(USER)
						.isVerified(true)

						.build();
				User userTest3 = service.register(userTest3Dto);
				System.out.println(
						"User3 token: " +
								service.generateTokenWithoutVerify(userTest3).getAccessToken());
				var userTest4Dto = RegisterRequestDto.builder()
						.first_name("DiDi")
						.last_name("")
						.email("user-test4@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/7GDTVRf/aptech.png")
						// .role(USER)
						.isVerified(true)

						.build();
				User userTest4 = service.register(userTest4Dto);
				System.out.println(
						"User4 token: " +
								service.generateTokenWithoutVerify(userTest4).getAccessToken());
				var userTest5Dto = RegisterRequestDto.builder()
						.first_name("DucTH")
						.last_name("")
						.email("user-test5@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/7GDTVRf/aptech.png")
						// .role(USER)
						.isVerified(true)
						.build();
				User userTest5 = service.register(userTest5Dto);
				System.out.println(
						"User5 token: " +
								service.generateTokenWithoutVerify(userTest5).getAccessToken());

				// =============== POST ===============
				postService.create(
						PostDto.builder().content("First post, Haha!").userId(userTest.getId()).typeId(1)
								.type(CommentType.COURSE)
								.build());
				postService.create(PostDto.builder()
						.content("Post2 Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
						.userId(userTest2.getId()).typeId(1).type(CommentType.COURSE)
						.build());
				postService.create(PostDto.builder()
						.content("Post3 Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
						.userId(userTest3.getId()).typeId(1).type(CommentType.COURSE)
						.build());

				// =============== MORE USER ===============
				for (int index = 6; index < 88; index++) {
					var userTestsDto = RegisterRequestDto.builder()
							.first_name(CommonUtils.randomFirstName())
							.last_name(CommonUtils.randomLastName())
							.email("user-test" + index + "@mail.com")
							.password("password")
							.imageUrl("https://i.ibb.co/7GDTVRf/aptech.png")
							// .role(USER)
							.isVerified(true)
							.build();
					User userTests = service.register(userTestsDto);
					System.out.println(
							"User" + index + "token: " +
									service.generateTokenWithoutVerify(userTests).getAccessToken());
				}

				// =============== MORE EMPLOYEE ===============
				var employee2 = RegisterRequestDto.builder()
						.first_name("Employee2")
						.last_name("")
						.email("employee2@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/2KnQrF0/logo-click-thumb-light.png")
						.role(EMPLOYEE)
						.isVerified(true)

						.build();

				User employeeAcc2 = service.register(employee2);

				userPermissionService.saveUserPermission(permissionExam, employeeAcc2);

				System.out.println("Employee2 token: "
						+
						service.generateTokenWithoutVerify(employeeAcc2).getAccessToken());

				var employee3 = RegisterRequestDto.builder()
						.first_name("Employee3")
						.last_name("")
						.email("employee3@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/2KnQrF0/logo-click-thumb-light.png")
						.role(EMPLOYEE)
						.isVerified(true)

						.build();

				User employeeAcc3 = service.register(employee3);

				userPermissionService.saveUserPermission(permissionBlog, employeeAcc3);

				System.out.println("Employee3 token: "
						+
						service.generateTokenWithoutVerify(employeeAcc3).getAccessToken());

				// =============== MORE MANAGER ===============
				var manager2 = RegisterRequestDto.builder()
						.first_name("Manager2")
						.last_name("")
						.email("manager2@mail.com")
						.password("password")
						.imageUrl("https://i.ibb.co/2KnQrF0/logo-click-thumb-light.png")
						.role(MANAGER)
						.isVerified(true)

						.build();

				User managerAcc2 = service.register(manager2);

				System.out.println(
						"Manager2  token: " +
								service.generateTokenWithoutVerify(managerAcc2).getAccessToken());

				// =============== CATEGORY ===============
				List<CategoryDto> categoryDtos = new ArrayList<>();
				CategoryDto category1 = CategoryDto.builder()
						.name("Programming")
						.image("https://www.theschoolrun.com/sites/theschoolrun.com/files/article_images/what_is_a_programming_language.jpg")
						.description(
								"Explore the world of programming and learn how to create software, websites, and applications. Gain hands-on experience with popular programming languages and tools, and build a foundation for a successful career in technology.")
						.build();
				CategoryDto category2 = CategoryDto.builder()
						.name("Graphic Design")
						.image("https://images.unsplash.com/photo-1626785774573-4b799315345d?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxzZWFyY2h8NXx8R3JhcGhpYyUyMERlc2lnbnxlbnwwfHwwfHx8MA%3D%3D&auto=format&fit=crop&w=500&q=60")
						.description(
								"Unleash your creativity with graphic design. Learn how to create visually appealing designs, manipulate images, and develop skills using popular design software. Discover the art of communication through visual elements.")
						.build();
				CategoryDto category3 = CategoryDto.builder()
						.name("Artificial Intelligence")
						.image("https://media.istockphoto.com/id/1440356809/photo/artificial-intelligence-technology-robot-futuristic-data-science-data-analytics-quantum.webp?b=1&s=170667a&w=0&k=20&c=wXYn8o0Y5OYTZbRFTeXvyQ2V4dt8HMHPLgFSJxjqWcg=")
						.description(
								"Dive into the fascinating field of Artificial Intelligence (AI). Learn about machine learning, neural networks, and data analysis algorithms. Explore how AI is transforming various industries and gain insights into the future of intelligent systems.")
						.build();
				CategoryDto category4 = CategoryDto.builder()
						.name("Data Science")
						.image("https://media.istockphoto.com/id/1405263192/vi/anh/kh%C3%A1i-ni%E1%BB%87m-khoa-h%E1%BB%8Dc-d%E1%BB%AF-li%E1%BB%87u.jpg?s=2048x2048&w=is&k=20&c=U5JcK90r1rbbLMuQm9G8e3BvFerS4fSLbS4BRyQYZd4=")
						.description(
								"Unlock the power of data through data science. Discover techniques for analyzing and interpreting data, and gain insights to drive informed decision-making. Learn how to extract meaningful information from complex datasets and solve real-world problems.")
						.build();

				categoryDtos.add(category1);
				categoryDtos.add(category2);
				categoryDtos.add(category3);
				categoryDtos.add(category4);

				categoryService.saveAll(categoryDtos);

				List<AuthorDto> authorDtos = new ArrayList<>();
				AuthorDto author1 = AuthorDto.builder()
						.name("Stacia M. V.")
						.image("https://i.ibb.co/PZ1mLcR/1ccad4bd825948071148.jpg")
						.title("Microsoft Data Platform MVP and ClicknLearn Author")
						.information(
								"Stacia M. V. is an instructor, author, and principal consultant of Data Inspirations. Her career spans more than 30 years, with a focus on improving business practices through technology. Since 2000, she has provided consulting and education services for Microsoft\u2019s data platform and authored or co-authored many books covering this topic. In addition, Stacia has been a frequent speaker over the years at technology conferences worldwide.")
						.build();
				AuthorDto author2 = AuthorDto.builder()
						.name("Marques W.")
						.image("https://i.ibb.co/ZKk06Ds/member-lucia.jpg")
						.title("Founder of Chicago Software Geeks and ClicknLearn Author")
						.information(
								"Marques W. has been involved with software development for years, specializing in Javascript application architecture, hybrid mobile application development, and Node.js applications. As a family man living in Chicago, he's had the chance to work with large enterprises doing legacy code optimization and refactoring, and startups building from the ground up. I'm passionate about experimenting with Javascript frameworks and libraries and figuring out what would work best for my current team/project. He also really enjoys teaching and mentoring new developers.")
						.build();
				AuthorDto author3 = AuthorDto.builder()
						.name("David L.")
						.image("https://i.ibb.co/3THGf9j/member-braum.jpg")
						.title("Creator of SFDC99.com, Salesforce MVP and ClicknLearn Author")
						.information(
								"David L. is a self-taught programmer whose dreams came true when he became a Salesforce Technical Architect at Google. Now, David dreams of inspiring the next generation of Salesforce developers. He's a four-time Salesforce MVP winner and runs SFDC99.com, the world's most popular Salesforce blog. David has been a close partner with Salesforce throughout his career, often speaking and developing content for Dreamforce, Trailhead, webinars, user groups, and the official Salesforce developer website.")
						.build();
				AuthorDto author4 = AuthorDto.builder()
						.name("Matias C.")
						.image("https://i.ibb.co/GTGRmZp/member-nana.jpg")
						.title("Co-founder and chief of design at Behance and ClicknLearn Author")
						.information(
								"Matias C. - There\u2019s a lot to do online if you are trying to get into the dating scene. Take your pick\u2009\u2014\u2009there\u2019s so many different niche\u2019s of people you can find online. But what about the folks who aren\u2019t looking for their significant other, but want to keep the one they have happy? There\u2019s not a ton of that out there, but Christina Brodbeck (former UI designer at YouTube) decided she wanted to change this. TheIceBreak is an app (and soon a website) that creates a space for couples to interact with each other. They can answer questions, send pictures and even suggest things to do with each other. It\u2019s a fresh, new approach to keeping connected and communicating with your honey.")
						.build();

				authorDtos.add(author1);
				authorDtos.add(author2);
				authorDtos.add(author3);
				authorDtos.add(author4);

				authorService.saveAll(authorDtos);

				// Category savedCategory1 = categoryService.findById(1);
				// CourseDto course1 = CourseDto.builder().achievementName("Master Java,Master
				// SpringBoot")
				// .image("https://i.ibb.co/0jCVHrQ/spring-boot.png")
				// .duration(300)
				// .description("Description").price(15).net_price(10)
				// .sections(Arrays.asList("Section 1", "Section 2", "Section 3"))
				// .tagName("java,spring boot,hibernate").name("Java SpringBoot 2023")
				// .rating(0)
				// .category(savedCategory1.getId())
				// .build();
				// courseService.save(course1);
			}
		};
	}

	@Override
	public String toString() {
		return "CourseManagementServerApplication []";
	}
}
