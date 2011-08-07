package com.tomakehurst.springclosuretemplates.web.mvc;

import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;

import com.google.template.soy.SoyFileSet;
import com.google.template.soy.jssrc.SoyJsSrcOptions;

@Controller
public class ClosureTemplateJavascriptController {
	
	@Autowired
	private ClosureTemplateConfig config;
	
	private String cacheControl = "public, max-age=3600";

	@RequestMapping(value="/tmpl/{templateFileName}.js", method=GET)
	public ResponseEntity<String> compileJsForTemplateFile(@PathVariable String templateFileName) {
		
		File templateFile;
		try {
			templateFile = new File(config.getTemplatesLocation().getFile(), templateFileName + ".soy");
		} catch (IOException ioe) {
			throw notFound();
		}
		
		if (!templateFile.exists() || !templateFile.isFile()) {
			throw notFound();
		}
		
		SoyFileSet soyFileSet = (new SoyFileSet.Builder()).add(templateFile).build();
		SoyJsSrcOptions jsSrcOptions = new SoyJsSrcOptions();
		
		List<String> compiledTemplates = soyFileSet.compileToJsSrc(jsSrcOptions, null);
		if (compiledTemplates.size() < 1) {
			throw notFound();
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", "text/javascript");
		headers.add("Cache-Control", cacheControl);
		ResponseEntity<String> response = new ResponseEntity<String>(compiledTemplates.get(0), headers, OK);
		return response;
	}

	private HttpClientErrorException notFound() {
		return new HttpClientErrorException(NOT_FOUND);
	}
	
	public void setConfig(ClosureTemplateConfig config) {
		this.config = config;
	}

	public void setCacheControl(String cacheControl) {
		this.cacheControl = cacheControl;
	}
}