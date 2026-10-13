package com.anyguessr.anyguessrbackend;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Map;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
public class HomeController {

    @RequestMapping("/profile/{id}")
    @CrossOrigin(origins = "http://localhost:5173")
    public Map<String, Object> index(@PathVariable String id) {


        //fetch user data from db using id sent in request.

        return Map.of(
                "userId",id,
                "name","Niall Mahon",//some string
                "location","Shmuckville",//some string
                "intials","NM",//some string
                "gamesPlayed",67676767//some int
        );

    }

}