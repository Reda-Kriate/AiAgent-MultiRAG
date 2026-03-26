package net.reda.transactionservice.web;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
public class ImgController {

    @GetMapping(value = "/images", produces = MediaType.IMAGE_PNG_VALUE)
    public byte[] getImage(@RequestParam String path) throws IOException {
        Path imagePath = Paths.get(path);
        return Files.readAllBytes(imagePath);
    }
}
