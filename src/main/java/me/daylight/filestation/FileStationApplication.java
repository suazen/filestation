package me.daylight.filestation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.ErrorPage;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.HttpStatus;

@Configuration
@EnableJpaAuditing
@SpringBootApplication
public class FileStationApplication {

	@Bean
	public WebServerFactoryCustomizer<ConfigurableWebServerFactory> containerCustomizer() {

		return container -> {
			ErrorPage error401Page = new ErrorPage(HttpStatus.UNAUTHORIZED, "/errorPage");
			ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404Page");
			ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/errorPage");
			ErrorPage error400Page = new ErrorPage(HttpStatus.BAD_REQUEST,"/errorPage");

			container.addErrorPages(error401Page, error404Page, error500Page,error400Page);
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(FileStationApplication.class, args);
	}

}

