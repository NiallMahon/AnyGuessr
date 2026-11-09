package com.anyguessr.anyguessrbackend.mapCreation.ui;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper; // Spring Boot 4 uses Jackson 3

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@RestController 
@RequestMapping ("/api/maps")
public class MapController {

    public record PointDto(double x, double y) {}
    public record PolygonDto(String name, List<PointDto> points) {}
    public record MapExportRequest(Long mapId, String name, String imageRef, int width, int height, List<PolygonDto> polygons) {}
    public record MapSummary(long mapId, String name, String imageRef, int regionCount){}
    public record RegionDto(long id, String name, List<PointDto> points) {}
    public record MapDetail(long mapId, String name, String imageRef, int width, int height, List<RegionDto> regions) {}

    private static final long TEMP_OWNER_ID = 1L; // replace once real auth exists
    private static final Path UPLOAD_DIR = Path.of("uploads");

    private final JdbcTemplate jdbc;
    private final ObjectMapper json;

    public MapController(JdbcTemplate jdbc, ObjectMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

     /* ------------------------------------------------------------ images */

    @PostMapping("/images")
    public Map<String, String> upload(@RequestParam("image") MultipartFile file) throws IOException {
        String ext = switch (Objects.requireNonNullElse(file.getContentType(), "")) {
            case "image/png" -> "png";
            case "image/jpeg" -> "jpg";
            case "image/webp" -> "webp";
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Only PNG, JPEG or WebP images are allowed");
        };
        Files.createDirectories(UPLOAD_DIR);
        String name = UUID.randomUUID() + "." + ext;
        file.transferTo(UPLOAD_DIR.resolve(name).toAbsolutePath());
        return Map.of("imageRef", name);
    }

    // Regex on the path variable blocks "../" tricks.
    @GetMapping("/images/{name:[a-f0-9\\-]+\\.(?:png|jpg|webp)}")
    public ResponseEntity<Resource> image(@PathVariable String name) {
        Path file = UPLOAD_DIR.resolve(name);
        if (!Files.exists(file)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        MediaType type = name.endsWith(".png") ? MediaType.IMAGE_PNG
                : name.endsWith(".webp") ? MediaType.parseMediaType("image/webp")
                : MediaType.IMAGE_JPEG;
        return ResponseEntity.ok().contentType(type).body(new FileSystemResource(file));
    }

    /* -------------------------------------------------------------- read */

      @GetMapping
    public List<MapSummary> list() {
        return jdbc.query("""
                SELECT m.map_id, m.map_name, m.image_ref,
                       (SELECT COUNT(*) FROM regions r WHERE r.map_id = m.map_id) AS cnt
                FROM map_data m WHERE m.deleted_at IS NULL ORDER BY m.map_id DESC
                """,
                (rs, i) -> new MapSummary(rs.getLong("map_id"), rs.getString("map_name"),
                        rs.getString("image_ref"), rs.getInt("cnt")));
    }

    @GetMapping("/{id}")
    public MapDetail get(@PathVariable long id) {
        var rows = jdbc.queryForList(
                "SELECT map_name, image_ref, width, height FROM map_data WHERE map_id = ? AND deleted_at IS NULL", id);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Map not found");
        var m = rows.get(0);

        List<RegionDto> regions = jdbc.query(
                "SELECT region_id, name, polygon FROM regions WHERE map_id = ? ORDER BY region_id", (rs, i) ->
                        new RegionDto(rs.getLong("region_id"), rs.getString("name"),
                                Arrays.asList(json.readValue(rs.getString("polygon"), PointDto[].class))), id);

        return new MapDetail(id, (String) m.get("map_name"), (String) m.get("image_ref"),
                ((Number) m.get("width")).intValue(), ((Number) m.get("height")).intValue(), regions);
    }

    /* ------------------------------------------------------------- write */

    @PostMapping
    @Transactional
    public Map<String, Long> save(@RequestBody MapExportRequest r) {
        validate(r);
        String imageRef = r.imageRef() == null ? "" : r.imageRef();
        long mapId;

        if (r.mapId() == null) {
            mapId = new SimpleJdbcInsert(jdbc).withTableName("map_data")
                    .usingGeneratedKeyColumns("map_id")
                    .executeAndReturnKey(Map.of(
                            "owner_id", TEMP_OWNER_ID, "map_name", r.name().trim(),
                            "image_ref", imageRef, "width", r.width(), "height", r.height(),
                            "status", "PUBLISHED", "version", 1,
                            "created_at", LocalDateTime.now()))
                    .longValue();
        } else {
            mapId = r.mapId();
            int updated = jdbc.update("""
                    UPDATE map_data SET map_name=?, image_ref=?, width=?, height=?, version=version+1
                    WHERE map_id=? AND deleted_at IS NULL
                    """, r.name().trim(), imageRef, r.width(), r.height(), mapId);
            if (updated == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Map not found");
        }

        // Upsert by name so region IDs (referenced by past guesses) survive edits.
        Map<String, Long> existing = new HashMap<>();
        jdbc.query("SELECT region_id, name FROM regions WHERE map_id = ?",
                rs -> { existing.put(rs.getString("name"), rs.getLong("region_id")); }, mapId);

        Set<String> keep = new HashSet<>();
        for (PolygonDto p : r.polygons()) {
            String name = p.name().trim();
            String points = json.writeValueAsString(p.points());
            keep.add(name);
            if (existing.containsKey(name)) {
                jdbc.update("UPDATE regions SET polygon = ? WHERE region_id = ?", points, existing.get(name));
            } else {
                jdbc.update("INSERT INTO regions (map_id, name, polygon) VALUES (?, ?, ?)", mapId, name, points);
            }
        }
        // Deleting a region cascades to its guesses; acceptable for a prototype.
        existing.forEach((name, regionId) -> {
            if (!keep.contains(name)) jdbc.update("DELETE FROM regions WHERE region_id = ?", regionId);
        });

        return Map.of("mapId", mapId);
    }

    private void validate(MapExportRequest r) {
        if (r.name() == null || r.name().isBlank() || r.name().length() > 100)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Map name is required (max 100 chars)");
        if (r.width() <= 0 || r.height() <= 0)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid map size");
        if (r.polygons() == null || r.polygons().isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one polygon is required");
        Set<String> names = new HashSet<>();
        for (PolygonDto p : r.polygons()) {
            if (p.name() == null || p.name().isBlank() || !names.add(p.name().trim().toLowerCase()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Polygon names must be unique and non-empty");
            if (p.points() == null || p.points().size() < 3)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each polygon needs at least 3 points");
        }
    }
}