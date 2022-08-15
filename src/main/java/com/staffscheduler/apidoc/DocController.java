package com.staffscheduler.apidoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecuredAnnotationRule;
import io.swagger.v3.oas.annotations.Hidden;

import java.io.IOException;
import java.net.URL;

@Controller("/app/v1/docs")
@Secured(SecuredAnnotationRule.IS_ANONYMOUS)
public class DocController {

    @Get(value = "/", produces = "application/json")
    @Hidden
    public HttpResponse fetchDocs() throws IOException {
        URL docPath = this.getClass().getResource("/META-INF/swagger/swagger.yml");
        String doc = convertYamlToJson(docPath);
        return HttpResponse.ok(doc);
    }

    private String convertYamlToJson(URL yaml) throws IOException {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(yaml, Object.class);
        //Object obj = yamlReader.readValue(yaml, Object.class);

        ObjectMapper jsonWriter = new ObjectMapper();
        return jsonWriter.writeValueAsString(obj);
    }
}
