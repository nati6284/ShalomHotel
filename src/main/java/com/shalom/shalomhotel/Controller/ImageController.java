//package com.shalom.shalomhotel.Controller;
//
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.net.MalformedURLException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@RestController
//public class ImageController {
//
//    private final String uploadDir = "C:/Users/user/uploads";  // Same as your upload folder
//
//    @GetMapping("/images/{filename:.+}")
//    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
//        try {
//            Path file = Paths.get(uploadDir).resolve(filename).normalize();
//            Resource resource = new UrlResource(file.toUri());
//            if(resource.exists() && resource.isReadable()) {
//                return ResponseEntity.ok().body(resource);
//            } else {
//                return ResponseEntity.notFound().build();
//            }
//        } catch (MalformedURLException e) {
//            return ResponseEntity.badRequest().build();
//        }
//    }
//}
//
