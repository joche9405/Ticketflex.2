package com.tu_paquete.ticketflex.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import com.mongodb.client.gridfs.model.GridFSFile;
import java.io.IOException;
import org.bson.types.ObjectId;

@RestController
@RequestMapping("/api")
public class ImagenController {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private GridFsOperations gridFsOperations;

    @GetMapping("/imagen/{id}")
    public ResponseEntity<InputStreamResource> obtenerImagen(@PathVariable String id) {
        try {
            // 1. Manejar imagen por defecto
            if ("default.jpg".equals(id)) {
                try {
                    ClassPathResource defaultImage = new ClassPathResource("static/images/default.jpg");
                    return ResponseEntity.ok()
                            .contentType(MediaType.IMAGE_JPEG)
                            .body(new InputStreamResource(defaultImage.getInputStream()));
                } catch (IOException e) {
                    return ResponseEntity.notFound().build();
                }
            }

            // 2. Convertir ID a ObjectId si es posible
            ObjectId objectId;
            try {
                objectId = new ObjectId(id);
            } catch (IllegalArgumentException e) {
                // Si no es un ObjectId válido, intentar buscar por nombre de archivo
                GridFSFile archivo = gridFsTemplate.findOne(
                        Query.query(Criteria.where("filename").is(id)));
                {
                    GridFsResource recurso = gridFsOperations.getResource(archivo);
                    return ResponseEntity.ok()
                            .contentType(MediaType.parseMediaType(
                                    recurso.getContentType() != null ? recurso.getContentType() : "image/jpeg"))
                            .body(new InputStreamResource(recurso.getInputStream()));
                }
            }

            // 3. Buscar en GridFS por ObjectId
            GridFSFile archivo = gridFsTemplate.findOne(
                    Query.query(Criteria.where("_id").is(objectId)));

            // 4. Obtener y devolver el recurso
            GridFsResource recurso = gridFsOperations.getResource(archivo);
            String contentType = recurso.getContentType() != null ? recurso.getContentType() : "image/jpeg";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(new InputStreamResource(recurso.getInputStream()));

        } catch (Exception e) {
            e.printStackTrace();
            // 5. En caso de error, intentar devolver la imagen por defecto
            try {
                ClassPathResource defaultImage = new ClassPathResource("static/images/default.jpg");
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(new InputStreamResource(defaultImage.getInputStream()));
            } catch (IOException ex) {
                return ResponseEntity.internalServerError().build();
            }
        }
    }
}