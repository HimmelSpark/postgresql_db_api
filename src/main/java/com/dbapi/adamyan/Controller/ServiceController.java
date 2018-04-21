package com.dbapi.adamyan.Controller;

import com.dbapi.adamyan.DAO.ServiceDAO;
import com.dbapi.adamyan.Model.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/service")
public class ServiceController {
    private ServiceDAO serviceDAO;

    @Autowired
    public ServiceController(ServiceDAO serviceDAO) {
        this.serviceDAO = serviceDAO;
    }

    @GetMapping(path = "/status")
    public ResponseEntity getStatus() {
        Map<String, Integer> result = new HashMap<>();
        result.put("forum", serviceDAO.getForumCount());
        result.put("post", serviceDAO.getPostCount());
        result.put("thread", serviceDAO.getThreadCount());
        result.put("user", serviceDAO.getUserCount());
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @PostMapping(path = "/clear")
    public ResponseEntity clearDB() {
        serviceDAO.clearDB();
        return ResponseEntity.status(HttpStatus.OK).body(new Message("null"));
    }
}
