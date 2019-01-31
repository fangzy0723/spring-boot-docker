package com.example.docker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by fangzy on 2019/1/31 12:07
 */
@RestController
public class DockerController {

    @GetMapping("/docker")
    public String dockerTest(){
        return "docker test demo application1";
    }
}
