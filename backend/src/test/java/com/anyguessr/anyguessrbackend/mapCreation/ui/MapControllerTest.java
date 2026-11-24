
package com.anyguessr.anyguessrbackend.mapCreation.ui;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MapControllerTest {

    @Test
    void getMapReturnsDimensionsAndDrawableRegions() throws Exception {
        // Isolated test database: does not change local or AWS MySQL.
        DriverManagerDataSource dataSource =
                new DriverManagerDataSource(
                        "jdbc:h2:mem:quizmapcontract;DB_CLOSE_DELAY=-1",
                        "sa",
                        ""
                );

        JdbcTemplate jdbc = new JdbcTemplate(dataSource);

        jdbc.execute("""
                CREATE TABLE map_data (
                    map_id BIGINT PRIMARY KEY,
                    map_name VARCHAR(100),
                    image_ref VARCHAR(500),
                    width INT,
                    height INT,
                    deleted_at TIMESTAMP
                )
                """);

        jdbc.execute("""
                CREATE TABLE regions (
                    region_id BIGINT PRIMARY KEY,
                    map_id BIGINT,
                    name VARCHAR(100),
                    polygon VARCHAR(4000)
                )
                """);

        jdbc.execute("""
                INSERT INTO map_data
                    (map_id, map_name, image_ref, width, height)
                VALUES
                    (2, 'Clickable Polygon Demo', '', 800, 500)
                """);

        jdbc.execute("""
                INSERT INTO regions
                    (region_id, map_id, name, polygon)
                VALUES
                    (4, 2, 'North Zone',
                     '[{"x":50,"y":80},{"x":240,"y":80},{"x":240,"y":360},{"x":50,"y":360}]'),
                    (5, 2, 'Central Zone',
                     '[{"x":300,"y":80},{"x":500,"y":80},{"x":500,"y":360},{"x":300,"y":360}]'),
                    (6, 2, 'South Zone',
                     '[{"x":560,"y":80},{"x":750,"y":80},{"x":750,"y":360},{"x":560,"y":360}]')
                """);

        MapController controller =
                new MapController(jdbc, new ObjectMapper());

        MockMvc mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();

        mvc.perform(get("/api/maps/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mapId").value(2))
                .andExpect(jsonPath("$.name").value("Clickable Polygon Demo"))
                .andExpect(jsonPath("$.width").value(800))
                .andExpect(jsonPath("$.height").value(500))
                .andExpect(jsonPath("$.regions.length()").value(3))
                .andExpect(jsonPath("$.regions[0].id").value(4))
                .andExpect(jsonPath("$.regions[0].name").value("North Zone"))
                .andExpect(jsonPath("$.regions[0].points.length()").value(4))
                .andExpect(jsonPath("$.regions[0].points[0].x").value(50.0))
                .andExpect(jsonPath("$.regions[1].id").value(5))
                .andExpect(jsonPath("$.regions[2].id").value(6));
    }
}
